package com.sagi.supertictactoeonline.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sagi.supertictactoeonline.R;
import com.sagi.supertictactoeonline.interfaces.IWaitingProgressBar;

public class LoginFragment extends Fragment implements IWaitingProgressBar {

    private OnFragmentInteractionListener mListener;

    private EditText edtEmail, edtPass;
    private CheckBox checkBoxRememberMe;
    private Button btnLogin;
    private TextView textViewRegister;
    private boolean isRememberMe = false;
    private ProgressDialog progressDialog;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressDialog = new ProgressDialog(getContext());
        loadViews(view);
        loadListeners();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            mListener.registerEventFromRegisterLogin(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.registerEventFromRegisterLogin(null);
        mListener = null;
    }

    private void loadListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isValid())
                    return;
                showProgressDialog();
                checkIfUserExist();
            }
        });
        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegisterScreen();
            }
        });
        checkBoxRememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
                isRememberMe = isCheck;
            }
        });
    }


    private void showRegisterScreen() {
        mListener.showRegisterFragment();
    }

    private boolean isValid() {
        String email = edtEmail.getText().toString();
        String pass = edtPass.getText().toString();

        if (pass.trim().equals("") || email.equals("")) {
            Toast.makeText(getContext(), "must fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        } else if (pass.length() < 6) {
            Toast.makeText(getContext(), "Pass must have at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!isEmailValid(email)) {
            Toast.makeText(getContext(), "Email is not valid", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void checkIfUserExist() {
        String email = edtEmail.getText().toString();
        String pass = edtPass.getText().toString();
        mListener.signIn(email, pass, isRememberMe);
    }

    private void showProgressDialog() {
        progressDialog.setMessage("Try login your profile");
        progressDialog.setTitle("Waiting");
        progressDialog.setCancelable(false);
        progressDialog.setIcon(R.drawable.x);
        progressDialog.show();
    }


    private void loadViews(View view) {
        edtEmail = view.findViewById(R.id.edtEmailLogin);
        edtPass = view.findViewById(R.id.edtPassLogin);
        checkBoxRememberMe = view.findViewById(R.id.checkBoxRememberMeLgin);
        btnLogin = view.findViewById(R.id.btnLogin);
        textViewRegister = view.findViewById(R.id.textViewRegister);
    }


    @Override
    public void stopProgressBar() {
        progressDialog.dismiss();
    }


    public interface OnFragmentInteractionListener {
        void signIn(String email, String password, boolean isRememberMe);

        void showRegisterFragment();

        void registerEventFromRegisterLogin(IWaitingProgressBar iWaitingProgressBar);
    }
}
