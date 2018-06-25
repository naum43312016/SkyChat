package asafov.naum.skychat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import asafov.naum.skychat.chat.Chat;
import asafov.naum.skychat.functions.Functions;

/**
 * Created by user on 02/04/2018.
 */

public class RequestReceivedListAdapter extends RecyclerView.Adapter<RequestReceivedListAdapter.ViewHolder>{


    List<User> userList;
    Context context;
    DatabaseReference myRef;
    public RequestReceivedListAdapter(List<User> userList) {
        this.userList = userList;
        myRef = FirebaseDatabase.getInstance().getReference();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new RequestReceivedListAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request_received_item,parent,false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final User user = userList.get(position);
        final Bitmap[] image = new Bitmap[1];
        holder.userNickname.setText(user.getNickname());
        holder.userEmail.setText(user.getEmail());
        //User photo
        StorageReference storR = FirebaseStorage.getInstance().getReference().child("ProfilePhoto/" + user.getNickname() +"/profile");
        try {
            final File localFile = File.createTempFile("images","jpg");
            storR.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    holder.userProfilePhoto.setImageBitmap(bitmap);
                    image[0] = bitmap;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*String f = context.getFilesDir().getPath() + File.separator + "SkyChatImage" + File.separator + "profile_images" + File.separator + user.getNickname() + File.separator + "profile.jpg";
        final Bitmap image = Functions.getProfilePuctureInImgView(f);
        holder.userProfilePhoto.setImageBitmap(image);*/
        //Zdes onAddBtnClick posle najataie dobavlyat v dryzia y dvoix polzovatelei
        //i ydalit request iz db
        //Add button listener
        holder.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Добавление в друзья
                myRef.child("Friends").child(user.getNickname()).child(ListFriendsActivity.curUser.getNickname()).setValue(ListFriendsActivity.curUser);
                myRef.child("Friends").child(ListFriendsActivity.curUser.getNickname()).child(user.getNickname()).setValue(user);
                //Добавление фото профиля нового друга
                //Сохранение изображение профиля друга в папке в памяти пользователя
                String f = context.getFilesDir().getPath() + File.separator + "SkyChatImage" + File.separator + "friends_profile_images" + File.separator + user.getNickname();
                Functions.createFolder(f);//Create fodler
                File file = new File(context.getFilesDir().getPath() + File.separator + "SkyChatImage" + File.separator + "friends_profile_images" + File.separator + user.getNickname() + File.separator + "profile.jpg");
                Functions.createBitmapInFolder(file,image[0]);
                Chat chat = Functions.createChat(ListFriendsActivity.curUser,user);
                myRef.child("Chats").child(chat.getChatId()).setValue(chat);
                //Delete request
                myRef.child("Request").child("friendsRequestReceived").child(ListFriendsActivity.curUser.getNickname())
                        .child(user.getNickname()).setValue(null);
                myRef.child("Request").child("friendsRequestSent").child(user.getNickname()).child(ListFriendsActivity.curUser.getNickname()).setValue(null);
                Toast.makeText(context,user.getNickname() + " was added",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userProfilePhoto;
        TextView userNickname;
        TextView userEmail;
        Button addBtn;
        public ViewHolder(View itemView) {
            super(itemView);
            userProfilePhoto = (ImageView) itemView.findViewById(R.id.userAccountIconListRequestReceived);
            userNickname = (TextView) itemView.findViewById(R.id.txtUserNickNameRequestReceived);
            userEmail = (TextView) itemView.findViewById(R.id.txtUserEmailRequestReceived);
            addBtn = (Button) itemView.findViewById(R.id.btnAddFriendRequestReceived);
        }
    }
}
