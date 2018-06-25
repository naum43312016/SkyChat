package asafov.naum.skychat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;

import asafov.naum.skychat.functions.Functions;

/**
 * Created by user on 06/04/2018.
 */

public class RequestSentAdapter extends RecyclerView.Adapter<RequestSentAdapter.ViewHolder>{

    List<User> userList;
    Context context;
    DatabaseReference myRef;

    public RequestSentAdapter(List<User> userList,Context context) {
        this.userList = userList;
        this.context = context;
        myRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RequestSentAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request_sent_recview,parent,false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final User user = userList.get(position);
        holder.nickName.setText(user.getNickname());
        holder.email.setText(user.getEmail());

        //Get profile image
        String path = context.getFilesDir().getPath() + File.separator + "SkyChatImage" + File.separator + "friends_profile_images" + File.separator + user.getNickname() + File.separator + "profile.jpg";
        Functions.setProfilePuctureInImgView(path,holder.profileImage);
        //Listener for cancel button
        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Delete request
                myRef.child("Request").child("friendsRequestSent").child(ListFriendsActivity.curUser.getNickname())
                        .child(user.getNickname()).setValue(null);
                myRef.child("Request").child("friendsRequestReceived").child(user.getNickname()).child(ListFriendsActivity.curUser.getNickname()).setValue(null);
                Toast.makeText(context,"Request was canceled!",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView nickName;
        TextView email;
        Button btnCancel;
        public ViewHolder(View itemView) {
            super(itemView);
            profileImage = (ImageView) itemView.findViewById(R.id.userAccountIconListRequestSent);
            nickName = (TextView) itemView.findViewById(R.id.txtUserNickNameRequestSent);
            email = (TextView) itemView.findViewById(R.id.txtUserEmailRequestSent);
            btnCancel = (Button) itemView.findViewById(R.id.btnCancelFriendRequestSent);
        }
    }
}
