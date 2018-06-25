package asafov.naum.skychat;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainSignUpFragment extends Fragment implements View.OnClickListener{

    private EditText ETemailReg;
    private EditText ETpasswordReg;
    private EditText ETnickNameReg;
    private Button btnSignUp;
    private ProgressBar loading;
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private List<String> usersNickNamesList;



    public MainSignUpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_sign_up,container,false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        loading = (ProgressBar) view.findViewById(R.id.loadingIconMainSignUpFragment);
        ETemailReg = (EditText) view.findViewById(R.id.edEmailReg);
        ETpasswordReg = (EditText) view.findViewById(R.id.edPasswordReg);
        ETnickNameReg = (EditText) view.findViewById(R.id.edNickNameReg);
        btnSignUp = (Button) view.findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(this);
        usersNickNamesList = new ArrayList<>();
        initUsersList();


        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, new MainFragment());
                ft.commit();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.btnSignUp){
            if (ETemailReg.getText().toString() != null && ETpasswordReg.getText().toString() != null
                    && !ETemailReg.getText().toString().equals("") && !ETpasswordReg.getText().toString().equals("")
                    && ETnickNameReg.getText().toString() != null && !ETnickNameReg.getText().toString().equals("")){
                if (checkNickname(ETnickNameReg.getText().toString())){
                    Log.d("NICKNAMEs",ETnickNameReg.getText().toString());
                    for (String n : usersNickNamesList){
                        Log.d("LISNI",n);
                    }
                    createAccount(ETemailReg.getText().toString(),ETpasswordReg.getText().toString(),ETnickNameReg.getText().toString());
                }else {
                    Toast.makeText(getActivity(),"Nickname exist",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //Инициализировать массив никнеймов
    private void initUsersList(){
        //Получить всех пользователей и поместить в массив
        FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {Log.d("LISNI","OK");
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    User someUser = postSnapshot.getValue(User.class);
                    usersNickNamesList.add(someUser.getNickname());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("LISNI",databaseError.toString());
            }
        });
    }

    private boolean checkNickname(String nickName){
        for (String nick : usersNickNamesList){
            if (nick.equals(nickName)){
                return false;
            }
        }
        return true;
    }

    private void updateUI(FirebaseUser user) {
        if (user != null){
            Intent intent = new Intent(getActivity(),ListFriendsActivity.class);
            startActivity(intent);
        }
    }

    //Создание нового аккаунта
    private void createAccount(final String email, String password, final String nickname){
        loading.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(getActivity(),
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //Если регистрация прошла успешно,то обновляем информацию о пользователи.
                            FirebaseUser user = mAuth.getCurrentUser();
                            String userId = user.getUid();
                            addUserToDataBase(userId,email,nickname);
                            createFolderProfileimage(nickname);
                            loading.setVisibility(View.INVISIBLE);
                            updateUI(user);
                        }else {
                            //Если регистрация не прошла успешно,то сообщаем пользователю.
                            loading.setVisibility(View.INVISIBLE);
                            Toast.makeText(getActivity(), "Registration failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    //Добавить пользователя в базу данных
    private void addUserToDataBase(String userId,String userEmail,String nickname){
        User user = new User(userId,userEmail,nickname);
        database.child("users").child(userId).setValue(user);
    }

//Создать папку для фотографии профиля
    private void createFolderProfileimage(String nickname){
        String f = getContext().getFilesDir().getPath() + File.separator + "SkyChatImage" + File.separator + "profile_images";
        File child = new File(f, nickname);//Создание папки в каталоге profile_images
        child.mkdirs();
        loadDefaultProfileImage(nickname);
    }

    private void loadDefaultProfileImage(String nickname){
        Bitmap defaultProfileImage = BitmapFactory.decodeResource(getResources(),R.drawable.profile);

        //Запись файла в папку
        File file = new File(getContext().getFilesDir().getPath() + File.separator + "SkyChatImage" + File.separator + "profile_images" + File.separator + nickname + File.separator + "profile.jpg");
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            defaultProfileImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadDefaultProfileImageToStorage(file,nickname);
    }

    private void loadDefaultProfileImageToStorage(File file,String nickname){
        StorageReference filepath = FirebaseStorage.getInstance().getReference().child("ProfilePhoto").child(nickname).child("profile");

        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        UploadTask uploadTask = filepath.putStream(stream);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
            }
        });
    }
}
