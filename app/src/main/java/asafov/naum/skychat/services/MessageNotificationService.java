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

import asafov.naum.skychat.ChatRoomActivity;
import asafov.naum.skychat.ListFriendsActivity;
import asafov.naum.skychat.MainActivity;
import asafov.naum.skychat.R;
import asafov.naum.skychat.chat.Chat;
import asafov.naum.skychat.chat.Message;
import asafov.naum.skychat.chat.Tags;
import asafov.naum.skychat.notifications.Notification;

/**
 * Created by user on 28/05/2018.
 */

public class MessageNotificationService extends Service {
    public boolean flag;
    public final int mId = 5;
    private DatabaseReference database;
    public static List<Notification> notificationList;
    NotificationManager notManager;
    Handler handler;
    Runnable runnable;
    public static final String NOTIFICATION_CHANNEL_ID = "2525";
    public static final String NOTIFICATION_CHANNEL_NAME = "MessageNotification";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flag = false;
        ListFriendsActivity.MesNotFlag = true;
        Log.d("MYTAG","ONSTART SRVICE");
        if (ListFriendsActivity.curUser == null){
            stopSelf();
            //Log.d("MYTAG","cur user null");
            //Nujno zanovo zapustit service
        }
        //Log.d("MYTAG","On start service");
        if (ListFriendsActivity.curUser != null){
            //Log.d("MYTAG","Current user = " + ListFriendsActivity.curUser.getNickname());
        }
        //chatList = new ArrayList<>();
        notManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        database = FirebaseDatabase.getInstance().getReference();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                getAllChats();
                Log.d("MYTAG","V rune");
                //database.addChildEventListener(chatChildEventListener);
                //database.addChildEventListener(messageChildEventListener);
            }
        };
        handler.postDelayed(runnable,800);
        notificationList = new ArrayList<>();
        return super.onStartCommand(intent, flags, startId);
    }


    private boolean checkUsers(Chat chat){
        if (chat !=null && ListFriendsActivity.curUser != null) {
            if (chat.getUsers().get(0).getNickname().equals(ListFriendsActivity.curUser.getNickname())
                    || chat.getUsers().get(1).getNickname().equals(ListFriendsActivity.curUser.getNickname())) {
                return true;
            }
        }
        return false;
    }
    private void getAllChats(){
        database.child("Chats").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (checkUsers(dataSnapshot.getValue(Chat.class))) {
                    getMessages(dataSnapshot.getValue(Chat.class));
                    //chatList.add(dataSnapshot.getValue(Chat.class));
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                int index = ListFriendsActivity.getChatIndex(chat);
                //chatList.set(index,chat);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                int index = ListFriendsActivity.getChatIndex(chat);
                if (index != -1){
                    //chatList.remove(index);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Log.d("MYTAG","Owibka service chat");
                stopSelf();
            }
        });
    }
    private void getMessages(final Chat mChat){
        database.child("Chats").child(mChat.getChatId()).child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                final Message message = dataSnapshot.getValue(Message.class);
                if (!checkCurrentChat(mChat)) {
                    if (message != null) {
                        if (!message.getSenderUser().getNickname().equals(ListFriendsActivity.curUser.getNickname())) {
                            if (!message.isRead()) {
                                createNotification(message);
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //Remove notification
                Message chMes = dataSnapshot.getValue(Message.class);
                if (chMes != null && notificationList != null){
                    if (chMes.isRead()) {
                        Notification notification = null;
                        for (Notification not : notificationList) {
                            if (not.getMessageId().equals(chMes.getMessageId())) {
                                notManager.cancel(not.getNotificationId());
                                notification = not;
                                break;
                            }
                        }
                        if (notification != null){
                            if (notificationList.contains(notification)){
                                notificationList.remove(notification);
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Log.d("MYTAG","Owibka service messages");
                stopSelf();
            }
        });
    }


    private void createNotification(Message message){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this,"MessageNotification");
        mBuilder.setSmallIcon(R.drawable.notificon);
        mBuilder.setContentTitle("New message");
        switch (message.getTag()){
            case Tags.TAG_MESSAGE:
                mBuilder.setContentText(message.getSenderUser().getNickname() + " - " + message.getMessage());
                break;
            default:
                mBuilder.setContentText("You have new message from - " + message.getSenderUser().getNickname());
                break;
        }
        mBuilder.setAutoCancel(true);
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = NOTIFICATION_CHANNEL_NAME;
            String description = "Notifications of messages";
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
Log.d("MYTAG","NOT CRE");
        int notificationId = message.getRandomId();
        NotificationManager mNotificationMangaer =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationMangaer.notify(notificationId,mBuilder.build());
        Notification notification = new Notification(notificationId,message.getMessageId());
        notificationList.add(notification);
    }

    private boolean checkCurrentChat(Chat chat){
        if (ChatRoomActivity.currentChat != null){
            if (!ChatRoomActivity.currentChat.equals("")){
                if (ChatRoomActivity.currentChat.equals(chat.getChatId())){
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        ListFriendsActivity.MesNotFlag = false;
        handler.removeCallbacks(runnable);
        Log.d("MYTAG","DESTR service");
        //database.child("Chats").child(mChat.getChatId()).child("messages").addChildEventListener(null);
        super.onDestroy();
    }
}
