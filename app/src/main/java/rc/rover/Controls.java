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
import java.util.List;
import java.util.Map;

import rc.rover.model.RoverStatus;


public class Controls extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference myUserRef;
    private DatabaseReference myRoverRef;

    private TextView rpmView;
    private TextView distanceView;
    private View forwardBtn, backwardBtn, leftBtn, rightBtn, stopBtn;
    private RoverStatus roverStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        roverStatus = new RoverStatus();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);
        rpmView = this.findViewById(R.id.rpm);
        distanceView = this.findViewById(R.id.distance);
        forwardBtn = this.findViewById(R.id.forwardBtn);
        backwardBtn = this.findViewById(R.id.backwardBtn);
        leftBtn = this.findViewById(R.id.leftBtn);
        rightBtn = this.findViewById(R.id.rightBtn);
        stopBtn = this.findViewById(R.id.stopBtn);

        forwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSend("move", "f");
            }
        });

        backwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSend("move", "b");
            }
        });

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSend("move", "l");
            }
        });

        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSend("move", "r");
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSend("move", "s");
            }
        });

        getDatabase();
        retrieveData();
    }

    public boolean onCreateOptionsMenu(Menu menu){

        getMenuInflater().inflate(R.menu.controls_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.sign_out) {
            FirebaseAuth.getInstance().signOut();
            finish();
            Intent intent = new Intent(getApplicationContext(),Login.class);
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    private void attemptSend(String action, String message) {
        if (TextUtils.isEmpty(action) || TextUtils.isEmpty(message)) {
            return;
        }
        Log.i("CONTROLS", "Sending action " + action + " with message: " + message);

    }

    private void getDatabase(){
        // TODO: Find the reference form the database.
        database = FirebaseDatabase.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        myUserRef = database.getReference("users_rovers/" + mAuth.getUid());
        myRoverRef = database.getReference("users_rovers/mini");
    }

    private void retrieveData(){
        // TODO: Get the data on a single node.
        myUserRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Map<String, Object> statusMap = (Map<String, Object>)dataSnapshot.getValue();
                roverStatus.setRoverName((String)statusMap.get("roverName"));
                roverStatus.setDirection(RoverStatus.Direction.valueOf((String)statusMap.get("direction")));
                roverStatus.setMotion(RoverStatus.Throttle.valueOf((String)statusMap.get("motion")));

                rpmView.setText(Float.toString(roverStatus.getRpm()));
                distanceView.setText(Float.toString(roverStatus.getDistance()));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Map<String, Object> statusMap = (Map<String, Object>)dataSnapshot.getValue();
                roverStatus.setRoverName((String)statusMap.get("roverName"));
                roverStatus.setDirection(RoverStatus.Direction.valueOf((String)statusMap.get("direction")));
                roverStatus.setMotion(RoverStatus.Throttle.valueOf((String)statusMap.get("motion")));

                rpmView.setText(Float.toString(roverStatus.getRpm()));
                distanceView.setText(Float.toString(roverStatus.getDistance()));
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // TODO: Get the whole data array on a reference.
        myUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<RoverStatus> roverStatuses= new ArrayList<>();

                // TODO: Now data is retrieved, needs to process data.
                if (dataSnapshot.getValue() != null) {

                    // iterate all the items in the dataSnapshot
                    for (DataSnapshot a : dataSnapshot.getChildren()) {
                        RoverStatus rs = new RoverStatus(
                                a.getValue(RoverStatus.class).getRoverName(),
                                a.getValue(RoverStatus.class).getDirection(),
                                a.getValue(RoverStatus.class).getMotion(),
                                a.getValue(RoverStatus.class).getRpm(),
                                a.getValue(RoverStatus.class).getDistance()
                        );

                        roverStatuses.add(rs);  // now all the data is in roverStatuses.
                        Log.d("CONTROLS", "status " + rs.toString());
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Data Unavailable", Toast.LENGTH_LONG).show();
                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Getting data failed, log a message
                Log.d("CONTROLS", "Data Loading Canceled/Failed.", databaseError.toException());
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}



