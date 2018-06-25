package asafov.naum.skychat;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import asafov.naum.skychat.chat.AudioMessagePlayer;
import asafov.naum.skychat.chat.Message;
import asafov.naum.skychat.chat.Tags;
import asafov.naum.skychat.functions.Functions;

/**
 * Created by user on 17/04/2018.
 */

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ViewHolder>{

    List<Message> messageList;
    Activity activity;
    MediaPlayer mediaPlayer;
    Handler seekbarUpdateHandler;
    Runnable updateSeekbar;


    public ChatRoomAdapter(List<Message> messageList, Activity activity){
        this.messageList = messageList;
        this.activity = activity;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ChatRoomAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recview_chatroom,parent,false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Message message = messageList.get(position);
        setMessageTime(holder,message);//Set message time.
        //Log.d("TESTNAUM",message.getMessage());
        if (message.getTag().equals("message")) {
            if (message.getSenderUser().getNickname().equals(ListFriendsActivity.curUser.getNickname())) {
                holder.messageText.setBackgroundResource(R.drawable.chatroom_message_textview_send);
                holder.loadingIconChatAudio.setVisibility(View.INVISIBLE);
                RelativeLayout.LayoutParams params;
                params = (RelativeLayout.LayoutParams)holder.layoutMessage.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                /////////
                holder.layoutMessageImage.setVisibility(View.GONE);
                holder.layoutMessage.setLayoutParams(params);
                holder.layoutMessage.setVisibility(View.VISIBLE);
                holder.layoutMessage.setGravity(Gravity.RIGHT);
                holder.messageLayout.setGravity(Gravity.END);
                holder.imgStrelkaSend.setVisibility(View.VISIBLE);
                holder.layoutBtnPlayAudio.setVisibility(View.INVISIBLE);
                holder.imgStrelkaGet.setVisibility(View.INVISIBLE);
            } else {//dobavit readed v kajdoe soobwenie
                //ChatRoomActivity.recyclerView.smoothScrollToPosition(ChatRoomActivity.recyclerView.getAdapter().getItemCount());//Spuskaet rec view v konec poslee novogo soobweniya
                holder.loadingIconChatAudio.setVisibility(View.INVISIBLE);

                /////////
                holder.layoutMessageImage.setVisibility(View.GONE);
                holder.messageText.setBackgroundResource(R.drawable.chatroom_message_textview_get);
                holder.messageLayout.setGravity(Gravity.LEFT);
                RelativeLayout.LayoutParams params;
                params = (RelativeLayout.LayoutParams)holder.layoutMessage.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                holder.layoutMessage.setLayoutParams(params);
                holder.layoutMessage.setGravity(Gravity.LEFT);
                holder.layoutMessage.setVisibility(View.VISIBLE);
                holder.layoutBtnPlayAudio.setVisibility(View.INVISIBLE);
                holder.imgStrelkaSend.setVisibility(View.INVISIBLE);
                holder.imgStrelkaGet.setVisibility(View.VISIBLE);
            }
            holder.messageText.setText(message.getMessage());
        }
        //Audio message
        if (message.getTag().equals(Tags.TAG_AUDIO_MESSAGE)){
            if (message.getSenderUser().getNickname().equals(ListFriendsActivity.curUser.getNickname())){
                final String filePath = ChatRoomActivity.path + File.separator + message.getFileName();
                holder.loadingIconChatAudio.setVisibility(View.INVISIBLE);
                holder.layoutMessage.setVisibility(View.INVISIBLE);
                holder.layoutBtnPlayAudio.setVisibility(View.VISIBLE);
                /////////
                holder.layoutMessageImage.setVisibility(View.GONE);
                holder.messageLayout.setGravity(Gravity.END);
                holder.imgAudioStrelkaSend.setVisibility(View.VISIBLE);
                holder.imgAudioStrelkaGet.setVisibility(View.INVISIBLE);
                holder.btnPlayAudio.setVisibility(View.VISIBLE);
                holder.btnPauseAudio.setVisibility(View.INVISIBLE);
                holder.btnPlayAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.btnPlayAudio.setVisibility(View.INVISIBLE);
                        holder.btnPauseAudio.setVisibility(View.VISIBLE);
                        try {
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setDataSource(filePath);
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }catch (IllegalStateException e){
                            e.printStackTrace();
                        }
                        mediaPlayerSeekBar(holder);
                        seekbarUpdateHandler.postDelayed(updateSeekbar, 0);
                    }
                });
                holder.btnPauseAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mediaPlayer!=null){
                            try {
                                mediaPlayer.stop();
                                mediaPlayer.release();
                            }catch (IllegalStateException e){
                                e.printStackTrace();
                            }
                            seekbarUpdateHandler.removeCallbacks(updateSeekbar);
                        }
                        holder.btnPlayAudio.setVisibility(View.VISIBLE);
                        holder.btnPauseAudio.setVisibility(View.INVISIBLE);
                    }
                });
            }else {
                //ChatRoomActivity.recyclerView.smoothScrollToPosition(ChatRoomActivity.recyclerView.getAdapter().getItemCount()-1);//Spuskaet rec view v konec poslee novogo soobweniya
                final String audioFilePath = ChatRoomActivity.path + File.separator + message.getFileName();
                File audioFileOnDevice = new File(audioFilePath);
                if (audioFileOnDevice.exists() && !audioFileOnDevice.isDirectory()){
                    holder.loadingIconChatAudio.setVisibility(View.INVISIBLE);
                    holder.layoutMessage.setVisibility(View.INVISIBLE);
                    /////////
                    holder.layoutMessageImage.setVisibility(View.GONE);
                    RelativeLayout.LayoutParams params;
                    params = (RelativeLayout.LayoutParams)holder.layoutBtnPlayAudio.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    holder.layoutBtnPlayAudio.setLayoutParams(params);
                    holder.layoutBtnPlayAudio.setVisibility(View.VISIBLE);
                    holder.messageLayout.setGravity(Gravity.LEFT);
                    holder.btnPlayAudio.setVisibility(View.VISIBLE);
                    holder.imgAudioStrelkaSend.setVisibility(View.INVISIBLE);
                    holder.imgAudioStrelkaGet.setVisibility(View.VISIBLE);
                    holder.btnPauseAudio.setVisibility(View.INVISIBLE);
                    holder.btnPlayAudio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            holder.btnPlayAudio.setVisibility(View.INVISIBLE);
                            holder.btnPauseAudio.setVisibility(View.VISIBLE);
                            try {
                                mediaPlayer = new MediaPlayer();
                                mediaPlayer.setDataSource(audioFilePath);
                                mediaPlayer.prepare();
                                mediaPlayer.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }catch (IllegalStateException e){
                                e.printStackTrace();
                            }
                            mediaPlayerSeekBar(holder);
                            seekbarUpdateHandler.postDelayed(updateSeekbar, 0);
                        }
                    });
                    holder.btnPauseAudio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (mediaPlayer!=null){
                                try {
                                    mediaPlayer.stop();
                                    mediaPlayer.release();
                                }catch (IllegalStateException e){
                                    e.printStackTrace();
                                }
                                seekbarUpdateHandler.removeCallbacks(updateSeekbar);
                            }
                            holder.btnPlayAudio.setVisibility(View.VISIBLE);
                            holder.btnPauseAudio.setVisibility(View.INVISIBLE);
                        }
                    });
                }else {
                    holder.loadingIconChatAudio.setVisibility(View.VISIBLE);
                    holder.layoutMessage.setVisibility(View.INVISIBLE);
                    /////////
                    holder.layoutMessageImage.setVisibility(View.GONE);
                    final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("AudioMessage").child(ChatRoomActivity.chat.getChatId()).child(message.getFileName());
                    File rootPath = new File(ChatRoomActivity.path);
                    if(!rootPath.exists()) {
                        rootPath.mkdirs();
                    }
                    final File localFile = new File(rootPath,message.getFileName());
                        filepath.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                //ChatRoomActivity.recyclerView.smoothScrollToPosition(ChatRoomActivity.recyclerView.getAdapter().getItemCount());//Spuskaet rec view v konec poslee novogo soobweniya//Scrool item
                                holder.loadingIconChatAudio.setVisibility(View.INVISIBLE);
                                holder.layoutMessage.setVisibility(View.INVISIBLE);
                                /////////
                                holder.layoutMessageImage.setVisibility(View.GONE);
                                RelativeLayout.LayoutParams params;
                                params = (RelativeLayout.LayoutParams)holder.layoutBtnPlayAudio.getLayoutParams();
                                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                                holder.layoutBtnPlayAudio.setLayoutParams(params);
                                holder.layoutBtnPlayAudio.setVisibility(View.VISIBLE);
                                holder.messageLayout.setGravity(Gravity.LEFT);
                                holder.imgAudioStrelkaSend.setVisibility(View.INVISIBLE);
                                holder.imgAudioStrelkaGet.setVisibility(View.VISIBLE);
                                holder.btnPlayAudio.setVisibility(View.VISIBLE);
                                holder.btnPauseAudio.setVisibility(View.INVISIBLE);
                                holder.btnPlayAudio.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        holder.btnPlayAudio.setVisibility(View.INVISIBLE);
                                        holder.btnPauseAudio.setVisibility(View.VISIBLE);
                                        try {
                                            mediaPlayer = new MediaPlayer();
                                            mediaPlayer.setDataSource(audioFilePath);
                                            mediaPlayer.prepare();
                                            mediaPlayer.start();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }catch (IllegalStateException e){
                                            e.printStackTrace();
                                        }
                                        mediaPlayerSeekBar(holder);
                                        seekbarUpdateHandler.postDelayed(updateSeekbar, 0);
                                    }
                                });
                                holder.btnPauseAudio.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (mediaPlayer!=null){
                                            try {
                                                mediaPlayer.stop();
                                                mediaPlayer.release();
                                            }catch (IllegalStateException e){
                                                e.printStackTrace();
                                            }
                                            seekbarUpdateHandler.removeCallbacks(updateSeekbar);
                                        }
                                        holder.btnPlayAudio.setVisibility(View.VISIBLE);
                                        holder.btnPauseAudio.setVisibility(View.INVISIBLE);
                                    }
                                });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    ChatRoomActivity.recyclerView.getAdapter().notifyItemChanged(ChatRoomActivity.recyclerView.getAdapter().getItemCount()-1);
                                }
                            });


                }
            }
        }
        //Message-img file
        if (message.getTag().equals(Tags.TAG_IMAGE_SEND)) {
            if (message.getSenderUser().getNickname().equals(ListFriendsActivity.curUser.getNickname())) {
                //Get Image and change size
                Bitmap image = decodeSampledBitmapFromResource(ChatRoomActivity.IMAGE_SEND_PATH + message.getFileName() + ".jpg",450,450);
                RelativeLayout.LayoutParams params;
                params = (RelativeLayout.LayoutParams)holder.layoutMessageImage.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                holder.layoutMessage.setVisibility(View.INVISIBLE);
                holder.layoutMessageImage.setLayoutParams(params);
                holder.layoutMessageImage.setVisibility(View.VISIBLE);
                holder.messageLayout.setGravity(Gravity.END);
                holder.messageTimeImage.setVisibility(View.VISIBLE);
                holder.imgStrelkaSendImage.setVisibility(View.VISIBLE);
                holder.imgStrelkaGetImage.setVisibility(View.INVISIBLE);
                holder.imageViewMessage.setImageBitmap(image);
                holder.imageViewMessage.setVisibility(View.VISIBLE);
            } else {
                //ChatRoomActivity.recyclerView.smoothScrollToPosition(ChatRoomActivity.recyclerView.getAdapter().getItemCount()-1);//Spuskaet rec view v konec poslee novogo soobweniya
                final String imageFilePath = ChatRoomActivity.IMAGE_GET_PATH + File.separator + message.getFileName() + ".jpg";
                File imageFileOnDevice = new File(imageFilePath);
                if (imageFileOnDevice.exists() && !imageFileOnDevice.isDirectory()){
                    //Get Image and change size
                    Bitmap originalImage = decodeSampledBitmapFromResource(imageFilePath,450,450);
                    Bitmap image = rotateImage(originalImage,imageFilePath);
                    RelativeLayout.LayoutParams params;
                    params = (RelativeLayout.LayoutParams)holder.layoutMessageImage.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    holder.layoutMessageImage.setLayoutParams(params);
                    holder.layoutMessageImage.setVisibility(View.VISIBLE);
                    holder.layoutMessage.setVisibility(View.INVISIBLE);
                    holder.messageLayout.setGravity(Gravity.START);
                    holder.messageTimeImage.setVisibility(View.VISIBLE);
                    holder.imgStrelkaSendImage.setVisibility(View.INVISIBLE);
                    holder.imgStrelkaGetImage.setVisibility(View.VISIBLE);
                    holder.imageViewMessage.setImageBitmap(image);
                    holder.imageViewMessage.setVisibility(View.VISIBLE);
                }else {
                    holder.loadingIconChatAudio.setVisibility(View.VISIBLE);
                    holder.layoutMessage.setVisibility(View.INVISIBLE);
                    final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("ChatImage").child(ChatRoomActivity.chat.getChatId()).child(message.getFileName());
                    File rootPath = new File(ChatRoomActivity.IMAGE_GET_PATH);
                    if(!rootPath.exists()) {
                        rootPath.mkdirs();
                    }
                    final String fileName = message.getFileName();
                    final File localFile = new File(rootPath,message.getFileName() + ".jpg");
                    filepath.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        }
                    }).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                            holder.loadingIconChatAudio.setVisibility(View.INVISIBLE);
                            //Get Image and change size
                            Bitmap originalImage = decodeSampledBitmapFromResource(ChatRoomActivity.IMAGE_GET_PATH + fileName + ".jpg",450,450);
                            Bitmap image = rotateImage(originalImage,imageFilePath);
                            RelativeLayout.LayoutParams params;
                            params = (RelativeLayout.LayoutParams)holder.layoutMessageImage.getLayoutParams();
                            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                            holder.layoutMessageImage.setLayoutParams(params);
                            holder.layoutMessageImage.setVisibility(View.VISIBLE);
                            holder.messageLayout.setGravity(Gravity.START);
                            holder.messageTimeImage.setVisibility(View.VISIBLE);
                            holder.imgStrelkaSendImage.setVisibility(View.INVISIBLE);
                            holder.imgStrelkaGetImage.setVisibility(View.VISIBLE);
                            holder.imageViewMessage.setImageBitmap(image);
                            holder.imageViewMessage.setVisibility(View.VISIBLE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            holder.loadingIconChatAudio.setVisibility(View.INVISIBLE);
                            Toast.makeText(activity,"Can not get image,check your internet connection!",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }

    private void mediaPlayerSeekBar(final ViewHolder holder){
        if (mediaPlayer!=null){
            holder.audioMessageSeekBar.setMax(mediaPlayer.getDuration());
        }
        seekbarUpdateHandler = new Handler();
        updateSeekbar = new Runnable() {
            @Override
            public void run() {
                holder.audioMessageSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                if (holder.audioMessageSeekBar.getProgress() == mediaPlayer.getDuration()){
                    seekbarUpdateHandler.removeCallbacks(updateSeekbar);
                    holder.btnPlayAudio.setVisibility(View.VISIBLE);
                    holder.btnPauseAudio.setVisibility(View.INVISIBLE);
                    holder.audioMessageSeekBar.setProgress(0);
                    try {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    }catch (IllegalStateException e){
                        e.printStackTrace();
                    }
                }else {
                    seekbarUpdateHandler.postDelayed(this, 50);
                }
            }
        };


        holder.audioMessageSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b){
                    if(mediaPlayer!= null && holder.btnPauseAudio.getVisibility() == View.VISIBLE){
                        mediaPlayer.seekTo(i);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageView imgStrelkaGet;
        ImageView imgStrelkaSend;
        ImageView imgAudioStrelkaGet;
        ImageView imgAudioStrelkaSend;
        RelativeLayout messageLayout;
        LinearLayout layoutBtnPlayAudio;
        LinearLayout layoutMessage;
        ImageView btnPlayAudio;
        ImageView btnPauseAudio;
        ProgressBar loadingIconChatAudio;
        SeekBar audioMessageSeekBar;
        TextView messageTime;
        TextView audioMessageTime;

        //Image send
        LinearLayout layoutMessageImage;
        ImageView imgStrelkaGetImage;
        ImageView imageViewMessage;
        TextView messageTimeImage;
        ImageView imgStrelkaSendImage;
        public ViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.messageTextView);
            imgStrelkaGet = (ImageView) itemView.findViewById(R.id.imgStrelkaGet);
            imgAudioStrelkaGet = (ImageView) itemView.findViewById(R.id.imgAudioStrelkaGet);
            imgAudioStrelkaSend = (ImageView) itemView.findViewById(R.id.imgAudioStrelkaSend);
            imgStrelkaSend = (ImageView) itemView.findViewById(R.id.imgStrelkaSend);
            messageLayout = (RelativeLayout) itemView.findViewById(R.id.messageLayoutItemRecView);
            layoutBtnPlayAudio = (LinearLayout) itemView.findViewById(R.id.layoutBtnPlayAudio);
            layoutMessage = (LinearLayout) itemView.findViewById(R.id.layoutMessage);
            btnPlayAudio = (ImageView) itemView.findViewById(R.id.btnPlayAudio);
            btnPauseAudio = (ImageView) itemView.findViewById(R.id.btnPauseAudio);
            loadingIconChatAudio = (ProgressBar) itemView.findViewById(R.id.loadingIconChatAudio);
            audioMessageSeekBar = (SeekBar) itemView.findViewById(R.id.mediaPlayerAudioMessageSeekBar);
            messageTime = (TextView) itemView.findViewById(R.id.messageTime);
            audioMessageTime = (TextView) itemView.findViewById(R.id.audioMessageTime);
            //Image send
            layoutMessageImage = (LinearLayout) itemView.findViewById(R.id.layoutMessageImage);
            imgStrelkaGetImage = (ImageView) itemView.findViewById(R.id.imgStrelkaGetImage);
            imageViewMessage = (ImageView) itemView.findViewById(R.id.imageViewMessage);
            messageTimeImage = (TextView) itemView.findViewById(R.id.messageTimeImage);
            imgStrelkaSendImage = (ImageView) itemView.findViewById(R.id.imgStrelkaSendImage);
        }
    }

    private void setMessageTime(ViewHolder holder,Message message){
        if (message!=null){
            if (message.getTag().equals(Tags.TAG_MESSAGE)){
                holder.messageTime.setText(message.getMessageTime().get("hour").toString() + ":" + message.getMessageTime().get("minute").toString());
            }else if (message.getTag().equals(Tags.TAG_AUDIO_MESSAGE)){
                holder.audioMessageTime.setText(message.getMessageTime().get("hour").toString() + ":" + message.getMessageTime().get("minute").toString());
            }else if (message.getTag().equals(Tags.TAG_IMAGE_SEND)){
                holder.messageTimeImage.setText(message.getMessageTime().get("hour").toString() + ":" + message.getMessageTime().get("minute").toString());
            }
        }
    }

    private Bitmap rotateImage(Bitmap image,String imageFilePath){
        try {
            ExifInterface exif = new ExifInterface(imageFilePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }
            image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true); // rotating bitmap
        }
        catch (Exception e) {

        }
        return image;
    }


    public static Bitmap decodeSampledBitmapFromResource(String path,
                                                         int reqWidth, int reqHeight) {

        // Читаем с inJustDecodeBounds=true для определения размеров
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Вычисляем inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Читаем с использованием inSampleSize коэффициента
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Реальные размеры изображения
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Вычисляем наибольший inSampleSize, который будет кратным двум
            // и оставит полученные размеры больше, чем требуемые
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
