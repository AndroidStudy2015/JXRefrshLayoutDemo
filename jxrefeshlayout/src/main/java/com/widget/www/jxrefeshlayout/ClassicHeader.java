package com.widget.www.jxrefeshlayout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ClassicHeader extends BaseHeadView {

    private View mView;
    private TextView mTv;
    private ImageView mIv;
    private LinearLayout mLl;

    public ClassicHeader(@NonNull Context context) {
        super(context);
    }

    public ClassicHeader(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void initView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        mView = inflater.inflate(R.layout.view_header, this, true);
        mLl = (LinearLayout) mView.findViewById(R.id.ll);
        mTv = (TextView) mView.findViewById(R.id.tv);
        mIv = (ImageView) mView.findViewById(R.id.iv);
    }
  float  mFloat = 0.7f;

    @Override
    public void onPullDownRefresh(int pullDownHeight, int headerViewHeight) {
        mTv.setText(pullDownHeight + "下拉刷新");
    }

    @Override
    public void onReleaseRefresh(int pullDownHeight, int headerViewHeight) {
        mTv.setText(pullDownHeight + "松手以刷新");

    }

    @Override
    public void onRefreshing() {
        mTv.setText("正在刷新");
    }

    @Override
    public void onRefreshFinish() {

    }
}
