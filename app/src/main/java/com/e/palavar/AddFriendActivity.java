package com.e.palavar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class AddFriendActivity extends AppCompatActivity implements Conversational {

    Button addFriend;
    Button removeFriend;
    TextView friendName;
    String username;
    String password;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addfirend_activity);
        getInfos();
        addFriend = findViewById(R.id.btnAddFriend);
        removeFriend = findViewById(R.id.btnRemoveFriend);
        friendName = findViewById(R.id.textFriendName);
        final Connection connection = new Connection(getApplicationContext(), username, password, this);
        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connection.setFriend(friendName.getText().toString());
            }
        });

        removeFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connection.deleteFriend(friendName.getText().toString());
            }
        });


    }

    private void getInfos() {
        Intent intent = getIntent();
        username = intent.getStringExtra("Username");
        password = intent.getStringExtra("Password");
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("Username", username);
        intent.putExtra("Password", password);
        startActivity(intent);
    }

    @Override
    public void dealWithConversation(String response, String command) {

        try {
            JSONObject object = new JSONObject(response);
            Toast toast = Toast.makeText(getApplicationContext(), object.get("Info").toString(), Toast.LENGTH_LONG);
            toast.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
