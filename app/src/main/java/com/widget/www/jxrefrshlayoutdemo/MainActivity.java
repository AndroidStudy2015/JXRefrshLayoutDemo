package com.widget.www.jxrefrshlayoutdemo;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.widget.www.jxrefeshlayout.JXRefreshLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SwipeRefreshLayout swipeRefreshLayout = new SwipeRefreshLayout(this);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        final JXRefreshLayout jxRefreshLayout = (JXRefreshLayout) findViewById(R.id.refrshLayout);


        jxRefreshLayout.setOnRefreshListener(new JXRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(MainActivity.this, "正在刷新", Toast.LENGTH_LONG).show();

                jxRefreshLayout.finishRefresh(3000);
            }
        });
    }
}
