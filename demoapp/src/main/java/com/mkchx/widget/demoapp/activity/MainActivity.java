package com.mkchx.widget.demoapp.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.mkchx.widget.chart.PieView;
import com.mkchx.widget.demoapp.R;

public class MainActivity extends AppCompatActivity {

    private PieView chartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chartView = (PieView) findViewById(R.id.circle_bar);
        chartView.setTextSize(R.dimen.test);
        chartView.setTextColor(android.R.color.black);

        chartView.addSlice("Beef", 30);
        chartView.addSlice("Vegetarian", 20);
        chartView.addSlice("Pork", 40);
        chartView.addSlice("Other", 10);

        chartView.draw();
        chartView.setOnSliceClickListener(new PieView.onSliceClickListener() {
            @Override
            public void onSliceClick(int position, float percentage) {
                Toast.makeText(MainActivity.this, "Slice Index " + position + " with " + (int) percentage + "%", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
