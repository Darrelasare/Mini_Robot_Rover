package rc.rover;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

import rc.rover.model.RoverStatus;
import rc.rover.model.SensorTimer;
import rc.rover.model.UserAction;


public class Controls extends AppCompatActivity {

    private final Float DISTANCE_INCREMENT = 5f;
    private final Float RPM_MAX_VALUE = 1000f;
    private final Float RPM_INCREMENT = 10f;

    private DatabaseReference myUserRef;
    private DatabaseReference myRoverRef;

    FirebaseAuth firebaseAuth;
    private TextView rpmView;
    private TextView distanceView;
    private View forwardBtn, backwardBtn, leftBtn, rightBtn, stopBtn;
    private UserAction userAction;
    private RoverStatus roverStatus;
    SensorTimer eventTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);
        rpmView = this.findViewById(R.id.rpm);
        distanceView = this.findViewById(R.id.distance);
        forwardBtn = this.findViewById(R.id.forwardBtn);
        backwardBtn = this.findViewById(R.id.backwardBtn);
        leftBtn = this.findViewById(R.id.leftBtn);
        rightBtn = this.findViewById(R.id.rightBtn);
        stopBtn = this.findViewById(R.id.stopBtn);

        //NOTES: Process the button presses on the view
        View.OnClickListener listener = view -> {
            if (view.getId() == R.id.leftBtn) {
                sendAction(new UserAction("mini", UserAction.Direction.left, UserAction.Throttle.none));
            }
            if (view.getId() == R.id.rightBtn) {
                sendAction(new UserAction("mini", UserAction.Direction.right, UserAction.Throttle.none));
            }
            if (view.getId() == R.id.forwardBtn) {
                sendAction(new UserAction("mini", UserAction.Direction.none, UserAction.Throttle.forward));
            }
            if (view.getId() == R.id.backwardBtn) {
                sendAction(new UserAction("mini", UserAction.Direction.none, UserAction.Throttle.reverse));
            }
            if (view.getId() == R.id.stopBtn) {
                sendAction(new UserAction("mini", UserAction.Direction.none, UserAction.Throttle.none));
            }
        };



        //add the event listener to the buttons so that we can process when the user presses the button
        forwardBtn.setOnClickListener(listener);
        backwardBtn.setOnClickListener(listener);
        leftBtn.setOnClickListener(listener);
        rightBtn.setOnClickListener(listener);
        stopBtn.setOnClickListener(listener);

        //user the timer to fake the car movement
        eventTimer = new SensorTimer("MOVE_CAR", 100, new Runnable() {
            @Override
            public void run() {
                if (roverStatus != null && userAction != null) {
                    Float rpm = roverStatus.getRpm(), originalRpm = roverStatus.getRpm();
                    Float distance = roverStatus.getDistance(), originalDistance = roverStatus.getDistance();

                    //detect 'forward' and 'backward' press and increase and decrease RPM and distance
                    switch (userAction.getMotion())
                    {
                        case forward:
                            if (rpm < RPM_MAX_VALUE) {
                                rpm += RPM_INCREMENT;
                                distance -= DISTANCE_INCREMENT;
                            }
                            distance -= DISTANCE_INCREMENT;
                            roverStatus.setThrottle(UserAction.Throttle.forward);
                            break;
                        case reverse:
                            if (rpm > -RPM_MAX_VALUE) {
                                rpm -= RPM_INCREMENT;
                                distance += DISTANCE_INCREMENT;
                            }
                            distance += DISTANCE_INCREMENT;
                            roverStatus.setThrottle(UserAction.Throttle.reverse);
                            break;
                        case none:
                            rpm = 0f;
                            if (roverStatus.getThrottle() == UserAction.Throttle.forward) {
                                distance -= DISTANCE_INCREMENT;
                            } else if (roverStatus.getThrottle() == UserAction.Throttle.reverse) {
                                distance += DISTANCE_INCREMENT;
                            }
                            break;
                    }

                    //keep the distance at 0 for collision because you can't be inside a wall
                    if (distance < 0) {
                        distance = 0f;
                    }

                    //detect direction change and randomize distance
                    if (userAction.getDirection() != UserAction.Direction.none) {
                        //update the distance from the wall if the direction changed
                        if (!userAction.getDirection().equals(roverStatus.getDirection())) {
                            //user the random class
                            Random r = new Random();
                            distance = r.nextInt(1000 + 1) + 500f;
                        }
                        roverStatus.setDirection(userAction.getDirection());
                    }

                    //send the changed values to firebase
                    if (Math.abs(rpm) < 2000 && Math.abs(distance) < 2000) {
                        //write the new rpm and distance to firebase
                        if (!originalRpm.equals(rpm) || !originalDistance.equals(distance)) {
                            myRoverRef.setValue(new RoverStatus("mini", rpm, distance));
                        }
                    }
                }
            }
        });

        //load the firebase database values and detect changes
        getDatabase();
        retrieveData();

        //start the event timer
        eventTimer.start();
    }

    /**
     * Used to send the user action to firebase to save
     * @param action the action pressed by the user
     */
    private void sendAction(UserAction action) {
        if (action == null) {
            return;
        }
        Log.i("CONTROLS", "Sending action " + action);
        if (firebaseAuth.getUid() != null) {
            //this line updates the firebase user node with the user action
            myUserRef.child(firebaseAuth.getUid()).setValue(action);
        } else {
            Log.w("CONTROLS", "Unable to send action. Please login first");
        }
    }

    /**
     * Load the firebase database references.
     */
    private void getDatabase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //point to the "mini" rover instance for information.
        myRoverRef = database.getReference("rover_status/mini");
        //point to the root node for all user actions
        myUserRef = database.getReference("user_action/");
    }

    private void retrieveData(){
        //the rover information is stored on one-node. Get the RPM and distance anytime it changes
        myRoverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                RoverStatus s = dataSnapshot.getValue(RoverStatus.class);
                assert s != null;

                if (roverStatus == null) {
                    roverStatus = s;
                }
                roverStatus.setDistance(s.getDistance());
                roverStatus.setRpm(s.getRpm());
                roverStatus.setRoverName(s.getRoverName());

                rpmView.setText(Float.toString(roverStatus.getRpm()));
                distanceView.setText(Float.toString(roverStatus.getDistance()));

                Log.d("STATUS", "status " + roverStatus.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read for rover status failed: " + databaseError.getCode());
            }
        });

        //if the user is logged in, read the user action and increase robot speed
        if (firebaseAuth.getUid() != null) {
            myUserRef.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userAction = dataSnapshot.getValue(UserAction.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    System.out.println("The read for user action for user " + firebaseAuth.getUid() + " failed: " + databaseError.getCode());
                }
            });
        }
    }

    public boolean onCreateOptionsMenu(Menu menu){

        getMenuInflater().inflate(R.menu.controls_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.sign_out) {
            firebaseAuth.signOut();
            finish();
            Intent intent = new Intent(getApplicationContext(),Login.class);
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onDestroy() {
        eventTimer.stop();
        super.onDestroy();
    }
}



