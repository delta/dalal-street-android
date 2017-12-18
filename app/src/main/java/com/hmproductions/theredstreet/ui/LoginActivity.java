package com.hmproductions.theredstreet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.ui.HomeActivity;

public class LoginActivity extends AppCompatActivity {

    EditText email,password;
    TextInputLayout email_input,password_input;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar=(Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);

        email=(EditText)findViewById(R.id.email);
        password=(EditText)findViewById(R.id.password);
        email_input=(TextInputLayout)findViewById(R.id.email_input);
        password_input=(TextInputLayout)findViewById(R.id.password_input);
        login=(Button)findViewById(R.id.button_login);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLogin();
            }
        });

    }

    public void setLogin(){

        if(validateEmail()){
            if(validatePassword()){
                Toast.makeText(this, "authenticated", Toast.LENGTH_SHORT).show();//todo: login request
                Intent intent=new Intent(this, HomeActivity.class);
                intent.putExtra("username",email.getText().toString());
                startActivity(intent);
                finish();
            }

        }

    }

    public boolean validateEmail(){
        if(email.getText().toString().trim().isEmpty()){
            email_input.setError("enter an email");
            email.requestFocus();
            return false;
        }
        else if(!email.getText().toString().contains("@")){
            email_input.setError("enter a valid email");
            email.requestFocus();
            return false;
        }
        else{
            email_input.setErrorEnabled(false);
        }

        return true;

    }

    public boolean validatePassword(){
        if(password.getText().toString().trim().isEmpty()){
            password_input.setError("enter a password");
            password.requestFocus();
            return false;
        }
        else{
            password_input.setErrorEnabled(false);
        }

        return true;

    }


}
