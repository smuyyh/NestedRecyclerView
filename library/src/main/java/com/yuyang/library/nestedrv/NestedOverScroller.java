package com.yuyang.library.nestedrv;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.lang.reflect.Field;

/**
 * 平滑滚动效果的辅助类
 * <p>
 * <p>
 * Created by yuyuhang on 2023/12/4.
 */
public class NestedOverScroller {

    public float invokeCurrentVelocity(@NonNull RecyclerView rv) {
        try {
            Field viewFlinger = null;
            for (Class<?> superClass = rv.getClass().getSuperclass(); superClass != null; superClass = superClass.getSuperclass()) {
                try {
                    viewFlinger = superClass.getDeclaredField("mViewFlinger");
                    break;
                } catch (Throwable ignored) {
                }
            }

            if (viewFlinger == null) {
                return 0.0F;
            } else {
                viewFlinger.setAccessible(true);
                Object viewFlingerValue = viewFlinger.get(rv);
                Field scroller = viewFlingerValue.getClass().getDeclaredField("mScroller");
                scroller.setAccessible(true);
                Object scrollerValue = scroller.get(viewFlingerValue);
                Field scrollerY = scrollerValue.getClass().getDeclaredField("mScrollerY");
                scrollerY.setAccessible(true);
                Object scrollerYValue = scrollerY.get(scrollerValue);
                Field currVelocity = scrollerYValue.getClass().getDeclaredField("mCurrVelocity");
                currVelocity.setAccessible(true);
                return (Float) currVelocity.get(scrollerYValue);
            }
        } catch (Throwable ignored) {
            return 0.0F;
        }
    }
}
