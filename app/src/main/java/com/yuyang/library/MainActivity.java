package com.yuyang.library;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.yuyang.library.nested.MainActivityWithoutTab;
import com.yuyang.library.nestedrv.ParentRecyclerView;

import java.util.Arrays;

/**
 * 带Tab
 * <p>
 * Created by yuyuhang on 2023/12/4.
 */
public class MainActivity extends AppCompatActivity {

    private ParentRecyclerView mParentRecyclerView;

    private ParentAdapter mParentAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mParentRecyclerView = findViewById(R.id.parent);
        mParentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mParentRecyclerView.setAdapter(mParentAdapter = new ParentAdapter());

        mParentAdapter.setDataList(Arrays.asList(R.mipmap.p1, R.mipmap.p2, R.mipmap.p3));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_tab) {
            Intent intent = new Intent(this, MainActivityWithoutTab.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
