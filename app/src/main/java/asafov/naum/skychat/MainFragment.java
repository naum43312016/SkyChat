package asafov.naum.skychat;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class MainFragment extends Fragment implements View.OnClickListener{

    Button openSignIn;
    Button openSignUp;
    public MainFragment(){

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main,container,false);

        openSignIn = (Button) view.findViewById(R.id.btnOpenSignIn);
        openSignUp = (Button) view.findViewById(R.id.btnOpenSignUp);
        openSignIn.setOnClickListener(this);
        openSignUp.setOnClickListener(this);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        return view;
    }


    @Override
    public void onClick(View view) {
        int i = view.getId();
        //Кнопка регистрации
        if (i == R.id.btnOpenSignIn){
            MainSignInFragment mainSignInFragment = new MainSignInFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container,mainSignInFragment);
            ft.commit();
        }
        if (i == R.id.btnOpenSignUp){
            MainSignUpFragment mainSignUpFragment = new MainSignUpFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container,mainSignUpFragment);
            ft.commit();
        }
    }
}
