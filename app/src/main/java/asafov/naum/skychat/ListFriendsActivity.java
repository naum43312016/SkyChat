package asafov.naum.skychat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import asafov.naum.skychat.chat.Chat;
import asafov.naum.skychat.chat.Message;
import asafov.naum.skychat.services.MessageNotificationService;
import asafov.naum.skychat.services.RequestNotificationService;

public class ListFriendsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference myRef;
    List<User> friendsList;
    FriendsListAdapter adapter;
    RecyclerView recyclerView;
    TextView txtIfRecIsEmpty;
    ProgressBar loading;
    LinearLayout layoutFriends;
    LinearLayout layoutRequest;

    RecyclerView recRequestList;//rec for request list
    RequestReceivedListAdapter requestReceivedAdapter;//adapter for request list
    List<User> requestReceivedUserList;//for request list
    public static User curUser;//for request list
    DatabaseReference requestListRef;//for request list
    public static List<Chat> chatList;
    public static boolean MesNotFlag;
    public static boolean ReqNotFlag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_friends);
        loading = (ProgressBar) findViewById(R.id.loadingIconFriendsList);
        //Знак загрузки
        loading.setVisibility(View.VISIBLE);
        layoutFriends = (LinearLayout)findViewById(R.id.layoutFriends);
        layoutRequest = (LinearLayout)findViewById(R.id.layoutRequest);

        //Log.d("MYTAG","ListFr On Start");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        friendsList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recViewFriendsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        //rec for received request start
        recRequestList = (RecyclerView) findViewById(R.id.recViewRequestReceivedList);
        recRequestList.setLayoutManager(new LinearLayoutManager(this));
        recRequestList.setHasFixedSize(true);
        chatList = new ArrayList<>();
        //rec for received request end
        txtIfRecIsEmpty = (TextView) findViewById(R.id.txtIfRecIsEmpty);
        setupTabLayout();//Set up tabs
        //adapter = new FriendsListAdapter(friendsList,chatList,getApplicationContext());
        adapter = new FriendsListAdapter(friendsList,getApplicationContext());
        recyclerView.setAdapter(adapter);
        if (ChatRoomActivity.currentChat!=null){
            ChatRoomActivity.currentChat = "";
        }
        initCurUser();
        loading.setVisibility(View.VISIBLE);
        getAllChats();
        //Request list start
        requestReceivedUserList = new ArrayList<>();
        requestReceivedAdapter = new RequestReceivedListAdapter(requestReceivedUserList);
        recRequestList.setAdapter(requestReceivedAdapter);
        //Request list end


    }


    private void getAllChats(){
        FirebaseDatabase.getInstance().getReference().child("Chats").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                chatList.add(dataSnapshot.getValue(Chat.class));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                int index = getChatIndex(chat);
                chatList.set(index,chat);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                int index = getChatIndex(chat);
                if (index != -1){
                    chatList.remove(index);
                    //adapter.notifyItemRemoved(index);
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

    //Получает данные из базы и следит за изменениями
    private void updateList(){
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                friendsList.add(dataSnapshot.getValue(User.class));
                adapter.notifyDataSetChanged();
                loading.setVisibility(View.INVISIBLE);
                checkIfEmpty();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                User user = dataSnapshot.getValue(User.class);
                int index = getItemIndex(user);
                friendsList.set(index,user);
                adapter.notifyItemChanged(index);
                checkIfEmpty();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                int index = getItemIndex(user);
                if (index != -1){
                    friendsList.remove(index);
                    adapter.notifyItemRemoved(index);
                }
                checkIfEmpty();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkIfEmpty(){
        adapter.notifyDataSetChanged();
        if (friendsList.size() == 0){
            recyclerView.setVisibility(View.INVISIBLE);
            txtIfRecIsEmpty.setVisibility(View.VISIBLE);
            loading.setVisibility(View.INVISIBLE);
        }else {
            recyclerView.setVisibility(View.VISIBLE);
            txtIfRecIsEmpty.setVisibility(View.INVISIBLE);
            loading.setVisibility(View.INVISIBLE);
        }
    }


    //Получить позицию пользователя
    private int getItemIndex(User user){
        int index = -1;
        if (user!=null){
            for (int i = 0; i < friendsList.size(); i++){
                if (friendsList.get(i).getUserId().equals(user.getUserId())){
                    index = i;
                    break;
                }
            }
        }
        return index;
    }
    //Получить позицию пользователя
    public static int getChatIndex(Chat chat){
        int index = -1;
        if (chat!=null){
            for (int i = 0; i < chatList.size(); i++){
                if (chatList.get(i).getChatId().equals(chatList.get(i).getChatId())){
                    index = i;
                    break;
                }
            }
        }
        return index;
    }
    //Polzovate v requestListe
    private int getItemIndexReq(User user){
        int index = -1;
        if (user!=null){
            for (int i = 0; i < requestReceivedUserList.size(); i++){
                if (requestReceivedUserList.get(i).getUserId().equals(user.getUserId())){
                    index = i;
                    break;
                }
            }
        }
        return index;
    }


    //Tabs
    private void setupTabLayout() {
        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabLayoutActListFr);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                onTabTapped(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void onTabTapped(int position) {
        switch (position) {
            case 0:
                checkIfEmpty();
                layoutFriends.setVisibility(View.VISIBLE);
                layoutRequest.setVisibility(View.INVISIBLE);
                break;
            case 1:
                txtIfRecIsEmpty.setVisibility(View.INVISIBLE);
                layoutFriends.setVisibility(View.INVISIBLE);
                layoutRequest.setVisibility(View.VISIBLE);
                break;
        }
    }


    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Najatie na item v menu
        switch (item.getItemId()) {
            //Knopka vihoda
            case R.id.UserSignOut:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ListFriendsActivity.this, MainActivity.class));
                return true;
            case R.id.searchFriendPage:
                //friendsList;
                Intent intent = new Intent(ListFriendsActivity.this,AddFriendActivity.class);
                ArrayList<String> array = new ArrayList<>();
                intent.putExtra("list", (Serializable) friendsList);
                startActivity(intent);
                return true;
            case R.id.accountInfo:
                startActivity(new Intent(ListFriendsActivity.this, AccountInfoActivity.class));
                return true;
            case R.id.requestSent:
                startActivity(new Intent(ListFriendsActivity.this, RequestSentActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //REQUEST RECEIVED START
    private void initCurUser(){
        //Get Current user
        FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                curUser = dataSnapshot.getValue(User.class);
                myRef = FirebaseDatabase.getInstance().getReference("Friends").child(curUser.getNickname());
                updateList();
                testCheckFriends();
                //checkIfEmpty();

                //Log.d("MYTAG","ListFr Init Cur User");

                requestListRef = FirebaseDatabase.getInstance().getReference();
                requestListRef = requestListRef.child("Request").child("friendsRequestReceived").child(curUser.getNickname());
                updateRequestReceivedList();
                //Service
                if (!MesNotFlag) {
                    startService(new Intent(ListFriendsActivity.this, MessageNotificationService.class));
                }
                //Service
                if (!ReqNotFlag) {
                    startService(new Intent(ListFriendsActivity.this, RequestNotificationService.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    //Получает данные из базы и следит за изменениями
    private void updateRequestReceivedList(){
        requestListRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                requestReceivedUserList.add(dataSnapshot.getValue(User.class));
                requestReceivedAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                User user = dataSnapshot.getValue(User.class);
                int index = getItemIndexReq(user);
                requestReceivedUserList.set(index,user);
                requestReceivedAdapter.notifyItemChanged(index);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                int index = getItemIndexReq(user);
                if (dataSnapshot != null){
                    if (index != -1 && index < requestReceivedUserList.size()){
                        requestReceivedUserList.remove(index);
                        requestReceivedAdapter.notifyItemRemoved(index);
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
    //REQUEST RECEIVED END


    private void testCheckFriends(){
        FirebaseDatabase.getInstance().getReference().child("Friedns").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(curUser.getNickname())){
                    checkIfEmpty();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onResume() {
        //Service
        if (!MesNotFlag) {
            startService(new Intent(ListFriendsActivity.this, MessageNotificationService.class));
        }
        //Service
        if (!ReqNotFlag) {
            startService(new Intent(ListFriendsActivity.this, RequestNotificationService.class));
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        //Log.d("MYTAG","ListFr On Stop");
        super.onStop();
    }
}
