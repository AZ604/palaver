package com.e.palavar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity implements Conversational {

    ListView listView;
    TextView textView;
    String[] array = null;
    Writer writer;
    private String username, password;
    private boolean mTwoPane;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        listView = findViewById(R.id.listView);
        //textView = findViewById(R.id.textView);
        getInfos();
        writer = new Writer(getApplicationContext());
        if (Access.hasActiveInternetConnection(getApplicationContext()) == true) {
            Connection connection = new Connection(getApplicationContext(), username, password, this);
            connection.getFriends();
        } else {
            array = writer.getFriendArrayOffline();
            setFriendlist();
            Toast toast = Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.offlineMode), Toast.LENGTH_LONG);
            toast.show();
        }

        if (findViewById(R.id.item_detail_container) != null) {
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

    }

    @Override
    public void onBackPressed() {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(getApplicationContext().openFileOutput("remember.txt", Context.MODE_PRIVATE));
            writer.write("n");
            writer.close();
        } catch (IOException e) {
            Log.e("OutputStreamWriter", "Fehler beim schreibem" + e.toString());
        }
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }

    private void getInfos() {
        Intent intent = getIntent();
        username = intent.getStringExtra("Username");
        password = intent.getStringExtra("Password");
    }

    private void setFriendlist() {

        if (array == null) return;
        if (array[0] == null) return;

        final MainActivity mParentActivity = MainActivity.this;

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, array);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // TODO Auto-generated method stub
                String value = adapter.getItem(position);
                if (value == getResources().getString(R.string.FreundHinzufügenOderLöschen) && position == array.length - 1) {
                    goToAddFriendActivity();
                }
                else {
                    if (mTwoPane) {

                        Bundle arguments = new Bundle();
                        ChatActivityFragment fragment = new ChatActivityFragment();
                        arguments.putString("Friendname", value);
                        arguments.putString("Username", username);
                        arguments.putString("Password", password);
                        fragment.setArguments(arguments);
                        mParentActivity.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.item_detail_container, fragment).commit();
                    } else {
                            goToChatActivity(value);
                    }
                }
            }
        });
    }


    private void goToAddFriendActivity() {
        Intent intent = new Intent(getApplicationContext(), AddFriendActivity.class);
        goToNextActivity(intent);
    }

    private void goToChatActivity(String friendname) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra("FriendName", friendname);
        goToNextActivity(intent);
    }

    private void goToNextActivity(Intent intent) {
        intent.putExtra("Username", username);
        intent.putExtra("Password", password);
        writer.saveFriends(array);
        startActivity(intent);
    }

    private void getToken() {


    }


    @Override
    public void dealWithConversation(String response, String command) {
        if (command == getApplicationContext().getResources().getString(R.string.getFriendsComm)) {
            if (response != null) {
                try {

                    JSONObject JSONobject = new JSONObject(response);
                    JSONArray friendlist = JSONobject.getJSONArray("Data");
                    array = new String[friendlist.length() + 1];
                    for (int i = 0; i < friendlist.length(); i++) {
                        array[i] = friendlist.getString(i);
                    }
                    array[array.length - 1] = getResources().getString(R.string.FreundHinzufügenOderLöschen);
                    setFriendlist();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
