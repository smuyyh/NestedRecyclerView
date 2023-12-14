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

    private int mLastInterceptX;

    private int mLastInterceptY;

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
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == SCROLL_STATE_IDLE) {
                    dispatchParentFling();
                }
            }
        });
    }

    private void dispatchParentFling() {
        ensureParentRecyclerView();
        // 子容器滚动到顶部，如果还有剩余加速度，就交给父容器处理
        if (mParentRecyclerView != null && isScrollToTop() && mVelocity != 0) {
            // 尽量让速度传递更加平滑
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
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mVelocity = 0;
        }

        int x = (int) ev.getRawX();
        int y = (int) ev.getRawY();
        if (ev.getAction() != MotionEvent.ACTION_MOVE) {
            mLastInterceptX = x;
            mLastInterceptY = y;
        }

        int deltaX = x - mLastInterceptX;
        int deltaY = y - mLastInterceptY;

        if (isScrollToTop() && Math.abs(deltaX) <= Math.abs(deltaY) && getParent() != null) {
            // 子容器滚动到顶部，继续向上滑动，此时父容器需要继续拦截事件。与父容器 onInterceptTouchEvent 对应
            getParent().requestDisallowInterceptTouchEvent(false);
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
            mVelocity = velocityY;
        }
        return fling;
    }

    public boolean isScrollToTop() {
        return !canScrollVertically(-1);
    }

    public boolean isScrollToBottom() {
        return !canScrollVertically(1);
    }

    private void ensureParentRecyclerView() {
        if (mParentRecyclerView == null) {
            ViewParent parentView = getParent();
            while (!(parentView instanceof ParentRecyclerView)) {
                parentView = parentView.getParent();
            }
            mParentRecyclerView = (ParentRecyclerView) parentView;
        }
    }
}
