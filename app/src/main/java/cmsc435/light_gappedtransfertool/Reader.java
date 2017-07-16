package cmsc435.light_gappedtransfertool;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;


public class Reader extends ActionBarActivity {
    private ListView fileList;
    private ArrayList<File> files;
    private ArrayList<File> directories;
    private File selectedFile;
    private int selectedIndex;
    ArrayAdapter adapter;
    ArrayList<String> fileNames;
    File path;
    private TextView textField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Choose File");
        setContentView(R.layout.activity_reader);

        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        textField = (TextView) findViewById(R.id.editText);

        fileList = (ListView) findViewById(R.id.listView);
        initialize(path);
        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                if(position <= directories.size()-1) {
                    selectedFile = directories.get(position);
                } else {
                    int index = position - directories.size();
                    selectedFile = files.get(index);
                }
                selectedIndex = position;
            }
        });
    }

    private void getListFiles(File parentDir) {
        File[] fileCollection = parentDir.listFiles();
        if (fileCollection != null) {
            for (File file : fileCollection) {
                if (file.isDirectory()) {
                    directories.add(file);
                } else {
                    files.add(file);
                }
            }
        }
    }

    public void showFile(View view) {
        if(selectedFile != null) {

            if(selectedFile.isDirectory()){
                path = selectedFile;
                initialize(path);
            } else {
                Uri directoryPath = Uri.fromFile(selectedFile.getAbsoluteFile());
                Intent fileOpenIntent = new Intent(Intent.ACTION_VIEW);
                fileOpenIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                String extension = MimeTypeMap.getFileExtensionFromUrl(directoryPath.getPath());
                String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if (type == null)
                    type = "text/plain";

                try {
                    fileOpenIntent.setDataAndType(directoryPath, type);
                    startActivity(fileOpenIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "You do not have any applications that can open this type of file.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void deleteFile(View view) {
        if(selectedFile != null) {
            if(selectedFile.isDirectory()) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle("Delete Conformation");
                dialogBuilder.setMessage("Are you sure you want to delete this entire folder?");
                dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteDirectory(selectedFile);
                        fileNames.remove(selectedIndex);
                        files.remove(selectedIndex);
                        adapter.notifyDataSetChanged();
                        fileList.clearChoices();
                        fileList.requestLayout();
                    }
                });
                dialogBuilder.setNegativeButton("No, go back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                });
                AlertDialog deleteConfirmation = dialogBuilder.create();
                deleteConfirmation.show();
            } else {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle("Delete Conformation");
                dialogBuilder.setMessage("Are you sure you want to delete this file?");
                dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedFile.delete();
                        fileNames.remove(selectedIndex);
                        files.remove(selectedIndex);
                        adapter.notifyDataSetChanged();
                        fileList.clearChoices();
                        fileList.requestLayout();
                        Toast.makeText(getBaseContext(), selectedFile.getName() + " deleted", Toast.LENGTH_LONG).show();
                    }
                });
                dialogBuilder.setNegativeButton("No, go back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                });
                AlertDialog deleteConfirmation = dialogBuilder.create();
                deleteConfirmation.show();
            }
        }
    }

    private void initialize(File path) {
        textField.setText(path.getAbsolutePath());
        selectedFile = null;
        files =  new ArrayList<File>();
        directories = new ArrayList<File>();
        getListFiles(path);

        fileNames = new ArrayList<>();
        for(File file : directories) {
            fileNames.add(file.getName() + "/");
        }
        for(File file : files) {
            fileNames.add(file.getName());
        }

        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_single_choice, fileNames);
        fileList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        fileList.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            if (path.getParent() != null) {
                path = path.getParentFile();
                textField.setText(path.getAbsolutePath());
                initialize(path);
            } else {
                finish();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }

    private void deleteDirectory(File directory) {
        File[] directoryFiles = directory.listFiles();
        if(directoryFiles.length == 0 ) {
            directory.delete();
        } else {
            for(File file : directoryFiles) {
                if(file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
            directory.delete();
        }
    }
}
