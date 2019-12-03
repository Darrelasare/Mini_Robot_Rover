package rc.rover;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rc.rover.model.RoverStatus;
import rc.rover.model.UserAction;


public class Controls extends AppCompatActivity {

    private DatabaseReference myUserRef;
    private DatabaseReference myRoverRef;

    FirebaseAuth firebaseAuth;
    private TextView rpmView;
    private TextView distanceView;
    private View forwardBtn, backwardBtn, leftBtn, rightBtn, stopBtn;
    private UserAction userAction;

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

        forwardBtn.setOnClickListener(listener);
        backwardBtn.setOnClickListener(listener);
        leftBtn.setOnClickListener(listener);
        rightBtn.setOnClickListener(listener);
        stopBtn.setOnClickListener(listener);

        getDatabase();
        retrieveData();
    }

    private void sendAction(UserAction action) {
        if (action == null) {
            return;
        }
        Log.i("CONTROLS", "Sending action " + action);
        if (firebaseAuth.getUid() != null) {
            myUserRef.child(firebaseAuth.getUid()).setValue(action);
        } else {
            Log.w("CONTROLS", "Unable to send action. Please login first");
        }
    }

    private void getDatabase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRoverRef = database.getReference("rover_status/mini");
        myUserRef = database.getReference("user_action/");
    }

    private void retrieveData(){
        // TODO: Get the data on a single node.
        myRoverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                RoverStatus roverStatus = dataSnapshot.getValue(RoverStatus.class);
                rpmView.setText(Float.toString(roverStatus.getRpm()));
                distanceView.setText(Float.toString(roverStatus.getDistance()));

                Log.d("STATUS", "status " + roverStatus.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read for rover status failed: " + databaseError.getCode());
            }
        });

        // TODO: Get the whole data array on a reference.
        myUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<UserAction> userActions = new ArrayList<>();

                // TODO: Now data is retrieved, needs to process data.
                if (dataSnapshot.getValue() != null) {

                    // iterate all the items in the dataSnapshot
                    for (DataSnapshot a : dataSnapshot.getChildren()) {
                        UserAction userAction = new UserAction(
                                a.getValue(UserAction.class).getRoverName(),
                                a.getValue(UserAction.class).getDirection(),
                                a.getValue(UserAction.class).getMotion()
                        );

                        userActions.add(userAction);
                        Log.d("CONTROLS", "status " + userAction.toString());
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Data Unavailable", Toast.LENGTH_LONG).show();
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read for user action failed: " + databaseError.getCode());
            }
        });
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
        super.onDestroy();
    }
}



