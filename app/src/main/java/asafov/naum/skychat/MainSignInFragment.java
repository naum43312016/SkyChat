package asafov.naum.skychat;


import android.app.FragmentManager;
import android.content.Intent;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainSignInFragment extends Fragment implements View.OnClickListener {


    private EditText ETemail;
    private EditText ETpassword;
    private Button btnSignIn;
    private ProgressBar loading;
    private FirebaseAuth mAuth;


    public MainSignInFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_sign_in, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);

        ETemail = (EditText) view.findViewById(R.id.edEmail);
        ETpassword = (EditText) view.findViewById(R.id.edPassword);
        btnSignIn = (Button) view.findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        loading = (ProgressBar) view.findViewById(R.id.loadingIconFragmetnSignIn);



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
        //Кнопка авторизации
        if (i == R.id.btnSignIn) {
            if (ETemail.getText().toString() != null && ETpassword.getText().toString() != null
                    && !ETemail.getText().toString().equals("") && !ETpassword.getText().toString().equals("")) {
                signIn(ETemail.getText().toString(),ETpassword.getText().toString());
            }
            Log.d("MYTAG", "SING in click");
        }
    }

    //Вход пользователя
    private void signIn(String email,String password){
        loading.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(getActivity(),
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            loading.setVisibility(View.INVISIBLE);
                            updateUI(user);
                        }else {
                            // If sign in fails, display a message to the user.
                            loading.setVisibility(View.INVISIBLE);
                            Toast.makeText(getActivity(), "Sign In failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null){
            Intent intent = new Intent(getActivity(),ListFriendsActivity.class);
            startActivity(intent);
        }
    }


}
