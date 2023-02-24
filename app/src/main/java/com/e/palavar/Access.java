package com.e.palavar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Access {


    public static boolean hasActiveInternetConnection(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


}
