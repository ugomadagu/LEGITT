package cmsc435.light_gappedtransfertool;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;


public class HomePageActivity extends ActionBarActivity {
    static final int SEND_DATA_REQUEST = 1;
    static final int SCAN_DATA_REQUEST = 2;
    static final int READ_DATA_REQUEST = 3;
    static final int WORKS_DATA_REQUEST = 4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
    }

    // scanCodes button starts Scanner
    public void scanCodes (View view) {

        Intent intent = new Intent(this, Scanner.class);
        startActivityForResult(intent, SCAN_DATA_REQUEST);
    }

    public void readCodes(View view) {

        Intent intent = new Intent(this, Reader.class);
        startActivityForResult(intent, READ_DATA_REQUEST);
    }

    public void sendFiles (View view) {
        Intent intent = new Intent(this, SendChooserActivity.class);
        startActivityForResult(intent, SEND_DATA_REQUEST);
    }

    public void howItWorksMethod (View view) {
        Intent intent = new Intent(this, HowItWorksActivity.class);
        startActivityForResult(intent, WORKS_DATA_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Do nothing
    }


}
