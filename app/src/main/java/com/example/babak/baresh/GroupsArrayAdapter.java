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

public class GroupsArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private String[] values;//{"General","Archives","Documents","Musics","Videos","Programs"};

    public GroupsArrayAdapter(Context context, String[] values) {
        super(context,android.R.layout.simple_spinner_item, values);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.context = context;
        this.values = values;
    }
}