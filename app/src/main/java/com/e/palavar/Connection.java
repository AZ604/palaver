package com.e.palavar;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class Connection {

    String username;
    String password;
    Conversational mainActivity;
    private Context context;


    public Connection(Context context, String username, String password, Conversational activity) {
        this.context = context;
        this.username = username;
        this.password = password;
        this.mainActivity = activity;
    }


    public void sendToServer(final String url, final JSONObject object, final String command) {
        final String savedata = object.toString();
        System.out.println("send requst with json: " + object.toString());
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    JSONObject objres = new JSONObject(response);
                    System.out.println("server responded with: " + objres.toString());
                    mainActivity.dealWithConversation(response, command);

                } catch (JSONException e) {
                    Toast.makeText(context, "Server Error", Toast.LENGTH_LONG).show();
                    System.out.println("json exception");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                System.out.println("server error no response:" + error.getMessage());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return savedata == null ? null : savedata.getBytes(StandardCharsets.UTF_8);
            }
        };
        requestQueue.add(stringRequest);
    }


    public void setFriend(String friendName) {
        sendToServer("http://palaver.se.paluno.uni-due.de/api/friends/add", addRestToAddFriend(friendName), "setNewFriend");
    }

    public void deleteFriend(String friendName) {
        sendToServer("http://palaver.se.paluno.uni-due.de/api/friends/remove", addRestToAddFriend(friendName), context.getResources().getString(R.string.setNewFriendComm));
    }

    private JSONObject getPerson() {
        JSONObject person = new JSONObject();
        try {
            person.put("Username", username);
            person.put("Password", password);
        } catch (JSONException e) {
            e.printStackTrace();

        } finally {
            return person;
        }
    }

    private JSONObject addRestToAddFriend(String friendName) {
        JSONObject object = getPerson();
        try {
            object.put("Friend", friendName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    public void sendMessage(String data, String friendName) {
        sendToServer("http://palaver.se.paluno.uni-due.de/api/message/send", addRestToMessage(context.getResources().getString(R.string.TextPlain), data, friendName), context.getResources().getString(R.string.messageSendComm));
    }

    public void getConversation(String friendName) {
        sendToServer("http://palaver.se.paluno.uni-due.de/api/message/get", addRestToMessage(null, null, friendName), context.getResources().getString(R.string.getConversationComm));
    }


    private JSONObject addRestToMessage(String type, String data, String friendName) {
        JSONObject object = getPerson();
        try {
            object.put("Recipient", friendName);
            if (type == null && data == null) {
                return object;
            }
            object.put("Mimetype", type);
            object.put("Data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    public void getFriends() {
        sendToServer("http://palaver.se.paluno.uni-due.de/api/friends/get", getPerson(), context.getResources().getString(R.string.getFriendsComm));
    }

    public void register() {
        sendToServer("http://palaver.se.paluno.uni-due.de/api/user/register", getPerson(), context.getResources().getString(R.string.registerComm));
    }

    public void validateUser() {
        sendToServer("http://palaver.se.paluno.uni-due.de/api/user/validate", getPerson(), context.getResources().getString(R.string.validateUserComm));
    }


}
