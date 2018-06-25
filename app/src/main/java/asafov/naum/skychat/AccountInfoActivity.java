package asafov.naum.skychat;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import asafov.naum.skychat.functions.Functions;

public class AccountInfoActivity extends AppCompatActivity {

    public static final int IMAGE_GALLERY_REQUEST = 20;
    private StorageReference mStorage;
    DatabaseReference myRef;
    ImageView profileImage;
    ProgressBar loadingIcon;
    Uri imageUri;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    User curUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//Кнопка назад
        profileImage = (ImageView) findViewById(R.id.profilePhoto);
        loadingIcon = (ProgressBar) findViewById(R.id.loadingIcon);
        myRef = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getInstance().getCurrentUser();
        //Цвет загрузочной иконки
        loadingIcon.getIndeterminateDrawable().setColorFilter(
                getResources().getColor(R.color.colorPrimary),
                android.graphics.PorterDuff.Mode.SRC_IN);

        mStorage = FirebaseStorage.getInstance().getReference();
        getCurrentUser();// Get nickname
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

    public void onImageGalleryClicked(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();

        Uri data = Uri.parse(pictureDirectoryPath);

        photoPickerIntent.setDataAndType(data, "image/*");

        startActivityForResult(photoPickerIntent, IMAGE_GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            if (requestCode == IMAGE_GALLERY_REQUEST) {
                //Убераем показ загрузки
                profileImage.setVisibility(View.INVISIBLE);
                loadingIcon.setVisibility(View.VISIBLE);
                // Адресс изображения
                imageUri = data.getData();


                StorageReference filepath = mStorage.child("ProfilePhoto").child(curUser.getNickname()).child("profile");
                filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Loading end
                        profileImage.setVisibility(View.VISIBLE);
                        loadingIcon.setVisibility(View.INVISIBLE);
                        Toast.makeText(AccountInfoActivity.this, "Upload done", Toast.LENGTH_SHORT).show();
                        // Поток для чтения изображения .
                        InputStream inputStream;
                        try {
                            inputStream = getContentResolver().openInputStream(imageUri);

                            // Получить bitmap из потока.
                            Bitmap image = BitmapFactory.decodeStream(inputStream);

                            // Показать изображение пользователю.
                            //profileImage.setImageBitmap(image);

                            //Запись файла в папку
                            File file = new File(getFilesDir().getPath() + File.separator + "SkyChatImage" + File.separator + "profile_images" + File.separator + curUser.getNickname() + File.separator + "profile.jpg");
                            OutputStream out = null;
                            try {
                                out = new FileOutputStream(file);
                                image.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                out.flush();
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                int orientation = getOrientation(AccountInfoActivity.this,imageUri);
                                Matrix matrix = new Matrix();
                                if (orientation == 90) {
                                    matrix.postRotate(90);
                                }
                                else if (orientation == 180) {
                                    matrix.postRotate(180);
                                }
                                else if (orientation == 270) {
                                    matrix.postRotate(270);
                                }
                                image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true); // rotating bitmap
                            }
                            catch (Exception e) {
                            }
                            profileImage.setImageBitmap(image);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(AccountInfoActivity.this, "Unable to open image", Toast.LENGTH_LONG).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AccountInfoActivity.this, "Unable to upload file", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void getCurrentUser() {
        myRef.child("users").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                curUser = dataSnapshot.getValue(User.class);
                getFolderPath();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFolderPath() {
        String f = getFilesDir().getPath() + File.separator + "SkyChatImage"+ File.separator + File.separator +"profile_images";
        File child = new File(f, curUser.getNickname());//Создание папки в каталоге profile_images
        child.mkdirs();
        f = getFilesDir().getPath() + File.separator + "SkyChatImage" + File.separator + "profile_images" + File.separator + curUser.getNickname() + File.separator + "profile.jpg";
        Bitmap image = BitmapFactory.decodeFile(f);
        /*Log.d("MYTAG","TAG");
        try {
            //Uri u = image. sdelat
            //int orientation = getOrientation(AccountInfoActivity.this,getImageContentUri(AccountInfoActivity.this,new File(f)));
            Log.d("MYTAG","OR = " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 90) {
                matrix.postRotate(90);
            }
            else if (orientation == 180) {
                matrix.postRotate(180);
            }
            else if (orientation == 270) {
                matrix.postRotate(270);
            }
            image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true); // rotating bitmap
        }
        catch (Exception e) {
            e.printStackTrace();
        }*/
        profileImage.setImageBitmap(image);
    }


    private static int getOrientation(Context context, Uri photoUri) {
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

        if (cursor.getCount() != 1) {
            cursor.close();
            return -1;
        }

        cursor.moveToFirst();
        int orientation = cursor.getInt(0);
        cursor.close();
        cursor = null;
        return orientation;
    }
}
