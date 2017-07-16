package cmsc435.light_gappedtransfertool;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import net.glxn.qrgen.android.QRCode;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;


public class SenderActivity extends ActionBarActivity {
    private File file;

    private String headerString;
    private ArrayList<String> contentStrings;

    private boolean isEncoding;
    private boolean isResumed;
    private int qrCodeIndex;

    private int padding;

    private QRThread qrThread;

    private EditText textField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);

        Intent intent = getIntent();
        file = new File(intent.getStringExtra("FILE_PATH"));

        headerString = null;
        contentStrings = new ArrayList<>();

        isEncoding = false;
        isResumed = false;
        qrCodeIndex = 0;

        textField = (EditText) findViewById(R.id.editText);
        textField.setHint("ID #");

        textField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onTextfieldFinished(textField);
                }
                return false;
            }
        });

        updateButtons();
        readFile();
    }

    //Uses fields to enable and disable the correct buttons
    private void updateButtons() {
        Button previousButton = (Button) findViewById(R.id.previousButton);
        Button encodeButton = (Button) findViewById(R.id.encodeButton);
        Button resumeButton = (Button) findViewById(R.id.resumeButton);
        Button pauseButton = (Button) findViewById(R.id.pauseButton);
        Button nextButton = (Button) findViewById(R.id.nextButton);

        if (isEncoding) {
            encodeButton.setEnabled(false);
            ((TextView) findViewById(R.id.qrLabel)).setText("QR Code: " + (qrCodeIndex+1) + "/" + contentStrings.size());
            textField.setText(Integer.toString(qrCodeIndex+1));

            if (isResumed) {
                resumeButton.setEnabled(false);
                pauseButton.setEnabled(true);

                previousButton.setEnabled(false);
                nextButton.setEnabled(false);

                textField.setEnabled(false);
            } else {
                resumeButton.setEnabled(qrCodeIndex < contentStrings.size() - 1);
                pauseButton.setEnabled(false);

                previousButton.setEnabled(qrCodeIndex > 0);
                nextButton.setEnabled(qrCodeIndex < contentStrings.size() - 1);

                textField.setEnabled(true);
            }
        } else {
            encodeButton.setEnabled(true);

            resumeButton.setEnabled(false);
            pauseButton.setEnabled(false);
            previousButton.setEnabled(false);
            nextButton.setEnabled(false);

            textField.setEnabled(false);
        }
    }

    public void onTextfieldFinished(View view) {
        String text = textField.getText().toString();
        try {
            int newIndex = Integer.parseInt(text)-1;
            if (newIndex >= 0 && newIndex <= contentStrings.size() - 1) {
                qrCodeIndex = newIndex;
                updateButtons();
                displayProperQRCode();
            }
        } catch (NumberFormatException e) {
            //do nothing
        }
        textField.setText(Integer.toString(qrCodeIndex+1));
    }

    public void onPreviousPressed(View view) {
        if (qrCodeIndex > 0) {
            qrCodeIndex--;
            updateButtons();
            displayProperQRCode();
        }
    }

    public void onEncodePressed(View view) {
        isEncoding = true;
        resume();
        updateButtons();
        displayProperQRCode();
    }

    public void onNextPressed(View view) {
        if (qrCodeIndex < contentStrings.size() - 1) {
            qrCodeIndex++;
            updateButtons();
            displayProperQRCode();
        }
    }

    public void onResumePressed(View view) {
        resume();
        updateButtons();
    }

    public void onPausePressed(View view) {
        pause();
        updateButtons();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        displayProperQRCode();
    }

    public void displayProperQRCode() {
        if (!isEncoding && headerString != null) {
            displayQRCode(headerString);
        } else if (qrCodeIndex < contentStrings.size()) {
            displayQRCode(contentStrings.get(qrCodeIndex));
        }
    }

    public void displayQRCode(String str) {
        ImageView qrImage = (ImageView) findViewById(R.id.imageView);

        QRCode code = QRCode.from(str);
        code.withErrorCorrection(ErrorCorrectionLevel.L);
        code.withSize(qrImage.getWidth(), qrImage.getHeight());
        Bitmap myBitmap = code.bitmap();

        qrImage.setImageBitmap(myBitmap);
    }

    //Reads "length*3" bytes from "in" and puts the base 16 result in "str". Returns the number of bytes read divided by 3
    private int readBase16FromBinary(BufferedInputStream in, int length, StringBuilder str) throws IOException {
        for (int i=0; i<length; i++) {
            int byte1 = in.read();
            int byte2 = in.read();
            int byte3 = in.read();
            if (byte1 == -1) {return i;}
            else if (byte2 == -1) {padding = 2; byte2 = 0; byte3 = 0;}
            else if (byte3 == -1) {padding = 1; byte3 = 0;}

            int value = (byte1*0x100 + byte2)*0x100 + byte3;

            int char1 = (value/0x40000) + 48;
            int char2 = ((value/0x1000)%0x40) + 48;
            int char3 = ((value/0x40)%0x40) + 48;
            int char4 = (value%0x40) + 48;

            String toWrite = "" + (char)char1 + (char)char2 + (char)char3 + (char)char4;
            str.append(toWrite);
        }

        return length;
    }

    private void readFile() {
        try {
            BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));

            try {
                StringBuilder str;
                padding = 0;
                while (readBase16FromBinary(fis, 128, (str = new StringBuilder())) > 0) {
                    contentStrings.add((contentStrings.size() + 1) + ";" + str.toString());
                    System.out.println(contentStrings.get(contentStrings.size()-1));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        headerString = file.getName() + ";" + contentStrings.size() + ";" + padding;
        System.out.println(headerString);
    }

    private class QRThread extends Thread {

        public void updateUI() {
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    updateButtons();
                    displayProperQRCode();
                }
            });
        }

        public void run() {
            while (true) {
                try {
                    Thread.sleep(500);
                    if (qrCodeIndex < contentStrings.size() - 1) {
                        qrCodeIndex++;
                        if (qrCodeIndex == contentStrings.size() - 1) {
                            isResumed = false;
                        }
                        updateUI();
                    }
                    if (!isResumed) {
                        return;
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    public void resume() {
        if (qrCodeIndex < contentStrings.size() - 1) {
            isResumed = true;
            qrThread = new QRThread();
            qrThread.start();
        }
    }

    public void pause() {
        isResumed = false;
        if (qrThread != null && qrThread.isAlive()) {
            qrThread.interrupt();
            try {
                qrThread.join();
            } catch (InterruptedException e) {
                //do nothing
            }
        }
        qrThread = null;
    }
}
