package rc.rover;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.nkzawa.emitter.Emitter;
import com.google.firebase.auth.FirebaseAuth;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;


public class Controls extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private TextView rpmView;
    private TextView distanceView;
    private View forwardBtn, backwardBtn, leftBtn, rightBtn, stopBtn;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.0.29:5000");
        } catch (URISyntaxException e) {}
    }

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

        mSocket.on("status", onNewMessage);
        mSocket.connect();
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
        mSocket.emit(action, message);
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Controls.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String rpm;
                    String distance;
                    try {
                        rpm = data.getString("rpm");
                        distance = data.getString("distance");
                    } catch (JSONException e) {
                        Log.e("CONTROLS", "Failed while receiving data from rpi: " + data.toString() + "Exception: " + e.toString());
                        return;
                    }
                    Log.i("CONTROLS", "Received data from rpi: " + data.toString());
                    rpmView.setText(rpm);
                    distanceView.setText(distance);
                }
            });
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off("status", onNewMessage);
    }

}



