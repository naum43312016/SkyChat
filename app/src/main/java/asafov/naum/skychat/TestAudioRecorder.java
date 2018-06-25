package asafov.naum.skychat;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class TestAudioRecorder extends AppCompatActivity {

    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    String pathSave;
    Button recStartBtn,recStopBtn,btnPlayStart,btnStopPlay;

    final int REQUEST_PERMISSION_CODE = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_audio_recorder);
        if(!checkPermissionOnDevice()){
            requestPermission();
        }
        recStartBtn = (Button) findViewById(R.id.btnStartRecord);
        recStopBtn = (Button) findViewById(R.id.btnRecordStop);
        btnPlayStart = (Button) findViewById(R.id.btnStartPlay);
        btnStopPlay = (Button) findViewById(R.id.btnStopPlay);


            recStartBtn.setOnClickListener(new View.OnClickListener() {//Record
                @Override
                public void onClick(View view) {
                    if (checkPermissionOnDevice()) {
                    pathSave = Environment.getExternalStorageDirectory()
                            .getAbsolutePath() + "/"
                            + UUID.randomUUID().toString() + "_audio_record.3gp";
                    setupMediaRecorder();
                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(TestAudioRecorder.this,"Recording...",Toast.LENGTH_SHORT).show();
                    }else {
                        requestPermission();
                    }
                    recStopBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mediaRecorder.stop();
                        }
                    });

                    btnPlayStart.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mediaPlayer = new MediaPlayer();
                            try {
                                mediaPlayer.setDataSource(pathSave);
                                mediaPlayer.prepare();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mediaPlayer.start();
                            Toast.makeText(TestAudioRecorder.this, "Playing...", Toast.LENGTH_SHORT).show();

                        }
                    });
                    btnStopPlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (mediaPlayer != null){
                                mediaPlayer.stop();
                                mediaPlayer.release();
                                setupMediaRecorder();
                            }
                        }
                    });
                }
            });
    }

    private void setupMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(pathSave);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        },REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){

            case REQUEST_PERMISSION_CODE:
            {
                if (grantResults.length > 0 & grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this,"Permission granted",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }

    private boolean checkPermissionOnDevice() {
       int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
       int record_audio_result = ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO);
       return write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
               record_audio_result == PackageManager.PERMISSION_GRANTED;
    }



}
