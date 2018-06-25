package asafov.naum.skychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.UUID;

import asafov.naum.skychat.chat.Chat;

public class CameraActivity extends AppCompatActivity {

    final static String STORAGE_IMAGE_SEND_PATH = "";
    final static String BITMAP_PHOTO_CAMERA = "takenImage";
    ImageView photoTakeImgView;
    ImageView btnSendPhoto;
    Chat chat;
    User user;
    String photoName;
    String filePath;
    ProgressDialog dialog;
    Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//Кнопка назад
        photoTakeImgView = (ImageView) findViewById(R.id.photoTakeImgView);
        btnSendPhoto = (ImageView) findViewById(R.id.btnSendPhoto);
        dialog = new ProgressDialog(this);
        Intent intent = getIntent();
        filePath = intent.getStringExtra(BITMAP_PHOTO_CAMERA);
        chat = (Chat) intent.getSerializableExtra("chat");
        user = (User) intent.getSerializableExtra("user");
        photoName =  intent.getStringExtra("name");
        //Image from gallery
        if (intent.getStringExtra("imageuri")!=null){
            imageUri = Uri.parse(intent.getStringExtra("imageuri"));
            if (imageUri != null){
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap image = BitmapFactory.decodeStream(inputStream);
                    photoTakeImgView.setImageBitmap(image);
                    photoName = UUID.randomUUID().toString();
                    Log.d("NAMEPH",photoName);
                    //Save image in folder of app
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //Запись файла в папку
                            File file = new File(getFilesDir().getPath() + File.separator + "SkyChatImage" + File.separator +
                                    "cameraPictures" + File.separator +
                                    chat.getChatId() + File.separator + "send" + File.separator
                                    + photoName + ".jpg");
                            OutputStream out = null;
                            try {
                                out = new FileOutputStream(file);
                                image.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                out.flush();
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    btnSendPhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Можно так-же проверить интернет подключение и если всё ок,тогда сразу перенаправить в чатактивити и в потоке отправить фото в сторэж
                            dialog.setMessage("Sending");
                            dialog.show();
                            filePath = getFilesDir().getPath() + File.separator + "SkyChatImage" + File.separator +
                                    "cameraPictures" + File.separator +
                                    chat.getChatId() + File.separator + "send" + File.separator
                                    + photoName + ".jpg";
                            saveImageInStorage();
                        }
                    });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        //Camera image
        else {
            final Bitmap bitmap = rotateImage(filePath);
            photoTakeImgView.setImageBitmap(bitmap);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //Запись файла в папку
                    File file = new File(getFilesDir().getPath() + File.separator + "SkyChatImage" + File.separator +
                            "cameraPictures" + File.separator +
                            chat.getChatId() + File.separator + "send" + File.separator
                            + photoName + ".jpg");
                    OutputStream out = null;
                    try {
                        out = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
            btnSendPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Можно так-же проверить интернет подключение и если всё ок,тогда сразу перенаправить в чатактивити и в потоке отправить фото в сторэж
                    dialog.setMessage("Sending");
                    dialog.show();
                    saveImageInStorage();
                }
            });
        }
    }

    //Save camera image
    private void saveImageInStorage(){
        StorageReference storage = FirebaseStorage.getInstance().getReference().child("ChatImage")
                .child(chat.getChatId()).child(photoName);
        Uri imageUri = Uri.fromFile(new File(filePath));
        storage.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                dialog.dismiss();
                Intent intentChat = new Intent(CameraActivity.this,ChatRoomActivity.class);
                intentChat.putExtra("chat",(Serializable)chat);
                intentChat.putExtra("user",(Serializable)user);
                intentChat.putExtra("name",photoName);
                startActivity(intentChat);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CameraActivity.this,"Error can not send file, check your internet connection!",Toast.LENGTH_SHORT).show();
            }
        });
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

    public static Bitmap rotateImage(String filePath){

        Bitmap bitmap = BitmapFactory.decodeFile(filePath);

        try {
            ExifInterface exif = new ExifInterface(filePath);
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
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // rotating bitmap
        }
        catch (Exception e) {

        }
        return bitmap;
    }
}
