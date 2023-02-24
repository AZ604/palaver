package com.e.palavar;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Writer {

    Context context;
    String username, password;

    public Writer(Context context) {
        this.context = context;
        this.password = getLastUserPassword();
        this.username = getLastUserName();
    }

    public String getText(String path) {
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(path);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("DateiLesen", "Datei nicht gefunden: " + e.toString());
        } catch (IOException e) {
            Log.e("DateiLesen", "Kann datei nicht lesen: " + e.toString());
        }
        return ret;
    }

    private String getLastUser() {
        return getText("last.txt");
    }

    public String getLastUserName() {
        String user = getLastUser();
        if (user == "") return "";
        System.out.println(user.substring(1, user.indexOf("§")));
        return user.substring(1, user.indexOf("§"));
    }

    public String getLastUserPassword() {
        String user = getLastUser();
        if (user == "") return "";
        System.out.println(user.substring(user.indexOf("§") + 1));
        return user.substring(user.indexOf("§") + 1);

    }

    public void writeLastuser(String userName, String password) {
        System.out.println(userName);
        String out = userName + "§" + password;
        try {
            OutputStreamWriter writer = new OutputStreamWriter(context.openFileOutput("last.txt", Context.MODE_PRIVATE));
            writer.write(out);
            writer.close();
        } catch (IOException e) {
            Log.e("OutputStreamWriter", "Fehler beim schreibem" + e.toString());
        }
    }

    public boolean getRememberMe() {
        String text = getText("remember.txt");
        if (text.equals("\ny")) {
            return true;
        }
        if (text.equals("\nn")) {
            return false;
        }
        Log.e("Remember", "Rememberme Fehlgeschlagen");
        return false;
    }

    public void setRememberMe(boolean rememberMe) {
        String out = "";
        if (rememberMe == true) {
            out = "y";
        } else {
            out = "n";
        }
        try {
            OutputStreamWriter writer = new OutputStreamWriter(context.openFileOutput("remember.txt", Context.MODE_PRIVATE));
            writer.write(out);
            writer.close();
        } catch (IOException e) {
            Log.e("OutputStreamWriter", "Fehler beim schreibem" + e.toString());
        }
    }

    public void setUserOffline() {
        JSONArray array = null;
        try {
            array = getUserOffline();
        } catch (Exception e) {

        }
        if (array == null) {
            array = new JSONArray();
        }

        if (lookUserUp(array)) {
            return;
        }
        array.put(username + "$$" + password);
        try {
            OutputStreamWriter writer = new OutputStreamWriter(context.openFileOutput("users.txt", Context.MODE_PRIVATE));
            writer.write(array.toString());
            writer.close();
        } catch (IOException e) {
            Log.e("OutputStreamWriter", "Fehler beim schreibem" + e.toString());
        }
    }

    public boolean lookUserUp(JSONArray array) {
        if (array == null) return false;
        for (int i = 0; i < array.length(); i++) {
            try {
                if (array.get(i).toString().equals(username + "$$" + password)) {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public JSONArray getUserOffline() {
        try {
            return new JSONArray(getText("users.txt"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] getFriendArrayOffline() {
        try {
            JSONArray array = new JSONArray(getText(getFileNameForFriendList() + ".txt"));
            String[] out = new String[array.length()];
            for (int i = 0; i < out.length; i++) {
                out[i] = array.get(i).toString();
            }
            return out;


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getFileNameForFriendList() {
        return username + ".txt";
    }

    private String getFileInputForFriendList(String[] array) {

        try {
            JSONArray object = new JSONArray(array);
            if (object.get(object.length() - 1) == context.getResources().getString(R.string.FreundHinzufügenOderLöschen)) {
                object.remove(object.length() - 1);
            }
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void saveFriends(String[] friendList) {
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(context.getApplicationContext().openFileOutput(getFileNameForFriendList() + ".txt", Context.MODE_PRIVATE));
            writer.write(getFileInputForFriendList(friendList));
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void saveConversation(String[] conversation, String friendName) {
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(context.getApplicationContext().openFileOutput(getFileNameForConversation(friendName) + ".txt", Context.MODE_PRIVATE));
            writer.write(getFileInputForConversation(conversation));
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileInputForConversation(String[] array) {

        try {
            JSONArray object = new JSONArray(array);
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String[] getConversationArrayOffline(String friendName) {
        try {
            JSONArray array = new JSONArray(getText(getFileNameForConversation(friendName) + ".txt"));
            String[] out = new String[array.length()];
            for (int i = 0; i < out.length; i++) {
                out[i] = array.get(i).toString();
            }
            return out;


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getFileNameForConversation(String friendName) {
        if (username == null || friendName == null) {
            Log.e("FriendNameOrUsername", friendName + " " + username);
        }
        if (username.compareTo(friendName) > 0) {
            return username + "§§§" + friendName;
        }
        return friendName + "§§§" + username;
    }

}
