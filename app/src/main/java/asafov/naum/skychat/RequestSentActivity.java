package asafov.naum.skychat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class RequestSentActivity extends AppCompatActivity {

    RequestSentAdapter adapter;
    RecyclerView requestSentRecView;
    User currentUser;
    DatabaseReference requestListRef;
    List<User> requestSentList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_sent);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//Кнопка назад
        requestSentRecView = (RecyclerView) findViewById(R.id.recViewRequestSent);
        requestSentRecView.setLayoutManager(new LinearLayoutManager(this));
        requestSentRecView.setHasFixedSize(true);
        requestSentList = new ArrayList<>();
        adapter = new RequestSentAdapter(requestSentList,getApplicationContext());
        requestSentRecView.setAdapter(adapter);

        requestListRef = FirebaseDatabase.getInstance().getReference();
        requestListRef = requestListRef.child("Request").child("friendsRequestSent").child(ListFriendsActivity.curUser.getNickname());
        updateRequestReceivedList();

    }

    //Получает данные из базы и следит за изменениями
    private void updateRequestReceivedList(){
        requestListRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                requestSentList.add(dataSnapshot.getValue(User.class));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                User user = dataSnapshot.getValue(User.class);
                int index = getItemIndexReq(user);
                requestSentList.set(index,user);
                adapter.notifyItemChanged(index);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                int index = getItemIndexReq(user);
                if (dataSnapshot != null){
                    if (index != -1 && index < requestSentList.size()){
                        requestSentList.remove(index);
                        adapter.notifyItemRemoved(index);
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

    private int getItemIndexReq(User user){
        int index = -1;
        if (user!=null){
            for (int i = 0; i < requestSentList.size(); i++){
                if (requestSentList.get(i).getUserId().equals(user.getUserId())){
                    index = i;
                    break;
                }
            }
        }
        return index;
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
