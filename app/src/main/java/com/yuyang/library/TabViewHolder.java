package com.yuyang.library;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.yuyang.library.nestedrv.ChildRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuyuhang on 2023/12/4.
 */
public class TabViewHolder extends RecyclerView.ViewHolder {

    private final TabLayout mTabLayout;
    private final ViewPager mViewPager;

    private final List<ChildRecyclerView> mViewList = new ArrayList<>();

    private ChildRecyclerView mCurrentChildRecyclerView;

    public TabViewHolder(@NonNull android.view.View itemView) {
        super(itemView);

        mTabLayout = itemView.findViewById(R.id.tab_layout);
        mViewPager = itemView.findViewById(R.id.view_pager);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if (!mViewList.isEmpty()) {
                    mCurrentChildRecyclerView = mViewList.get(i);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    public void bindData(List<String> tabs) {
        mViewList.clear();

        for (String str : tabs) {
            ChildRecyclerView childRecyclerView = new ChildRecyclerView(mViewPager.getContext());
            childRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            childRecyclerView.setAdapter(new ChildAdapter(str));
            childRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    super.getItemOffsets(outRect, view, parent, state);
                    outRect.left = outRect.right = outRect.bottom = outRect.top = 10;
                }
            });
            mViewList.add(childRecyclerView);
        }

        mCurrentChildRecyclerView = mViewList.get(mViewPager.getCurrentItem());
        int lastItem = mViewPager.getCurrentItem();
        mViewPager.setAdapter(new PagerAdapter() {

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                ChildRecyclerView childRecyclerView = mViewList.get(position);
                if (container == childRecyclerView.getParent()) {
                    container.removeView(childRecyclerView);
                }
                container.addView(childRecyclerView);
                return childRecyclerView;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                // super.destroyItem(container, position, object);
                container.removeView((View) object);
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return tabs.get(position);
            }

            @Override
            public int getCount() {
                return tabs.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
                return view == o;
            }
        });
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(lastItem);
    }

    public ChildRecyclerView getCurrentChildRecyclerView() {
        return mCurrentChildRecyclerView;
    }
}