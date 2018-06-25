package asafov.naum.skychat.permission;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import asafov.naum.skychat.chat.AudioRecorder;

/**
 * Created by user on 01/05/2018.
 */

public interface Permission {

    boolean checkPermissionOnDevice();
    void requestPermission();
}
