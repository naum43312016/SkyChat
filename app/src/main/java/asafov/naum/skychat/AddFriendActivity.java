package asafov.naum.skychat;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import asafov.naum.skychat.chat.Chat;
import asafov.naum.skychat.functions.Functions;

public class AddFriendActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference myRef;
    StorageReference storageRef;
    List<User> usersList;
    Button btnSearch;
    Button btnAdd;
    EditText edSearch;
    TextView txtSearchRes;
    ImageView userAccountIcon;
    LinearLayout searchResLinearLayout;
    User userSearchRes;
    User curUser;
    Bitmap profileBitmap;

    List<User> myRequestSent;
    List<User> myRequestReceived;
    List<User> friendsList;

    ProgressBar loading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//Кнопка назад
        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnAdd = (Button) findViewById(R.id.btnAddFriend);
        edSearch = (EditText) findViewById(R.id.edSearch);
        txtSearchRes = (TextView) findViewById(R.id.txtSearchRes);
        userAccountIcon = (ImageView) findViewById(R.id.userAccountIconAddFriend);
        searchResLinearLayout = (LinearLayout) findViewById(R.id.addFriendLinLayout);

        friendsList = (List<User>) getIntent().getSerializableExtra("list");

        //Loading
        loading = (ProgressBar) findViewById(R.id.loadingIconAddFriend);

        myRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference();

        //Get Current user
        myRef.child("users").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                curUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        usersList = new ArrayList<>();//Users array
        //Get all users and put it in array
        myRef.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Pravilnii sposob mi polu4aem vse objecti User iz chlid users
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    User someUser = postSnapshot.getValue(User.class);
                    if (!someUser.getEmail().equals(currentUser.getEmail()) && !someUser.getNickname().equals(curUser.getNickname())){
                            usersList.add(someUser);
                    }
                }
                getUserRequestsSend();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /*
        Кнопка поиска, проверяет если пользователь сушествует и показывать layout с его фото и эмейлом
         */
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchResLinearLayout.setVisibility(View.INVISIBLE);
                String searchStr = edSearch.getText().toString();
                if (!checkIfFriend(searchStr)){
                    if (checkIfUserExist(searchStr)){
                        userSearchRes = Functions.getUserByEmailOrNick(searchStr,usersList);
                        txtSearchRes.setText(userSearchRes.getNickname());
                        if (Functions.checkIfUserRequest(myRequestSent,userSearchRes)){
                            btnAdd.setText(R.string.requested);
                            btnAdd.setEnabled(false);
                        }
                        if (Functions.checkIfUserRequest(myRequestReceived,userSearchRes)){
                            addFriendBtn();
                        }else {
                            sendRequestBtn();
                        }

                        //loading start
                        loading.setVisibility(View.VISIBLE);
                        downloadProfilePhotoIntoImgView(userSearchRes.getNickname());
                    }else {
                        Toast.makeText(AddFriendActivity.this, "User " + searchStr + " not exist",
                                Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(AddFriendActivity.this, "User " + searchStr + " is your friend",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    //Кнопка добавление в друзья
    private void addFriendBtn(){
        btnAdd.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              //Добавление в друзья
              myRef.child("Friends").child(userSearchRes.getNickname()).child(ListFriendsActivity.curUser.getNickname()).setValue(ListFriendsActivity.curUser);
              myRef.child("Friends").child(ListFriendsActivity.curUser.getNickname()).child(userSearchRes.getNickname()).setValue(userSearchRes);
              //Добавление фото профиля нового друга
              //Сохранение изображение профиля друга в папке в памяти пользователя
              String f = getFilesDir().getPath() + File.separator + "SkyChatImage" + File.separator + "friends_profile_images" + File.separator + userSearchRes.getNickname();
              Functions.createFolder(f);//Create fodler
              File file = new File(getFilesDir().getPath() + File.separator + "SkyChatImage" + File.separator + "friends_profile_images" + File.separator + userSearchRes.getNickname() + File.separator + "profile.jpg");
              Functions.createBitmapInFolder(file, profileBitmap);
              //Create Chat
              Chat chat = Functions.createChat(curUser,userSearchRes);
              myRef.child("Chats").child(chat.getChatId()).setValue(chat);
              //Delete request
              myRef.child("Request").child("friendsRequestReceived").child(ListFriendsActivity.curUser.getNickname())
                      .child(userSearchRes.getNickname()).setValue(null);
              myRef.child("Request").child("friendsRequestSent").child(userSearchRes.getNickname()).child(ListFriendsActivity.curUser.getNickname()).setValue(null);
              Toast.makeText(getApplicationContext(), userSearchRes.getNickname() + " was added", Toast.LENGTH_SHORT).show();
              startActivity(new Intent(AddFriendActivity.this, MainActivity.class));
          }
          });
    }
    //Кнопка отпраки запроса на добавление в друзья
    private void sendRequestBtn() {
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //polu4atel
                myRef.child("Request").child("friendsRequestReceived").child(userSearchRes.getNickname()).child(curUser.getNickname()).setValue(curUser);
                //otpravitel
                myRef.child("Request").child("friendsRequestSent").child(curUser.getNickname()).child(userSearchRes.getNickname()).setValue(userSearchRes);
                Toast.makeText(AddFriendActivity.this, userSearchRes.getNickname() + " request was send!",
                        Toast.LENGTH_SHORT).show();
                //Сохранение изображение профиля друга в папке в памяти пользователя
                String f = getFilesDir().getPath() + File.separator + "SkyChatImage" + File.separator + "friends_profile_images" + File.separator + userSearchRes.getNickname();
                Functions.createFolder(f);//Create fodler
                File file = new File(getFilesDir().getPath() + File.separator + "SkyChatImage" + File.separator + "friends_profile_images" + File.separator + userSearchRes.getNickname() + File.separator + "profile.jpg");
                Functions.createBitmapInFolder(file, profileBitmap);
                startActivity(new Intent(AddFriendActivity.this, MainActivity.class));
            }
        });
    }
    //Get user requests sent
    private void getUserRequestsSend(){
        myRequestSent = new ArrayList<>();
        myRef.child("Request").child("friendsRequestSent").child(curUser.getNickname()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Pravilnii sposob mi polu4aem vse objecti User iz chlid users
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    User someUser = postSnapshot.getValue(User.class);
                    myRequestSent.add(someUser);
                }
                getUserRequestsReceived();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                getUserRequestsReceived();
            }
        });
    }
    //Get user requests received
    private void getUserRequestsReceived(){
        myRequestReceived = new ArrayList<>();
        myRef.child("Request").child("friendsRequestReceived").child(curUser.getNickname()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Pravilnii sposob mi polu4aem vse objecti User iz chlid users
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    User someUser = postSnapshot.getValue(User.class);
                    myRequestReceived.add(someUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    //Check if user exist
    private boolean checkIfUserExist(String searchStr){
        for (User user : usersList){
            if (user.getEmail().equals(searchStr) || user.getNickname().equals(searchStr)){
                return true;
            }
        }
        return false;
    }
    private void downloadProfilePhotoIntoImgView(final String nickname){
        StorageReference storR = storageRef.child("ProfilePhoto/" + nickname +"/profile");//ne nujno pisat jpg ili png
        try {
            final File localFile = File.createTempFile("images","jpg");
            storR.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    userAccountIcon.setImageBitmap(bitmap);
                    profileBitmap = bitmap;
                    loading.setVisibility(View.INVISIBLE);
                    searchResLinearLayout.setVisibility(View.VISIBLE);
                    //loading end
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Check if Friend
    private boolean checkIfFriend(String searchStr){
        for (User user : friendsList){
            if (user.getNickname().equals(searchStr) || user.getEmail().equals(searchStr))
                return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }
}