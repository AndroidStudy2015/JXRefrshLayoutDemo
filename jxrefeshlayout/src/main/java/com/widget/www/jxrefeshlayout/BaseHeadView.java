package com.widget.www.jxrefeshlayout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public abstract class BaseHeadView extends FrameLayout {

    public BaseHeadView(@NonNull Context context) {
        super(context);
        initView(context);

    }

    public BaseHeadView( @NonNull Context context,  @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);

    }

    protected abstract void initView(Context context);
    public abstract void onPullDownRefresh(int pullDownHeight, int headerViewHeight);
    public abstract void onReleaseRefresh(int pullDownHeight, int headerViewHeight);
    public abstract void onRefreshing();
    public abstract void onRefreshFinish();
}
