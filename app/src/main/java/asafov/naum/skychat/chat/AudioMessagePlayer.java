package asafov.naum.skychat.chat;

import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by user on 02/05/2018.
 */

public class AudioMessagePlayer{
    MediaPlayer mediaPlayer;
    String filePath;

    public AudioMessagePlayer(){}

    public void setupMediaPlayer(String filePath){
        this.filePath = filePath;
        mediaPlayer = new MediaPlayer();
    }
    public void startMediaplayer(){
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }
    public void stopMediaPlayer(){
        if (mediaPlayer!=null){
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            }catch (IllegalStateException e){
                e.printStackTrace();
            }
        }
    }

    public int getDuration(){
        return mediaPlayer.getDuration();
    }
    public int getCurrentPossition(){
        return mediaPlayer.getCurrentPosition();
    }
}
