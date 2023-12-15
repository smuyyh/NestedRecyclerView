package com.yuyang.library;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by yuyang on 2023/12/14.
 */
public class ChildAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_VIEWPAGER = 0;
    private static final int TYPE_TEXTVIEW = 1;
    private static final int VIEWPAGER_ITEM_COUNT = 5;

    private final int[] colors = new int[]{
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.CYAN,
    };

    private String title;

    public ChildAdapter(String title) {
        this.title = title;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_VIEWPAGER;
        } else {
            return TYPE_TEXTVIEW;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_VIEWPAGER) {
            ViewPager viewPager = new ViewPager(viewGroup.getContext());
            viewPager.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600));
            return new RecyclerView.ViewHolder(viewPager) {
            };
        } else {
            TextView textView = new TextView(viewGroup.getContext());
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setPadding(100, 100, 100, 100);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setBackgroundColor(Color.WHITE);
            return new RecyclerView.ViewHolder(textView) {
            };
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (position == 0) {
            ((ViewPager) viewHolder.itemView).setAdapter(new PagerAdapter() {
                @Override
                public int getCount() {
                    return VIEWPAGER_ITEM_COUNT;
                }

                @Override
                public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                    return view == object;
                }

                @NonNull
                @Override
                public Object instantiateItem(@NonNull ViewGroup container, int position) {
                    TextView textView = new TextView(container.getContext());
                    textView.setGravity(Gravity.CENTER);
                    textView.setText(String.format("我是ViewPager的第%d个item", position));
                    textView.setBackgroundColor(colors[position]);

                    container.addView(textView);
                    return textView;
                }

                @Override
                public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                    container.removeView((View) object);
                }
            });
        } else {
            ((TextView) viewHolder.itemView).setText(title + " item " + position);
            ((TextView) viewHolder.itemView).setMinHeight((int) (200 + position * 1.1f));
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "click " + title + " item " + position, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return 100;
    }
}
