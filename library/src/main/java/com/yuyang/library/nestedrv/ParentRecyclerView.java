package com.yuyang.library.nestedrv;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * 父RecyclerView
 * <p>
 * Created by yuyuhang on 2023/12/4.
 */
public class ParentRecyclerView extends RecyclerView {

    private final int mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    /**
     * fling时的加速度
     */
    private int mVelocity = 0;

    private float lastY = 0f;

    private int mLastXInterceptX;
    private int mLastYInterceptY;

    public ParentRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public ParentRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ParentRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == SCROLL_STATE_IDLE) {
                    dispatchChildFling();
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev != null && ev.getAction() == MotionEvent.ACTION_DOWN) {
            mVelocity = 0;
            stopScroll();
        }
        if (!(ev == null || ev.getAction() == MotionEvent.ACTION_MOVE)) {
            lastY = 0f;
        }
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean res = false;
        if (!isChildConsumeTouch(event)) {
            res = super.onInterceptTouchEvent(event);
        }
        return res;
    }

    private boolean isChildConsumeTouch(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        if (event.getAction() != MotionEvent.ACTION_MOVE) {
            mLastXInterceptX = x;
            mLastYInterceptY = y;
            return false;
        }
        int deltaX = x - mLastXInterceptX;
        int deltaY = y - mLastYInterceptY;
        if (Math.abs(deltaX) <= Math.abs(deltaY) || Math.abs(deltaY) <= mTouchSlop) {
            return false;
        }
        return shouldChildScroll(deltaY);
    }

    private boolean shouldChildScroll(int deltaY) {
        ChildRecyclerView childRecyclerView = findNestedScrollingChildRecyclerView();
        if (childRecyclerView == null) {
            return false;
        }
        if (isScrollToBottom()) {
            if (deltaY > 0) {
                return false;
            } else if (deltaY < 0 && !childRecyclerView.isScrollTop()) {
                return true;
            }
        } else {
            if (deltaY > 0 && !childRecyclerView.isScrollTop()) {
                return true;
            } else if (deltaY < 0) {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (lastY == 0f) {
            lastY = e.getRawY();
        }
        if (isScrollToBottom()) {
            ChildRecyclerView childRecyclerView = findNestedScrollingChildRecyclerView();
            if (childRecyclerView != null) {
                int deltaY = (int) (lastY - e.getRawY());
                if (deltaY >= 0 || !childRecyclerView.isScrollTop()) {
                    childRecyclerView.scrollBy(0, deltaY);
                    lastY = e.getRawY();
                    return true;
                }
            }
        }
        lastY = e.getRawY();

        try {
            return super.onTouchEvent(e);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean fling(int velX, int velY) {
        boolean fling = super.fling(velX, velY);
        if (!fling || velY <= 0) {
            mVelocity = 0;
        } else {
            mVelocity = velY;
        }
        return fling;
    }

    private void dispatchChildFling() {
        if (isScrollToBottom() && mVelocity != 0) {
            float mVelocity = NestedOverScroller.invokeCurrentVelocity(this);
            if (Math.abs(mVelocity) <= 2.0E-5F) {
                mVelocity = (float) this.mVelocity * 0.5F;
            } else {
                mVelocity *= 0.46F;
            }
            ChildRecyclerView childRecyclerView = findNestedScrollingChildRecyclerView();
            if (childRecyclerView != null) {
                childRecyclerView.fling(0, (int) mVelocity);
            }
        }
        mVelocity = 0;
    }

    public ChildRecyclerView findNestedScrollingChildRecyclerView() {
        if (getAdapter() instanceof INestedParentAdapter) {
            return ((INestedParentAdapter) getAdapter()).getCurrentChildRecyclerView();
        }
        return null;
    }

    public boolean isScrollToBottom() {
        return !canScrollVertically(1);
    }

    @Override
    public void scrollToPosition(final int position) {
        if (position == 0) {
            ChildRecyclerView childRecyclerView = findNestedScrollingChildRecyclerView();
            if (childRecyclerView != null) {
                childRecyclerView.scrollToPosition(0);
            }
        }

        super.scrollToPosition(position);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return target instanceof ChildRecyclerView;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        ChildRecyclerView childRecyclerView = findNestedScrollingChildRecyclerView();
        //1、当前ParentRecyclerView没有滑动底，且dy> 0，即下滑
        boolean isParentCanScroll = dy > 0 && !isScrollToBottom();
        //2、当前ChildRecyclerView滑到顶部了，且dy < 0，即上滑
        boolean isChildCanNotScroll = !(dy >= 0 || childRecyclerView == null || !childRecyclerView.isScrollTop());
        if (isParentCanScroll || isChildCanNotScroll) {
            scrollBy(0, dy);
            consumed[1] = dy;
        }
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return true;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        ChildRecyclerView childRecyclerView = findNestedScrollingChildRecyclerView();
        boolean isParentCanFling = velocityY > 0f && !isScrollToBottom();
        boolean isChildCanNotFling = !(velocityY >= 0
                || childRecyclerView == null
                || !childRecyclerView.isScrollTop());

        if (!isParentCanFling && !isChildCanNotFling) {
            return false;
        }
        fling(0, (int) velocityY);
        return true;
    }
}
