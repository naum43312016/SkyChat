package asafov.naum.skychat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ImagesSenderFromGalleryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference myRef;
    List<User> listFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images_sender_from_gallery);
        if (ListFriendsActivity.curUser == null){
            Toast.makeText(ImagesSenderFromGalleryActivity.this,"Connect to your account",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ImagesSenderFromGalleryActivity.this,ListFriendsActivity.class));
        }
        listFriends = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recViewSendImages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        myRef = FirebaseDatabase.getInstance().getReference();

        ///Dobavit chati i pri najatie otkrit chat i otpravit photo
        /*if (Intent.ACTION_SEND.equals(action) && type != null) {
        if ("text/plain".equals(type)) {
            handleSendText(intent); // Handle text being sent
        } else if (type.startsWith("image/")) {
            handleSendImage(intent); // Handle single image being sent
        }*/
        /*Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
         if (imageUri != null) {
            // Update UI to reflect image being shared
         }*/
    }
}
