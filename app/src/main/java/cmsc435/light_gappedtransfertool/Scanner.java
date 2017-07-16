package cmsc435.light_gappedtransfertool;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

/* Import ZBar Class files */
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import net.sourceforge.zbar.Config;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Scanner extends ActionBarActivity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;
    private ArrayList<String> messages;
    private String[] rawMessages;

    private TextView scanText;
    private Button stopButton;
    private Button checkMissedCodesButton;

    public static ImageScanner scanner;

    private boolean previewing = true;
    private boolean headerCodeScanned;
    private int padding;
    private String fileName;
    private int codesLeftToScan;
    private int highestQRCodeIDScanned;

    private QRThread qrThread;

    private CameraOverlay overlay;


    static {
        System.loadLibrary("iconv");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        autoFocusHandler = new Handler();
        mCamera = getCameraInstance();
        messages = new ArrayList<>();

        headerCodeScanned = false;

        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.ENABLE, 0);
        scanner.setConfig(Symbol.QRCODE, Config.ENABLE, 1);
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
        FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
        preview.addView(mPreview);
        overlay = new CameraOverlay(this);
        overlay.setCameraDimensions(mCamera.getParameters().getPreviewSize().width, mCamera.getParameters().getPreviewSize().height);
        preview.addView(overlay);

        scanText = (TextView)findViewById(R.id.scanText);
        scanText.setText("Please scan header code first.");

        stopButton = (Button)findViewById(R.id.StopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
            }
        });

        checkMissedCodesButton = (Button)findViewById(R.id.checkMissedCodesButton);
        checkMissedCodesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkForMissedQRCodes(); }
        });
    }

    public void onPause() {
        super.onPause();
        releaseCamera();
        mPreview.getHolder().removeCallback(mPreview);
        FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
        preview.removeView(mPreview);
        preview.removeView(overlay);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        // Get the Camera instance as the activity achieves full user focus
        if (mCamera == null) {
            autoFocusHandler = new Handler();
            mCamera = getCameraInstance(); // Local method to handle camera init
            mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
            FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
            preview.addView(mPreview);

            overlay = new CameraOverlay(this);
            overlay.setCameraDimensions(mCamera.getParameters().getPreviewSize().width, mCamera.getParameters().getPreviewSize().height);
            preview.addView(overlay);
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e){
        }
        return c;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private void interpretScannedMessage(String scannedMessage) {
        if(headerCodeScanned) {
            int currentQRCodeID;

            Pattern p = Pattern.compile("(\\d*);.*");
            Matcher m = p.matcher(scannedMessage);
            if(m.matches()) {
                currentQRCodeID = Integer.parseInt(m.group(1));
                if (rawMessages[currentQRCodeID - 1] == null) { //If the QR code being scanned has not been scanned before
                    String newMessage = scannedMessage.substring(scannedMessage.indexOf(';') + 1);
                    rawMessages[currentQRCodeID - 1] = newMessage;
                    if(currentQRCodeID > highestQRCodeIDScanned) {
                        highestQRCodeIDScanned = currentQRCodeID;
                    }
                    codesLeftToScan--;
                    scanText.setText("Scanning in progress: " + codesLeftToScan + " codes left.");
                    if (codesLeftToScan == 0) //If we have scanned the last QR Code
                        stopScanning();
                }
            }

        } else {
            Pattern p = Pattern.compile("(.*);(\\d+);(\\d+)");
            Matcher m = p.matcher(scannedMessage);
            if(m.matches()) {  //If the scanned in message is a header
                headerCodeScanned = true;
                fileName = m.group(1);
                codesLeftToScan = Integer.parseInt(m.group(2));
                rawMessages = new String[Integer.parseInt(m.group(2))];
                padding = Integer.parseInt(m.group(3));
                scanText.setText("Scanning in progress: " + codesLeftToScan + " codes left.");
                highestQRCodeIDScanned = 0;

                if (codesLeftToScan == 0) //If we have scanned the last QR Code
                    stopScanning();
            }
        }
    }

    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (qrThread == null || !qrThread.isAlive()) {
                qrThread = new QRThread(data, camera);
                qrThread.start();
            }
        }
    };

    private class QRThread extends Thread {

        private Image barcode;
        private ArrayList<String> messages;

        public QRThread(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);
            int width = barcode.getWidth(), height = barcode.getHeight();
            int cropWidth = Math.min(960, width), cropHeight = Math.min(540, height);
            barcode.setCrop(width/2-cropWidth/2, height/2-cropHeight/2, cropWidth, cropHeight);
        }

        public void messageReceived() {
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    for (String message : QRThread.this.messages) {
                        interpretScannedMessage(message);
                    }
                }
            });
        }

        public void run() {
            int result = scanner.scanImage(barcode);

            if (result != 0) {
                SymbolSet syms = scanner.getResults();
                messages = new ArrayList<>();

                for (Symbol sym : syms) {
                    messages.add(sym.getData());
                }

                messageReceived();
            }
        }
    }

    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 5000);
        }
    };

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                mCamera.autoFocus(autoFocusCB);
            }
    };

    private void writeBinaryFromBase16(BufferedOutputStream out, String str) throws IOException {
        int fileLength = str.length();
        for (int i=0; i*4<fileLength; i++) {
            boolean last = ((i+1)*4 == fileLength);

            int char1 = (int)str.charAt(i*4) - 48;
            int char2 = (int)str.charAt(i*4+1) - 48;
            int char3 = (int)str.charAt(i*4+2) - 48;
            int char4 = (int)str.charAt(i*4+3) - 48;

            int value = ((char1*0x40 + char2)*0x40 + char3)*0x40 + char4;

            int byte1 = (value/0x10000);
            int byte2 = (value/0x100)%0x100;
            int byte3 = (value%0x100);

            out.write(byte1);
            if (!last || padding < 2) out.write(byte2);
            if (!last || padding < 1) out.write(byte3);
        }
        out.flush();
        out.close();
    }

    private void stopScanning() {
        // Saves the message in a text file for later use
        Intent intent = getIntent();
        if (codesLeftToScan == 0 && headerCodeScanned) {

            StringBuilder finalMessage = new StringBuilder();
            for (String s : rawMessages) {
                if(s != null) {
                    finalMessage.append(s);
                    messages.add(s);
                }
            }
            try {
                File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Lightgap");
                path.mkdirs();
                if (path.isDirectory()) {
                    File outputFile = new File(path.getPath(), fileName);
                    FileOutputStream fou = new FileOutputStream(outputFile);
                    try {
                        writeBinaryFromBase16(new BufferedOutputStream(fou), finalMessage.toString());
                        Toast.makeText(getBaseContext(), fileName + " Saved", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

            intent.putExtra("SCAN_RESULT", "See file manager");
            setResult(RESULT_OK, intent);
		} else {
            setResult(RESULT_CANCELED, intent);
        }
        //-----------------------------------------------

        finish();
    }

    private void checkForMissedQRCodes() {
        String missedCodes = "";
        int start = -1;
        int end;
        for(int i = 0; i < highestQRCodeIDScanned; i++) {
            if(rawMessages[i] == null && start == -1) {
                start = i + 1;
            } else if(rawMessages[i] != null && start != -1) {
                end = i;
                if(start == end){
                    missedCodes += " " + start;
                } else {
                    missedCodes += " " + start + "-" + end;
                }
                start = -1;
            }
        }

        if(start != -1) {
            end = highestQRCodeIDScanned - 1;
            if(start == end){
                missedCodes += " " + start;
            } else {
                missedCodes += " " + start + "-" + end;
            }
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Missed QR Codes");
        if(missedCodes.compareTo("") == 0) {
            dialogBuilder.setMessage("IDs missed: None");
        } else {
            dialogBuilder.setMessage("IDs missed:" + missedCodes);
        }
        dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });
        AlertDialog deleteConfirmation = dialogBuilder.create();
        deleteConfirmation.show();
    }
}
