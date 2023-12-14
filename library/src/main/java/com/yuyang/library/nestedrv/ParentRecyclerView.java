package com.yuyang.library.nestedrv;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
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

    private float mLastTouchY = 0f;

    private int mLastInterceptX;
    private int mLastInterceptY;

    /**
     * 用于向子容器传递 fling 速度
     */
    private final VelocityTracker mVelocityTracker = VelocityTracker.obtain();
    private int mMaximumFlingVelocity;
    private int mMinimumFlingVelocity;

    /**
     * 子容器是否消耗了滑动事件
     */
    private boolean childConsumeTouch = false;
    /**
     * 子容器消耗的滑动距离
     */
    private int childConsumeDistance = 0;

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
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();

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
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mVelocity = 0;
                mLastTouchY = ev.getRawY();
                childConsumeTouch = false;
                childConsumeDistance = 0;

                ChildRecyclerView childRecyclerView = findNestedScrollingChildRecyclerView();
                if (isScrollToBottom() && (childRecyclerView != null && !childRecyclerView.isScrollToTop())) {
                    stopScroll();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                childConsumeTouch = false;
                childConsumeDistance = 0;
                break;
            default:
                break;
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
        if (isChildConsumeTouch(event)) {
            // 子容器如果消费了触摸事件，后续父容器就无法再拦截事件
            // 在必要的时候，子容器需调用 requestDisallowInterceptTouchEvent(false) 来允许父容器继续拦截事件
            return false;
        }
        // 子容器不消费触摸事件，父容器按正常流程处理
        return super.onInterceptTouchEvent(event);
    }

    /**
     * 子容器是否消费触摸事件
     */
    private boolean isChildConsumeTouch(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        if (event.getAction() != MotionEvent.ACTION_MOVE) {
            mLastInterceptX = x;
            mLastInterceptY = y;
            return false;
        }
        int deltaX = x - mLastInterceptX;
        int deltaY = y - mLastInterceptY;
        if (Math.abs(deltaX) > Math.abs(deltaY) || Math.abs(deltaY) <= mTouchSlop) {
            return false;
        }

        return shouldChildScroll(deltaY);
    }

    /**
     * 子容器是否需要消费滚动事件
     */
    private boolean shouldChildScroll(int deltaY) {
        ChildRecyclerView childRecyclerView = findNestedScrollingChildRecyclerView();
        if (childRecyclerView == null) {
            return false;
        }
        if (isScrollToBottom()) {
            // 父容器已经滚动到底部 且 向下滑动 且 子容器还没滚动到底部
            return deltaY < 0 && !childRecyclerView.isScrollToBottom();
        } else {
            // 父容器还没滚动到底部 且 向上滑动 且 子容器已经滚动到顶部
            return deltaY > 0 && !childRecyclerView.isScrollToTop();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (isScrollToBottom()) {
            // 如果父容器已经滚动到底部，且向上滑动，且子容器还没滚动到顶部，事件传递给子容器
            ChildRecyclerView childRecyclerView = findNestedScrollingChildRecyclerView();
            if (childRecyclerView != null) {
                int deltaY = (int) (mLastTouchY - e.getRawY());
                if (deltaY >= 0 || !childRecyclerView.isScrollToTop()) {
                    mVelocityTracker.addMovement(e);
                    if (e.getAction() == MotionEvent.ACTION_UP) {
                        // 传递剩余 fling 速度
                        mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                        float velocityY = mVelocityTracker.getYVelocity();
                        if (Math.abs(velocityY) > mMinimumFlingVelocity) {
                            childRecyclerView.fling(0, -(int) velocityY);
                        }
                        mVelocityTracker.clear();
                    } else {
                        // 传递滑动事件
                        childRecyclerView.scrollBy(0, deltaY);
                    }

                    childConsumeDistance += deltaY;
                    mLastTouchY = e.getRawY();
                    childConsumeTouch = true;
                    return true;
                }
            }
        }

        mLastTouchY = e.getRawY();

        if (childConsumeTouch) {
            // 在同一个事件序列中，子容器消耗了部分滑动距离，需要扣除掉
            MotionEvent adjustedEvent = MotionEvent.obtain(
                    e.getDownTime(),
                    e.getEventTime(),
                    e.getAction(),
                    e.getX(),
                    e.getY() + childConsumeDistance, // 更新Y坐标
                    e.getMetaState()
            );

            boolean handled = super.onTouchEvent(adjustedEvent);
            adjustedEvent.recycle();
            return handled;
        }

        if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL) {
            mVelocityTracker.clear();
        }

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
        // 父容器滚动到底部后，如果还有剩余加速度，传递给子容器
        if (isScrollToBottom() && mVelocity != 0) {
            // 尽量让速度传递更加平滑
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

    public boolean isScrollToTop() {
        return !canScrollVertically(-1);
    }

    @Override
    public void scrollToPosition(final int position) {
        if (position == 0) {
            // 父容器滚动到顶部，从交互上来说子容器也需要滚动到顶部
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
        boolean isChildCanNotScroll = dy < 0 && (childRecyclerView == null || childRecyclerView.isScrollToTop());
        if (isParentCanScroll || isChildCanNotScroll) {
            smoothScrollBy(0, dy);
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
        // 1、当前ParentRecyclerView没有滑动底，且向下滑动，即下滑
        boolean isParentCanFling = velocityY > 0f && !isScrollToBottom();
        // 2、当前ChildRecyclerView滑到顶部了，且向上滑动，即上滑
        boolean isChildCanNotFling = velocityY < 0 && (childRecyclerView == null || childRecyclerView.isScrollToTop());

        if (!isParentCanFling && !isChildCanNotFling) {
            return false;
        }
        fling(0, (int) velocityY);
        return true;
    }
}
