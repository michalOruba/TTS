package pl.michaloruba.tts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private TextToSpeech tts;
    private Button btnSpeak;
    private EditText txtText;
    private Button slowRead;
    private Button normalRead;
    private Button fastRead;
    private Button loadFile;
    private BufferedReader br = null;
    private float readSpeed = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(this, this);

        btnSpeak = findViewById(R.id.btnSpeak);
        txtText = findViewById(R.id.txtText);
        slowRead = findViewById(R.id.slowRead);
        normalRead = findViewById(R.id.normalRead);
        fastRead = findViewById(R.id.fastRead);
        loadFile = findViewById(R.id.loadFile);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            verifyStoragePermissions(MainActivity.this);
        }
        else {
            try {
                String path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/Rok_1984.txt";
                File file = new File(path);
                if(!file.exists()){
                    file.createNewFile();
                    copyFiletoExternalStorage(R.raw.rok_1984, path);
                }
                br = new BufferedReader(new FileReader(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                speakOut(txtText.getText().toString());
            }
        });

        slowRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readSpeed = 0.4f;
                createToast(getString(R.string.slow));
                tts.setSpeechRate(readSpeed);
            }
        });

        normalRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readSpeed = 1.0f;
                createToast(getString(R.string.normal));
                tts.setSpeechRate(readSpeed);
            }
        });

        fastRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readSpeed = 3.0f;
                createToast(getString(R.string.fast));
                tts.setSpeechRate(readSpeed);
            }
        });

        loadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFile.setText(getString(R.string.next_part));
                    try {
                        speakOut(br.readLine());
                    } catch (IOException e){
                        e.printStackTrace();
                    }
            }
        });
    }

    private void createToast(String readSpeedText) {
        Toast.makeText(MainActivity.this, "Szybkość czytania: " + readSpeedText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(new Locale("pl_Pl"));

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                btnSpeak.setEnabled(true);
                speakOut(txtText.getText().toString());
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }
    private void speakOut(String text) {
        System.out.println(text);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void copyFiletoExternalStorage(int resourceId, String resourceName){
        String pathSDCard = resourceName;
        try{
            InputStream in = getResources().openRawResource(resourceId);
            FileOutputStream out = null;
            out = new FileOutputStream(pathSDCard);
            byte[] buff = new byte[1024];
            int read = 0;
            try {
                while ((read = in.read(buff)) > 0) {
                    out.write(buff, 0, read);
                }
            } finally {
                in.close();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            String path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/Rok_1984.txt";
            File file = new File(path);
            if(!file.exists()){
                file.createNewFile();
                copyFiletoExternalStorage(R.raw.rok_1984, path);
            }
            br = new BufferedReader(new FileReader(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
