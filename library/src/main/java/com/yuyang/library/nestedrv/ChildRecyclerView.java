package com.yuyang.library.nestedrv;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;

/**
 * 子RecyclerView
 * <p>
 * Created by yuyuhang on 2023/12/4.
 */
public class ChildRecyclerView extends RecyclerView {

    private ParentRecyclerView mParentRecyclerView = null;

    /**
     * fling时的加速度
     */
    private int mVelocity = 0;

    /**
     * 是否开始fling
     */
    private boolean isStartFling = false;

    private int mLastXInterceptX;
    private int mLastYInterceptY;

    public ChildRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public ChildRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChildRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOverScrollMode(OVER_SCROLL_NEVER);

        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    dispatchParentFling();
                }
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isStartFling) {
                    isStartFling = false;
                }
            }
        });
    }

    private void dispatchParentFling() {
        findParentRecyclerView();
        if (mParentRecyclerView != null && isScrollTop() && mVelocity != 0) {
            float velocityY = NestedOverScroller.invokeCurrentVelocity(this);
            if (Math.abs(velocityY) <= 2.0E-5F) {
                velocityY = (float) this.mVelocity * 0.5F;
            } else {
                velocityY *= 0.65F;
            }
            mParentRecyclerView.fling(0, (int) velocityY);
            mVelocity = 0;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev != null && ev.getAction() == MotionEvent.ACTION_DOWN) {
            mVelocity = 0;
        }

        int x = (int) ev.getRawX();
        int y = (int) ev.getRawY();
        if (ev.getAction() != MotionEvent.ACTION_MOVE) {
            mLastXInterceptX = x;
            mLastYInterceptY = y;
        }

        int deltaX = x - mLastXInterceptX;
        int deltaY = y - mLastYInterceptY;

        if (isScrollTop()) {
            getParent().requestDisallowInterceptTouchEvent(false);
        } else {
            getParent().requestDisallowInterceptTouchEvent(Math.abs(deltaX) <= Math.abs(deltaY));
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        if (!isAttachedToWindow()) return false;
        boolean fling = super.fling(velocityX, velocityY);
        if (!fling || velocityY >= 0) {
            mVelocity = 0;
        } else {
            isStartFling = true;
            mVelocity = velocityY;
        }
        return fling;
    }

    public boolean isScrollTop() {
        return !canScrollVertically(-1);
    }

    private ParentRecyclerView findParentRecyclerView() {
        if (mParentRecyclerView == null) {
            ViewParent parentView = getParent();
            while (!(parentView instanceof ParentRecyclerView)) {
                parentView = parentView.getParent();
            }
            mParentRecyclerView = (ParentRecyclerView) parentView;
        }
        return mParentRecyclerView;
    }
}
