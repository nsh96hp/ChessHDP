package com.onezeros.chinesechess;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.chinesechess.R;

import java.util.ArrayList;


public class Adapter extends ArrayAdapter<ChessResult> {
    private Context context;
    private int resource;
    private ArrayList<ChessResult> lstChess;

    public Adapter(Context context, int resource, ArrayList<ChessResult> lstChess) {
        super(context, resource, lstChess);
        this.context = context;
        this.resource = resource;
        this.lstChess = lstChess;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_tsa, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.item_time = (TextView) convertView.findViewById(R.id.item_time);
            viewHolder.item_level = (TextView) convertView.findViewById(R.id.item_level);
            viewHolder.item_result = (TextView) convertView.findViewById(R.id.item_result);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ChessResult c = lstChess.get(position);
        viewHolder.item_time.setText(c.getTime()+"");

        switch (lstChess.get(position).getLevel()){
            case 3:
                viewHolder.item_level.setText(context.getResources().getString(R.string.lv1));
                break;
            case 4:
                viewHolder.item_level.setText(context.getResources().getString(R.string.lv2));
                break;
            case 5:
                viewHolder.item_level.setText(context.getResources().getString(R.string.lv3));
                break;
        }

        switch (lstChess.get(position).getResult()){
            case 0:
                viewHolder.item_result.setText(context.getResources().getString(R.string.lose));
                break;
            case 1:
                viewHolder.item_result.setText(context.getResources().getString(R.string.win));
                break;
        }

        return convertView;

    }


    public class ViewHolder {
        TextView item_time, item_level, item_result;

    }
}
