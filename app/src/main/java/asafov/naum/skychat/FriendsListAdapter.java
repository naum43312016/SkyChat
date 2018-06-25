package asafov.naum.skychat;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import asafov.naum.skychat.chat.Chat;
import asafov.naum.skychat.chat.Message;
import asafov.naum.skychat.chat.Tags;
import asafov.naum.skychat.chat.Time;
import asafov.naum.skychat.functions.Functions;

/**
 * Created by user on 23/03/2018.
 */

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.ViewHolder>{

    List<User> userList;
    Context context;


    public FriendsListAdapter(List<User> userList,Context context) {
        this.userList = userList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friends_list,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final User user = userList.get(position);
        final Chat chat = getChat(user);

        if (chat!= null){
            //Get last message
            if (chat.getMessages()!=null){
                Message message = chat.getMessages().get(chat.getMessages().size() - 1);
                if (message != null) {
                    switch (message.getTag()) {
                        case Tags.TAG_MESSAGE:
                            viewHolder.txtUserMsg.setText(message.getMessage());
                            if (!message.isRead() && !message.getSenderUser().getNickname().equals(ListFriendsActivity.curUser.getNickname())){
                                    viewHolder.txtUserMsg.setTextColor(context.getResources().getColor(R.color.newMessages));
                                    viewHolder.userChatMsgNewMail.setVisibility(View.VISIBLE);
                            }else {
                                viewHolder.txtUserMsg.setTextColor(context.getResources().getColor(R.color.mTextColor));
                                viewHolder.userChatMsgNewMail.setVisibility(View.INVISIBLE);
                            }
                            break;
                        case Tags.TAG_AUDIO_MESSAGE:
                            viewHolder.txtUserMsg.setText("Audio");
                            if (!message.isRead() && !message.getSenderUser().getNickname().equals(ListFriendsActivity.curUser.getNickname())){
                                    viewHolder.txtUserMsg.setTextColor(context.getResources().getColor(R.color.newMessages));
                                    viewHolder.userChatMsgNewMail.setVisibility(View.VISIBLE);
                            }else {
                                viewHolder.txtUserMsg.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                                viewHolder.userChatMsgNewMail.setVisibility(View.INVISIBLE);
                            }
                            break;
                        case Tags.TAG_IMAGE_SEND:
                            viewHolder.txtUserMsg.setText("Image");
                            if (!message.isRead() && !message.getSenderUser().getNickname().equals(ListFriendsActivity.curUser.getNickname())){
                                    viewHolder.txtUserMsg.setTextColor(context.getResources().getColor(R.color.newMessages));
                                    viewHolder.userChatMsgNewMail.setVisibility(View.VISIBLE);
                            }else {
                                viewHolder.txtUserMsg.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                                viewHolder.userChatMsgNewMail.setVisibility(View.INVISIBLE);
                            }
                            break;
                        case Tags.TAG_FILE:
                            viewHolder.txtUserMsg.setText("File");
                            if (!message.isRead() && !message.getSenderUser().getNickname().equals(ListFriendsActivity.curUser.getNickname())){
                                    viewHolder.txtUserMsg.setTextColor(context.getResources().getColor(R.color.newMessages));
                                    viewHolder.userChatMsgNewMail.setVisibility(View.VISIBLE);
                            }else {
                                viewHolder.txtUserMsg.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                                viewHolder.userChatMsgNewMail.setVisibility(View.INVISIBLE);
                            }
                            break;
                    }
                }
                setMessageTime(viewHolder, message);
            }

            viewHolder.friendsLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context,ChatRoomActivity.class);
                    intent.putExtra("chat", (Serializable) chat);
                    intent.putExtra("user", (Serializable) user);
                    context.startActivity(intent);
                }
            });
        }
        viewHolder.txtUserName.setText(user.getNickname());
        String path = context.getFilesDir().getPath() + File.separator + "SkyChatImage" + File.separator + "friends_profile_images" + File.separator + user.getNickname() + File.separator + "profile.jpg";
        Functions.setProfilePuctureInImgView(path,viewHolder.userIcon);
    }
    private void setMessageTime(ViewHolder viewHolder,Message message){
        Date date = new Date();
        String day = Time.FORMAT_DAY.format(date);
        if (message.getMessageTime().get("day").equals(day)){
            viewHolder.txtUserMsgTime.setText(message.getMessageTime().get("hour") + ":" + message.getMessageTime().get("minute"));
        }else {
            viewHolder.txtUserMsgTime.setText(message.getMessageTime().get("day") + "/" + message.getMessageTime().get("month") +
            "/" + message.getMessageTime().get("year"));
        }
    }
    private Chat getChat(User user){
        for (Chat chat : ListFriendsActivity.chatList){
            List<User> users = chat.getUsers();
            if ((users.get(0).getNickname().equals(user.getNickname()) ||
            users.get(0).getNickname().equals(ListFriendsActivity.curUser.getNickname()))
                    && (users.get(1).getNickname().equals(user.getNickname()) ||
                    users.get(1).getNickname().equals(ListFriendsActivity.curUser.getNickname()))){
                return chat;
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView userIcon;
        ImageView userChatMsgNewMail;
        TextView txtUserName;
        TextView txtUserMsg;
        TextView txtUserMsgTime;
        LinearLayout friendsLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            userIcon = itemView.findViewById(R.id.userAccountIconListItem);
            userChatMsgNewMail = itemView.findViewById(R.id.userChatMsgNewMail);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtUserMsg = itemView.findViewById(R.id.txtUserMsg);
            txtUserMsgTime = itemView.findViewById(R.id.txtUserMsgTime);
            friendsLayout = itemView.findViewById(R.id.item_friends_list_layout);
        }
    }
}
