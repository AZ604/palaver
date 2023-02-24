package com.e.palavar;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity implements Conversational {

    private static TextView usernameWidget;
    private static TextView passwordWidget;
    private static Button confirm;
    private static Button register;
    private static Switch remember;
    LoginActivity loginActivity;
    private Writer writer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        writer = new Writer(getApplicationContext());
        loginActivity = this;

        if (writer.getRememberMe()) {
            getToMainActivity();
        }

        setContentView(R.layout.activity_login);
        usernameWidget = findViewById(R.id.username);
        passwordWidget = findViewById(R.id.password);
        remember = findViewById(R.id.remember);

        confirm = findViewById(R.id.login);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Access.hasActiveInternetConnection(getApplicationContext())) {
                    final Connection connection = new Connection(getApplicationContext(), usernameWidget.getText().toString(), passwordWidget.getText().toString(), loginActivity);
                    connection.validateUser();

                } else {
                    if (writer.lookUserUp(writer.getUserOffline())) {
                        writer.setRememberMe(remember.isChecked());
                        writer.writeLastuser(usernameWidget.getText().toString(), passwordWidget.getText().toString());
                        getToMainActivity();
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.wrong_password_or_username), Toast.LENGTH_LONG);
                        toast.show();
                    }
                }

            }
        });

        register = findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Connection connection = new Connection(getApplicationContext(), usernameWidget.getText().toString(), passwordWidget.getText().toString(), loginActivity);
                connection.register();

            }
        });

    }


    private void getToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("Username", writer.getLastUserName());
        intent.putExtra("Password", writer.getLastUserPassword());
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void dealWithConversation(String response, String command) {
        if (command == getApplicationContext().getResources().getString(R.string.validateUserComm)) {
            responseSuccesCommon(response);
        }

        if (command == getApplicationContext().getResources().getString(R.string.registerComm)) {
            responseSuccesCommon(response);
        }
    }

    private void responseSuccesCommon(String response) {
        if (response == null) {
            return;
        }
        try {
            String MsgType = new JSONObject(response).get("MsgType").toString();
            if (MsgType.equals("1")) {
                loginActivity.doAfterCheckSuccess(true, "");
            } else {
                String info = new JSONObject(response).get("Info").toString();
                loginActivity.doAfterCheckSuccess(false, info);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void doAfterCheckSuccess(boolean success, String errorMsg) {
        if (success) {
            writer.setRememberMe(remember.isChecked());
            writer.writeLastuser(usernameWidget.getText().toString(), passwordWidget.getText().toString());
            writer.setUserOffline();
            getToMainActivity();
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
