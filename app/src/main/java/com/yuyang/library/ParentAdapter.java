package com.yuyang.library;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.yuyang.library.nestedrv.ChildRecyclerView;
import com.yuyang.library.nestedrv.INestedParentAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yuyang on 2023/12/4.
 */
public class ParentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements INestedParentAdapter {

    private static final int TYPE_ITEM = 0;

    private static final int TYPE_INNER = 1;

    private List<Integer> dataList = new ArrayList<>();

    private List<String> tabs = Arrays.asList("推荐", "热点", "视频", "直播", "社会", "娱乐", "科技", "汽车", "体育", "财经", "军事", "国际", "时尚", "游戏", "旅游", "历史", "探索", "美食", "育儿", "养生", "故事", "美文");

    private TabViewHolder mTabViewHolder;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_ITEM) {
            ImageView imageView = new ImageView(viewGroup.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new RecyclerView.ViewHolder(imageView) {
            };
        }
        return new TabViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_inner, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == TYPE_ITEM) {
            ImageView imageView = (ImageView) viewHolder.itemView;
            Drawable drawable = ContextCompat.getDrawable(viewHolder.itemView.getContext(), dataList.get(viewHolder.getAdapterPosition()));
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            int targetHeight = viewHolder.itemView.getContext().getResources().getDisplayMetrics().widthPixels * height / width;
            ViewGroup.LayoutParams layoutParams = viewHolder.itemView.getLayoutParams();
            layoutParams.height = targetHeight;
            imageView.setImageDrawable(drawable);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "点击了第" + viewHolder.getAdapterPosition() + "个", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            mTabViewHolder = (TabViewHolder) viewHolder;
            mTabViewHolder.bindData(tabs);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position < dataList.size() ? TYPE_ITEM : TYPE_INNER;
    }

    @Override
    public ChildRecyclerView getCurrentChildRecyclerView() {
        return mTabViewHolder == null ? null : mTabViewHolder.getCurrentChildRecyclerView();
    }

    public void setDataList(List<Integer> dataList) {
        this.dataList.clear();
        if (dataList != null) {
            this.dataList.addAll(dataList);
        }
        notifyDataSetChanged();
    }


}
