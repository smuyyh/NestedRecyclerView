package com.yuyang.library.nested;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import com.yuyang.library.R;
import com.yuyang.library.nestedrv.ParentRecyclerView;

import java.util.Arrays;

/**
 * 不带Tab
 * <p>
 * Created by yuyang on 2023/12/4.
 */
public class MainActivityWithoutTab extends AppCompatActivity {

    private ParentRecyclerView mParentRecyclerView;

    private ParentAdapterWithoutTab mParentAdapterWithoutTab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mParentRecyclerView = findViewById(R.id.parent);
        mParentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mParentRecyclerView.setAdapter(mParentAdapterWithoutTab = new ParentAdapterWithoutTab());

        mParentAdapterWithoutTab.setDataList(Arrays.asList(R.mipmap.p1, R.mipmap.p2, R.mipmap.p3));
    }
}
