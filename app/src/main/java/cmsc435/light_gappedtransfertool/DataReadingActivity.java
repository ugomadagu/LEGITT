package cmsc435.light_gappedtransfertool;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;


public class DataReadingActivity extends ActionBarActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_reading);

        // Get the message from the intent
        Intent intent = getIntent();
        ArrayList<String> messages = intent.getStringArrayListExtra("MESSAGE");

        // Create the text view
        TextView textView = (TextView)findViewById(R.id.textView);
        StringBuilder message = new StringBuilder();
        for(String token : messages) {
            message.append(token);
        }
        textView.setText(message);

    }
}
