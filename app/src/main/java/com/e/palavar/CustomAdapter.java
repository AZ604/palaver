package com.e.palavar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomAdapter<T> extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;
    private final String username, friendName;

    public CustomAdapter(Context context, String[] values, String username, String friendName) {
        super(context, R.layout.rowlayout, values);
        this.context = context;
        this.values = values;
        this.username = username;
        this.friendName = friendName;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        String s = values[position];

        View rowView;
        if (s.startsWith(this.friendName)) {
            rowView = inflater.inflate(R.layout.rowlayout, parent, false);
        }
        else {
            rowView = inflater.inflate(R.layout.rowlayout_right, parent, false);
        }
        if (s.startsWith(this.friendName + ": https") || s.startsWith(this.username + ": https")) {
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            imageView.setImageResource(R.drawable.baseline_location_on_24);
            TextView textView = (TextView) rowView.findViewById(R.id.label);
            textView.setText(values[position].substring(0, values[position].indexOf(":") + 1));
        }
        else {
            if (s.startsWith(this.friendName + ": $$$") || s.startsWith(this.username + ": $$$")) {
                ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
                imageView.setImageResource(R.drawable.ic_launcher_dwnld);
                TextView textView = (TextView) rowView.findViewById(R.id.label);
                textView.setText(values[position].substring(0, values[position].indexOf(":") + 1));
            } else {
                if (s.startsWith(this.friendName + ": $$") || s.startsWith(this.username + ": $$")) {
                    ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
                    s = s.substring(s.indexOf('$') + 2, s.lastIndexOf("."));
                    byte[] decodedString = android.util.Base64.decode(s, android.util.Base64.DEFAULT);
                    Bitmap bMap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    int a = bMap.getHeight();
                    int b = bMap.getWidth();
                    imageView.setMinimumWidth(b);
                    imageView.setMinimumHeight(a);
                    imageView.setImageBitmap(bMap);
                    TextView textView = (TextView) rowView.findViewById(R.id.label);
                    textView.setText(values[position].substring(0, values[position].indexOf(":") + 1));
                } else {
                    TextView textView = (TextView) rowView.findViewById(R.id.label);
                    textView.setText(values[position]);
                }
            }
        }
        return rowView;
    }
}
