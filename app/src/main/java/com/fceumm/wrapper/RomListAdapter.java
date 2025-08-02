package com.fceumm.wrapper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class RomListAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> romList;

    public RomListAdapter(Context context, List<String> romList) {
        super(context, R.layout.rom_list_item, romList);
        this.context = context;
        this.romList = romList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.rom_list_item, parent, false);
            
            holder = new ViewHolder();
            holder.romName = convertView.findViewById(R.id.rom_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String romName = romList.get(position);
        holder.romName.setText(romName);

        return convertView;
    }

    private static class ViewHolder {
        TextView romName;
    }
} 