package cmsc435.light_gappedtransfertool;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class SendChooserActivity extends ActionBarActivity {
    private ListView fileList;
    private ArrayList<File> files;
    private ArrayList<File> directories;
    ArrayAdapter adapter;
    ArrayList<String> fileNames;
    private File path;
    private TextView textField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_chooser);
        textField = (TextView) findViewById(R.id.editText);
        fileList = (ListView) findViewById(R.id.listView);

        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        initialize(path);

        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                if(position <= directories.size()-1) {
                    chooseFile(directories.get(position));
                } else {
                    int index = position - directories.size();
                    chooseFile(files.get(index));
                }
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

    private void chooseFile(File file) {
        if (file != null) {
            if(file.isDirectory()){
                path = file;
                textField.setText(path.getAbsolutePath());
                initialize(path);
            } else {
                File compressedFile = compressFile(file);
                Intent intent = new Intent(this, SenderActivity.class);
                intent.putExtra("FILE_PATH", compressedFile.getPath());
                startActivity(intent);
            }

        }

    }

    private File compressFile(File file) {

        int BUFFER = 2048;
        String compressedFileName = file.getName() + ".zip";
        try{
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(path.getAbsolutePath() + "/" + compressedFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];

            FileInputStream fi = new FileInputStream(path.getAbsolutePath() + "/" + file.getName());
            origin = new BufferedInputStream(fi, BUFFER);

            ZipEntry entry = new ZipEntry(compressedFileName);
            out.putNextEntry(entry);

            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
            origin.close();

        } catch(Exception e) {
            e.printStackTrace();
        }

        File compressedFile = new File(path.getAbsolutePath() + "/" + compressedFileName);
        return compressedFile;
    }

    private void initialize(File directory ) {
        textField.setText(directory.getAbsolutePath());
        files =  new ArrayList<File>();
        directories = new ArrayList<File>();
        getListFiles(directory);

        fileNames = new ArrayList<>();
        for(File file : directories) {
            fileNames.add(file.getName() + "/");
        }
        for(File file : files) {
            fileNames.add(file.getName());
        }

        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, fileNames);
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
}
