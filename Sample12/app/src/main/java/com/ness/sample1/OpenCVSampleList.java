package com.ness.sample1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class OpenCVSampleList extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView lvList;
    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_cv_sample_list);
        InitView();
    }

    private void InitView() {

        context = this;
        lvList = (ListView) findViewById(R.id.lv_sample_list);
        lvList.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, getIndex()));
        lvList.setOnItemClickListener(this);

    }

    private String[] getIndex() {

        return new String[]{"Gray Image", "Detect Circle", "Detect Canny","Detect OMR"};
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent activityIntent = new Intent(this, MainActivity.class);
        activityIntent.putExtra(Constants.POSITION, position);
        startActivity(activityIntent);
    }
}
