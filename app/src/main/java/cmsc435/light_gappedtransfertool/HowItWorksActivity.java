package cmsc435.light_gappedtransfertool;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

public class HowItWorksActivity extends ActionBarActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_it_works);

        String text = "Scanning Messages:\n" +
                "1.\tHit the “Scan Files” button.\n" +
                "2.\tPut the header QR code into the camera view and hold it there until the text field changes to show how many QR codes you have left to scan.\n" +
                "3.\tHave the person who is sending the data start sending the content QR codes.\n" +
                "4.\tIf you would like to stop scanning hit the “Stop” button to stop scanning. If you hit this button the file will not be saved.\n" +
                "5.\tIf you have missed any QR codes in the scanning process hit “Check Missed Codes” to see exactly which codes you missed.\n" +
                "6.\tUpon successful scanning of all QR codes, you will be sent back to the home screen and a toast message will appear telling you that your file has been saved.\n" +
                "\n" +
                "Sending Messages:\n" +
                "1.\tHit the “Send Files” button.\n" +
                "2.\tNavigate to the file you wish to send and click it. Folders are indicated by having a “/” at the end of the file name. Folders appear at the top of the file list. The top left arrow in the actionbar is used to go up a directory.\n" +
                "3.\tAfter clicking the file you want to transfer, the first QR Code that show up will be the header QR code. Have your recipient scan this code first.\n" +
                "4.\tHit “Encode” to start displaying the content QR codes.\n" +
                "5.\tYou may pause the transfer process at any time by pressing “Pause”.\n" +
                "6.\tWhen paused, you can navigate to different QR Codes either with the “Previous” and “Next” buttons or by entering the QR code ID into the text field.\n" +
                "\n" +
                "Managing Files:\n" +
                "1.\tHit the “Manage Scanned Files” button.\n" +
                "2.\tNavigate to the file you wish to send and click it. Folders are indicated by having a “/” at the end of the file name. Folders appear at the top of the file list. The top left arrow in the actionbar is used to go up a directory.\n";

        TextView scanText = (TextView)findViewById(R.id.text);
        scanText.setText(text);
    }
}
