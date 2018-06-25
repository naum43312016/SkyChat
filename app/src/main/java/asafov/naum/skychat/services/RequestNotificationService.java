package asafov.naum.skychat.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import asafov.naum.skychat.ListFriendsActivity;
import asafov.naum.skychat.MainActivity;
import asafov.naum.skychat.R;
import asafov.naum.skychat.User;
import asafov.naum.skychat.chat.Message;
import asafov.naum.skychat.chat.Tags;
import asafov.naum.skychat.notifications.Notification;

/**
 * Created by user on 04/06/2018.
 */

public class RequestNotificationService extends Service {

    private DatabaseReference database;
    Handler handler;
    Runnable runnable;
    NotificationManager notManager;
    public static List<Notification> requestNotificationList;
    public static final String NOTIFICATION_CHANNEL_ID = "2526";
    public static final String NOTIFICATION_CHANNEL_NAME = "RequestNotification";


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ListFriendsActivity.ReqNotFlag = true;
        if (ListFriendsActivity.curUser == null){
            stopSelf();
        }
        notManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        database = FirebaseDatabase.getInstance().getReference();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                getAllRequests();
            }
        };
        handler.postDelayed(runnable,800);
        requestNotificationList = new ArrayList<>();
        return super.onStartCommand(intent, flags, startId);
    }

    private void getAllRequests(){
        database.child("Request").child("friendsRequestReceived").child(ListFriendsActivity.curUser.getNickname()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null){
                    createNotification(user);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                User userRequest = dataSnapshot.getValue(User.class);
                if (userRequest != null && requestNotificationList != null){
                    Notification notification = null;
                    for (Notification not : requestNotificationList){
                        notManager.cancel(not.getNotificationId());
                        notification = not;
                        break;
                    }
                    if (notification != null){
                        if (requestNotificationList.contains(notification)){
                            requestNotificationList.remove(notification);
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void createNotification(User user){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this,"RequestNotification");
        mBuilder.setSmallIcon(R.drawable.notificon);
        mBuilder.setContentTitle("New friend request");
        mBuilder.setContentText("User " + user.getNickname() + " sent you friend request");
        mBuilder.setAutoCancel(true);
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = NOTIFICATION_CHANNEL_NAME;
            String description = "Notifications of request";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);

        Intent resultIntent = new Intent(this,ListFriendsActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        int notificationId = new Random().nextInt();
        NotificationManager mNotificationMangaer =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationMangaer.notify(notificationId,mBuilder.build());
        Notification notification = new Notification(notificationId,user);
        requestNotificationList.add(notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        ListFriendsActivity.ReqNotFlag = false;
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }
}
