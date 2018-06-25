package asafov.naum.skychat.chat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

import java.io.IOException;

/**
 * Created by user on 01/05/2018.
 */

public class AudioRecorder{
    MediaRecorder mediaRecorder;
    final int REQUEST_PERMISSION_CODE = 1000;

    public AudioRecorder(){}

    public void setupMediaRecorder(String pathSave) {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(pathSave);
    }
    public void startRecord(){
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void stopRecord(){
        try {
            mediaRecorder.stop();
        }catch (IllegalStateException e){
            e.printStackTrace();
        }
    }

}
