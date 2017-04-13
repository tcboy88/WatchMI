package hsyeo.watchmi;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    int currentPos;

    private ArrayList<String> list;

    public CustomAdapter() {
        list = new ArrayList<String>();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final Context context = parent.getContext();
        TextView textView;
        CustomHolder holder;
        ImageView imageView;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.custom_item, parent, false);
            textView = (TextView) view.findViewById(R.id.text);
            imageView = (ImageView) view.findViewById(R.id.img);

            holder = new CustomHolder();
            holder.textView = textView;
            holder.imageView = imageView;
            view.setTag(holder);
        } else{
            holder = (CustomHolder) view.getTag();
            textView = holder.textView;
            imageView = holder.imageView;
        }

        textView.setText(list.get(position));
        if(position == currentPos) {
            textView.setTextColor(Color.RED);
            textView.setBackgroundColor(Color.argb(120,200,200,200));
        } else {
            textView.setTextColor(Color.WHITE);
            textView.setBackgroundColor(Color.BLACK);
        }

        imageView.setImageResource(R.drawable.folder);
        return view;
    }

    private class CustomHolder {
        TextView textView;
        ImageView imageView;
    }

    public void add(String msg) {
        list.add(msg);
    }

    public void clear(){
        list.clear();
    }

    public void setCurrentPos(int current) {
        this.currentPos = current;
    }
}
