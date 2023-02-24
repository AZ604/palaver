package com.e.palavar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.e.palavar.Access.*;

public class ChatActivity extends AppCompatActivity implements Conversational, GoogleApiClient.ConnectionCallbacks {

    Button send;
    Button sendFile;
    Button sendLocation;
    TextView text;
    ListView listView;
    String[] array;
    Writer writer;
    private String username, password, friendName;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);
        getInfos();
        writer = new Writer(getApplicationContext());
        final Connection connection;
        listView = findViewById(R.id.chatlist);
        text = findViewById(R.id.txtViewSend);
        send = findViewById(R.id.btnSendMessage);
        sendFile = findViewById(R.id.btnSendFile);
        sendLocation = findViewById(R.id.btnSendLocation);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String data = (String) adapterView.getItemAtPosition(i);
                String msg = data.substring(data.indexOf(":")+2);
                if (msg.startsWith("$$$")) {
                    byte[] arr = Base64.decode(msg.substring(3, msg.lastIndexOf(".")), Base64.DEFAULT);
                    String extension = msg.substring(msg.indexOf("."));
                    saveToExternalStorage(arr, extension);
                }
                else if (URLUtil.isNetworkUrl(msg)) {
                        try {
                            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(msg));
                            startActivity(myIntent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(ChatActivity.this, "No application can handle this request."
                                    + " Please install a webbrowser",  Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                }


            }
        });



        if (hasActiveInternetConnection(getApplicationContext())) {
            connection = new Connection(getApplicationContext(), username, password, this);
            connection.getConversation(friendName);
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connection.sendMessage(text.getText().toString(), friendName);
                    addMessage(username+": "+text.getText().toString());
                    text.setText("");

                }
            });
            sendFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    fileIntent.setType("*/*");
                    startActivityForResult(fileIntent, 10);

                }
            });
            sendLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        new AlertDialog.Builder(ChatActivity.this)
                                .setTitle("Required Location Permission")
                                .setMessage("You need the required permissions to access location")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 42069);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .create()
                                .show();
                    }
                    else {
                        mFusedLocationClient.getLastLocation().addOnSuccessListener(ChatActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    connection.sendMessage("https://www.google.com/maps/search/?api=1&query=" + location.getLatitude() + "," + location.getLongitude(), friendName);
                                    addMessage(username+": "+ "https://www.google.com/maps/search/?api=1&query=" + location.getLatitude() + "," + location.getLongitude());
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "location is null", Toast.LENGTH_SHORT);
                                }
                            }
                        });
                    }
                }
            });

        } else {
            loadConversation();
            text.setVisibility(View.INVISIBLE);
            send.setVisibility(View.INVISIBLE);
        }
    }

    private String[] addMessageToConversationArray(String message)
    {
        String [] toReturn = new String[array.length+1];
        for(int i = 0; i<array.length;i++)
        {
            toReturn[i]=array[i];
        }
        toReturn[toReturn.length-1]=message;
        return toReturn;
    }

    private void addMessage(String message)
    {
        array = addMessageToConversationArray(message);
        setListView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 10) {
            Uri fileURI = data.getData();
            try {
                InputStream file = getContentResolver().openInputStream(fileURI);
                Connection connection = new Connection(getApplicationContext(), username, password, this);
                byte[] bytes = getBytes(file);
                String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
                final String extension = getFileName(fileURI);
                Log.e("extension", "extension name = " + extension.substring(extension.lastIndexOf(".")));
                if ( checkIsImage(ChatActivity.this,fileURI )) {
                    connection.sendMessage("$$" + base64 + extension.substring(extension.lastIndexOf(".")), friendName);
                    addMessage(username+": "+ "$$" + base64 +extension.substring(extension.lastIndexOf(".")));

                }
                else {
                    connection.sendMessage("$$$" + base64 + extension.substring(extension.lastIndexOf(".")), friendName);
                    addMessage(username+": "+ "$$$" + base64  + extension.substring(extension.lastIndexOf(".")) );
                }
                file.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void getInfos() {
        Intent intent = getIntent();
        username = intent.getStringExtra("Username");
        password = intent.getStringExtra("Password");
        friendName = intent.getStringExtra("FriendName");
    }


    public void loadConversation() {
        array = writer.getConversationArrayOffline(friendName);
        setListView();
    }

    private void setListView() {
        if (array == null) {
            Toast toast = Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.offlineMode), Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        final CustomAdapter<String> adapter = new CustomAdapter<String>(this, array, username, friendName);
        listView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (array != null) {
            writer.saveConversation(array, friendName);
        }
    }

    @Override
    public void dealWithConversation(String response, String command) {
        if (command == getApplicationContext().getResources().getString(R.string.messageSendComm)) {
            try {
                JSONObject object = new JSONObject(response);
                Toast toast = Toast.makeText(getApplicationContext(), object.get("Info").toString(), Toast.LENGTH_LONG);
                toast.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (command == getApplicationContext().getResources().getString(R.string.getConversationComm)) {
            try {
                JSONObject object = new JSONObject(response);
                System.out.println(object.get("Info"));
                JSONArray JsonArray = object.getJSONArray("Data");
                array = new String[JsonArray.length()];
                for (int i = 0; i < JsonArray.length(); i++) {
                    JSONObject object1 = new JSONObject(JsonArray.get(i).toString());
                    array[i] = object1.get("Sender").toString() + ": " + object1.get("Data").toString();
                }
                setListView();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 42069) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }
            else {
                return;
            }
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static boolean checkIsImage(Context context, Uri uri) throws IOException {
        ContentResolver contentResolver = context.getContentResolver();
        String type = contentResolver.getType(uri);
        if (type != null) {
            return  type.startsWith("image/");
        } else {
            // try to decode as image (bounds only)
            InputStream inputStream = null;
            try {
                inputStream = contentResolver.openInputStream(uri);
                if (inputStream != null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(inputStream, null, options);
                    return options.outWidth > 0 && options.outHeight > 0;
                }
            } catch (IOException e) {
                // ignore
            } finally {
                inputStream.close();
            }
        }
        // default outcome if image not confirmed
        return false;
    }

    private void saveToExternalStorage(byte[] arr, String extension) {
        if (isStoragePermissionGranted()) {
            File path = Environment.getExternalStorageDirectory();
            File dir = new File(path+"/Download");
            dir.mkdirs();
            String fname = "temp" + extension;
            File file = new File(dir, fname);
            if (file.exists())
                file.delete();
            try {
                file.createNewFile();
                FileOutputStream out = new FileOutputStream(file);
                out.write(arr);
                out.flush();
                out.close();
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(file));
                sendBroadcast(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            MediaScannerConnection.scanFile(this, new String[]{file.toString()}, new String[]{file.getName()}, null);
            Toast toast = Toast.makeText(getApplicationContext(), "Saved file in download directory", Toast.LENGTH_SHORT);
        }
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public void openFile(Context context, File url) throws IOException {

        Uri fileuri = Uri.fromFile(url);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        // Check what kind of file you are trying to open, by comparing the url with extensions.
        // When the if condition is matched, plugin sets the correct intent (mime) type,
        // so Android knew what application to use to open the file
        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(fileuri, "application/msword");
        } else if (url.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(fileuri, "application/pdf");
        } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(fileuri, "application/vnd.ms-powerpoint");
        } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(fileuri, "application/vnd.ms-excel");
        } else if (url.toString().contains(".zip") || url.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(fileuri, "application/x-wav");
        } else if (url.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(fileuri, "application/rtf");
        } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(fileuri, "audio/x-wav");
        } else if (url.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(fileuri, "image/gif");
        } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
            // JPG file
            intent.setDataAndType(fileuri, "image/jpeg");
        } else if (url.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(fileuri, "text/plain");
        } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") || url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(fileuri, "video/*");
        } else {
            //if you want you can also define the intent type for any other file

            //additionally use else clause below, to manage other unknown extensions
            //in this case, Android will show all applications installed on the device
            //so you can choose which application to use
            intent.setDataAndType(fileuri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public boolean isStoragePermissionGranted() {
        String TAG = "Storage Permission";
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }
}
