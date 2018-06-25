package asafov.naum.skychat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import asafov.naum.skychat.chat.AudioRecorder;
import asafov.naum.skychat.chat.Chat;
import asafov.naum.skychat.chat.Message;
import asafov.naum.skychat.chat.Tags;
import asafov.naum.skychat.chat.Time;
import asafov.naum.skychat.permission.Permission;

public class ChatRoomActivity extends AppCompatActivity implements Permission{

    DatabaseReference myRef;
    List<Message> messageList;
    public static Chat chat;
    final int REQUEST_CODE_PHOTO = 2000;
    final int REQUEST_CODE_GALLERY = 3000;
    File cameraDirectory;
    File cameraDirectoryGet;
    String cameraPhotoName;
    String cameraPhotoLocation = "";
    User user;
    User curUser;
    ImageView btnSendMessage;
    EditText messageText;
    ChatRoomAdapter adapter;
    public static RecyclerView recyclerView;
    RelativeLayout chatRoomLayout;
    int chatMsgSize;
    final int REQUEST_PERMISSION_CODE = 1000;
    AudioRecorder recorder;
    String audioFilename,audioFilePath;
    public static String path;
    Handler handler;
    ProgressDialog audioRecordProgressDialog;
    ImageView attachFileChatRoom;
    ImageView closeButtonAttach;
    ImageView cameraAttach;
    ImageView galleryAttach;
    LinearLayout sendChoiceLinearLayout;
    String sendImageName;
    public static String IMAGE_SEND_PATH;
    public static String IMAGE_GET_PATH;
    public static String currentChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//Кнопка назад
        myRef = FirebaseDatabase.getInstance().getReference();
        btnSendMessage = (ImageView) findViewById(R.id.btnSendMessage);
        messageText = (EditText) findViewById(R.id.editTMessage);
        chatRoomLayout = (RelativeLayout) findViewById(R.id.chatRoomLayout);
        attachFileChatRoom = (ImageView) findViewById(R.id.attachFileChatRoom);
        closeButtonAttach = (ImageView) findViewById(R.id.btn_close);
        sendChoiceLinearLayout = (LinearLayout) findViewById(R.id.sendChoiceLinearLayout);
        cameraAttach = (ImageView) findViewById(R.id.cameraAttach);
        galleryAttach = (ImageView) findViewById(R.id.galleryAttach);
        chat = (Chat) getIntent().getSerializableExtra("chat");
        user = (User) getIntent().getSerializableExtra("user"); //second user not cur
        sendImageName = getIntent().getStringExtra("name");
        if (sendImageName != null && !sendImageName.equals("")){
            sendImage();
        }
        curUser = ListFriendsActivity.curUser;//current user
        messageList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recViewChat);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);//spuskaet rec view
        recyclerView.setHasFixedSize(true);
        adapter = new ChatRoomAdapter(messageList,this);
        recyclerView.setAdapter(adapter);
        currentChat = chat.getChatId();
        if (!checkPermissionOnDevice()){
            requestPermission();
        }
        //Path for audio messages
        path = getFilesDir().getPath() + File.separator
                + "SkyChatAudio" + File.separator + chat.getChatId();
        //Path for images send
        IMAGE_SEND_PATH = getFilesDir().getPath() + File.separator + "SkyChatImage" + File.separator +
                "cameraPictures" + File.separator +
                chat.getChatId() + File.separator + "send" + File.separator;
        IMAGE_GET_PATH = getFilesDir().getPath() + File.separator + "SkyChatImage" + File.separator +
                "cameraPictures" + File.separator +
                chat.getChatId() + File.separator + "get" + File.separator;
        getMessages();
        clickSendMessage();
        onImgViewAttFileClick();
        createCameraPhotoDirectory();
        cameraAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
        galleryAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onImageGalleryClicked();
            }
        });
    }

    private void clickSendMessage(){
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String,String> messageTime = initTime();
                if (!messageText.getText().toString().equals("")) {
                    String tag = Tags.TAG_MESSAGE;
                    Message message = new Message(curUser, user, messageText.getText().toString(),messageTime,tag);
                    List<Message> msgL = new ArrayList<>();
                    if (chat.getMessages() != null) {
                        for (Message msg : chat.getMessages()) {
                            msgL.add(msg);
                        }
                        msgL.add(message);
                        chat.setMessages(msgL);
                        Map<String, Object> tMap = new HashMap<>();
                        tMap.put(chat.getChatId(), chat);
                        FirebaseDatabase.getInstance().getReference().child("Chats").updateChildren(tMap);
                        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());//Spuskaet rec view v konec poslee novogo soobweniya
                    } else {
                        msgL.add(message);
                        chat.setMessages(msgL);
                        Map<String, Object> tMap = new HashMap<>();
                        tMap.put(chat.getChatId(), chat);
                        FirebaseDatabase.getInstance().getReference().child("Chats").updateChildren(tMap);
                    }
                    messageText.setText("");
                    hideKeyboard(chatRoomLayout);
                }
            }
        });
    }

    private void sendImage(){
        Map<String,String> messageTime = initTime();
        String tag = Tags.TAG_IMAGE_SEND;
        Message message = new Message(ListFriendsActivity.curUser, user,messageTime,tag,sendImageName);
        List<Message> msgL = new ArrayList<>();
        if (chat.getMessages() != null) {
            for (Message msg : chat.getMessages()) {
                msgL.add(msg);
            }
            msgL.add(message);
            chat.setMessages(msgL);
            Map<String, Object> tMap = new HashMap<>();
            tMap.put(chat.getChatId(), chat);
            FirebaseDatabase.getInstance().getReference().child("Chats").updateChildren(tMap);
            recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());//Spuskaet rec view v konec poslee novogo soobweniya
        } else {
            msgL.add(message);
            chat.setMessages(msgL);
            Map<String, Object> tMap = new HashMap<>();
            tMap.put(chat.getChatId(), chat);
            FirebaseDatabase.getInstance().getReference().child("Chats").updateChildren(tMap);
        }
    }

    private void getMessages(){
        FirebaseDatabase.getInstance().getReference().child("Chats").child(chat.getChatId()).child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                final Message message = dataSnapshot.getValue(Message.class);


                /*if (message != null){
                    if (!message.getSenderUser().getNickname().equals(curUser.getNickname())){
                        if (!message.isRead()) {
                            message.setRead(true);
                            FirebaseDatabase.getInstance().getReference().child("Chats").child(chat.getChatId()).child("messages")
                                    .child(dataSnapshot.getKey()).child("read").setValue(true);
                        }
                    }
                }*/

                messageList.add(message);
                chat.setMessages(messageList);
                ///////////////
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());//Spuskaet rec view v konec poslee novogo soobweniya
                adapter.notifyItemInserted(messageList.size());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);
                int index = getMessageIndex(message);
                if (index != -1){
                    messageList.set(index,message);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Message message = dataSnapshot.getValue(Message.class);
                int index = getMessageIndex(message);
                if (index != -1){
                    messageList.remove(index);
                    adapter.notifyItemRemoved(index);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                //startActivity(new Intent(ChatRoomActivity.this,ListFriendsActivity.class));
                break;
            case R.id.recordMic:
                if (!checkPermissionOnDevice()){
                    requestPermission();
                }else {
                    startRecording();
                }
                break;
        }
        return true;
    }


    //Время сообщения
    private Map<String,String> initTime() {
        Map<String,String> time = new HashMap<>();
        Date currentDate = new Date();
        String year = Time.FORMAT_YEAR.format(currentDate);
        String month = Time.FORMAT_MONTH.format(currentDate);
        String day = Time.FORMAT_DAY.format(currentDate);
        String hour = Time.FORMAT_HOUR.format(currentDate);
        String minute = Time.FORMAT_MINUTE.format(currentDate);
        String second = Time.FORMAT_SECOND.format(currentDate);
        time.put("year",year);
        time.put("month",month);
        time.put("day",day);
        time.put("hour",hour);
        time.put("minute",minute);
        time.put("second",second);
        return time;
    }

    //Спрятать клавиатуру
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    //Camera take pucture
    private void takePicture(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraPhotoName = UUID.randomUUID().toString();
        File photoFile = null;
        try{
            photoFile = createImageFile();
        }catch (IOException e) {
            e.printStackTrace();
        }
        Uri imageUri = FileProvider.getUriForFile(ChatRoomActivity.this,BuildConfig.APPLICATION_ID + ".fileprovider",photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_CODE_PHOTO);
    }
    public void onImageGalleryClicked() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();

        Uri data = Uri.parse(pictureDirectoryPath);

        photoPickerIntent.setDataAndType(data, "image/*");

        startActivityForResult(photoPickerIntent, REQUEST_CODE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PHOTO){
            if (resultCode == RESULT_OK){
                Intent cameraActivityIntent = new Intent(ChatRoomActivity.this,CameraActivity.class);
                cameraActivityIntent.putExtra(CameraActivity.BITMAP_PHOTO_CAMERA,cameraPhotoLocation);
                cameraActivityIntent.putExtra("chat",(Serializable)chat);
                cameraActivityIntent.putExtra("user",(Serializable)user);
                cameraActivityIntent.putExtra("name",cameraPhotoName);
                startActivity(cameraActivityIntent);
            }
        }
        if (requestCode == REQUEST_CODE_GALLERY){
            if (resultCode == RESULT_OK){
                Uri imageUri = data.getData();
                Intent galleryIntent = new Intent(ChatRoomActivity.this,CameraActivity.class);
                galleryIntent.putExtra("imageuri",imageUri.toString());
                galleryIntent.putExtra("chat",(Serializable)chat);
                galleryIntent.putExtra("user",(Serializable)user);
                startActivity(galleryIntent);
            }
        }
    }
    private File createImageFile() throws IOException {
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                cameraPhotoName,
                ".jpg",
                storageDir
        );
        cameraPhotoLocation = image.getAbsolutePath();
        return image;
    }

    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu,menu);
        return true;
    }

    //Recording
    private void startRecording(){
        audioRecordProgressDialog = new ProgressDialog(this);
        audioRecordProgressDialog.setMessage("Recording...");
        audioRecordProgressDialog.setButton(Dialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (recorder!= null) {
                    recorder.stopRecord();
                    if (handler!=null){
                        handler.removeCallbacksAndMessages(null);
                    }
                }
            }
        });
        audioRecordProgressDialog.setButton(Dialog.BUTTON_POSITIVE, "Finish", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (recorder != null) {
                    recorder.stopRecord();
                    if (handler!=null){
                        handler.removeCallbacksAndMessages(null);
                    }
                }
                saveAndSendAudio();
            }
        });
        audioRecordProgressDialog.show();
        audioRecordProgressDialog.setCancelable(false);
        //Audio file Path
        //Create Folder if not exist
        String path = getFilesDir().getPath() + File.separator
                + "SkyChatAudio" + File.separator + chat.getChatId();
        File child = new File(path);//Создание папки в каталоге SkyChatAudio
        child.mkdirs();
        //Record audio
        audioFilename = UUID.randomUUID().toString() + "_audio_record.3gp";
        audioFilePath = path + File.separator + audioFilename;
        recorder = new AudioRecorder();
        recorder.setupMediaRecorder(audioFilePath);
        recorder.startRecord();

        //Ograni4enie vremya zapisi
        final Activity activity = this;
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       if (audioRecordProgressDialog != null && audioRecordProgressDialog.isShowing()){
                        if (recorder!=null){
                            recorder.stopRecord();
                            createDialogMaximumAudioMessage();
                            }
                        }
                    }
                });
            }
        },(60*1000)*3);//3 minutes maximum audio message
    }
    private void saveAndSendAudio(){
        //Save message
        Map<String,String> messageTime = initTime();
            String tag = Tags.TAG_AUDIO_MESSAGE;
            Message message = new Message(curUser, user,messageTime,tag,audioFilename);
            List<Message> msgL = new ArrayList<>();
            if (chat.getMessages() != null) {
                for (Message msg : chat.getMessages()) {
                    msgL.add(msg);
                }
                msgL.add(message);
                chat.setMessages(msgL);
                Map<String, Object> tMap = new HashMap<>();
                tMap.put(chat.getChatId(), chat);
                FirebaseDatabase.getInstance().getReference().child("Chats").updateChildren(tMap);
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());//Spuskaet rec view v konec poslee novogo soobweniya
            } else {
                msgL.add(message);
                chat.setMessages(msgL);
                Map<String, Object> tMap = new HashMap<>();
                tMap.put(chat.getChatId(), chat);
                FirebaseDatabase.getInstance().getReference().child("Chats").updateChildren(tMap);
            }
        //Upload audio file
        StorageReference filepath = FirebaseStorage.getInstance().getReference().child("AudioMessage").child(chat.getChatId()).child(audioFilename);
        Uri file = Uri.fromFile(new File(audioFilePath));
        filepath.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Audio send error",Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Permission
    @Override
    public boolean checkPermissionOnDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO);
        int camera_result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED && camera_result == PackageManager.PERMISSION_GRANTED
                && read_external_storage_result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void requestPermission() {
        ActivityCompat.requestPermissions(this,new String[]{
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
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

    private void createDialogMaximumAudioMessage(){
        if (audioRecordProgressDialog.isShowing()){
            audioRecordProgressDialog.dismiss();
        }
        AlertDialog.Builder maxAudioDialog = new AlertDialog.Builder(this);
        maxAudioDialog.setMessage("Maximum audio message is 3 minutes");
        maxAudioDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (recorder != null) {
                    recorder.stopRecord();
                }
                saveAndSendAudio();
            }
        });
        maxAudioDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (recorder!= null) {
                    recorder.stopRecord();
                }
            }
        });
        maxAudioDialog.setCancelable(false);
        maxAudioDialog.show();
    }
    //Get message position
    private int getMessageIndex(Message message){
        int index = -1;
        if (chat!=null){
            for (int i = 0; i < messageList.size(); i++){
                    if (messageList.get(i).getMessageId().equals(message.getMessageId())) {
                        index = i;
                        break;
                    }
            }
        }
        return index;
    }

    public void onImgViewAttFileClick() {
        attachFileChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSendChoice();
            }
        });
    }
    public void showSendChoice(){
        sendChoiceLinearLayout.setVisibility(View.VISIBLE);
        attachFileChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sendChoiceLinearLayout.getVisibility() == View.VISIBLE){
                    sendChoiceLinearLayout.setVisibility(View.INVISIBLE);
                    onImgViewAttFileClick();
                }
            }
        });
        closeButtonAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendChoiceLinearLayout.setVisibility(View.INVISIBLE);
                onImgViewAttFileClick();
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Rect viewRect = new Rect();
        sendChoiceLinearLayout.getGlobalVisibleRect(viewRect);
        if (!viewRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
            if (sendChoiceLinearLayout.getVisibility() == View.VISIBLE){
                sendChoiceLinearLayout.setVisibility(View.INVISIBLE);
                onImgViewAttFileClick();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void createCameraPhotoDirectory(){
        cameraDirectory = new File(
                getFilesDir().getPath() +
                        File.separator + "SkyChatImage"+File.separator + "cameraPictures" + File.separator +
                        chat.getChatId() + File.separator + "send");
        if (!cameraDirectory.exists()){
            cameraDirectory.mkdirs();
        }
        cameraDirectoryGet = new File(
                getFilesDir().getPath() +
                        File.separator + "SkyChatImage"+File.separator + "cameraPictures" + File.separator +
                        chat.getChatId() + File.separator + "get");
        if (!cameraDirectoryGet.exists()){
            cameraDirectoryGet.mkdirs();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!messageList.isEmpty() && messageList != null){
                    for (Message msg : messageList){
                        if (msg != null){
                            if (!msg.getSenderUser().getNickname().equals(curUser.getNickname())){
                                if (!msg.isRead()){
                                    FirebaseDatabase.getInstance().getReference().child("Chats").child(chat.getChatId()).child("messages")
                                            .child(String.valueOf(messageList.indexOf(msg))).child("read").setValue(true);
                                }
                            }
                        }
                    }
                }
            }
        },500);
    }


    @Override
    protected void onStop() {
        currentChat = "";
        checkMessageState();
        super.onStop();
    }
    private void checkMessageState(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!messageList.isEmpty() && messageList != null){
                    for (Message msg : messageList){
                        if (msg != null){
                            if (!msg.getSenderUser().getNickname().equals(curUser.getNickname())){
                                if (!msg.isRead()){
                                    FirebaseDatabase.getInstance().getReference().child("Chats").child(chat.getChatId()).child("messages")
                                            .child(String.valueOf(messageList.indexOf(msg))).child("read").setValue(true);
                                }
                            }
                        }
                    }
                }
            }
        }).start();
    }
}