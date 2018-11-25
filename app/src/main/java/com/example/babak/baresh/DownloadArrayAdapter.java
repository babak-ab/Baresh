package com.example.babak.baresh;
import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

public class DownloadArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;

    public DownloadArrayAdapter(Context context, String[] values) {
        super(context, R.layout.download_row_layout, values);
        this.context = context;
        this.values = values;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.download_row_layout, parent, false);
        final ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageResource(android.R.drawable.ic_media_pause);
            }
        });

//        ArrayList<Integer> animalNames = new ArrayList<>();
//        animalNames.add(1);
//        animalNames.add(2);
//        animalNames.add(3);
//        animalNames.add(4);
//        animalNames.add(5);
//        RecyclerView recyclerView = rowView.findViewById(R.id.progress_listView);
//        //LinearLayoutManager horizontalLayoutManager
//        //        = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
//        //recyclerView.setLayoutManager(horizontalLayoutManager);
//        RecyclerView.LayoutManager mLayoutManager =
//                new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
//        ProgressArrayAdapter adapter = new ProgressArrayAdapter(context, animalNames);
//        recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL));
//        //ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(context, R.dimen.item_offset);
//        //recyclerView.addItemDecoration(itemDecoration);
//        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.setAdapter(adapter);

        return rowView;
    }
}