package com.yuyang.library.nestedrv;

/**
 * ParentAdapter 需实现此接口
 * <p>
 * Created by yuyang on 2023/12/4.
 */
public interface INestedParentAdapter {

    /**
     * 获取当前需要联动的子RecyclerView
     *
     * @return
     */
    ChildRecyclerView getCurrentChildRecyclerView();
}
