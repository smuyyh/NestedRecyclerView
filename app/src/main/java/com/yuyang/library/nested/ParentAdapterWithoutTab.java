package com.yuyang.library.nested;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.yuyang.library.ChildAdapter;
import com.yuyang.library.nestedrv.ChildRecyclerView;
import com.yuyang.library.nestedrv.INestedParentAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuyang on 2023/12/4.
 */
public class ParentAdapterWithoutTab extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements INestedParentAdapter {

    private static final int TYPE_ITEM = 0;

    private static final int TYPE_INNER = 1;

    private List<Integer> dataList = new ArrayList<>();

    private ChildRecyclerView childRecyclerView;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_ITEM) {
            ImageView imageView = new ImageView(viewGroup.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new RecyclerView.ViewHolder(imageView) {
            };
        }

        if (childRecyclerView == null) {
            childRecyclerView = new ChildRecyclerView(viewGroup.getContext());
            childRecyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            if (childRecyclerView.getParent() != null) {
                ((ViewGroup) childRecyclerView.getParent()).removeView(childRecyclerView);
            }
        }
        return new RecyclerView.ViewHolder(childRecyclerView) {
        };
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
            childRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            childRecyclerView.setAdapter(new ChildAdapter("默认"));
            childRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    super.getItemOffsets(outRect, view, parent, state);
                    outRect.left = outRect.right = outRect.bottom = outRect.top = 10;
                }
            });
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
        return childRecyclerView;
    }

    public void setDataList(List<Integer> dataList) {
        this.dataList.clear();
        if (dataList != null) {
            this.dataList.addAll(dataList);
        }
        notifyDataSetChanged();
    }

}
