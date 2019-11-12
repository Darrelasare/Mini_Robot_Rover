package rc.rover;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;


public class Controls extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);

        firebaseAuth = FirebaseAuth.getInstance();
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

    }



