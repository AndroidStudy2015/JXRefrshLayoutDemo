package com.widget.www.jxrefeshlayout;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;


public class JXRefreshLayoutTwoFloor extends LinearLayout {

    private LayoutInflater mInflater;
    private Scroller mScroller;

    /**
     * 刷新头
     */
    private BaseHeadView mHeaderView;
    /**
     * 一般为RecyclerView、ListView、Scrollview、Nestscrollview
     */
    private View mBodyView;

    /**
     * 在onInterceptTouchEvent里记录的Y坐标，用于判断手指滑动情况
     */
    private int mLastInterceptY;
    /**
     * 在onTouchEvent里记录的Y坐标，用于判断手指滑动情况
     */
    int mLastY;

    /**
     * 刷新头布局高度
     */
    private int mHeaderViewHeight;
    /**
     * 刷新头布局的LayoutParams
     */
    private MarginLayoutParams mHeadViewLp;

    /**
     * 最大下拉高度
     */
    private int mMaxPullDownHeight;
    /**
     * 当向下拉出了整个刷新头布局，松手时，触发这个Runnable，
     * 要延迟mReleaseToRefreshAnimationDuration毫秒执行，
     */
    private Runnable mStartRefreshAction;
    /**
     * 刷新完成隐藏刷新头的动画 执行时长
     */
    int mRefreshFinishToHideHeaderAnimationDuration = 200;
    /**
     * 松手后回弹动画 执行时长
     */
    int mReleaseToRefreshAnimationDuration = 250;


    public JXRefreshLayoutTwoFloor(Context context) {
        super(context);
        init(context);
    }

    public JXRefreshLayoutTwoFloor(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);


    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        mInflater = LayoutInflater.from(context);
        mScroller = new Scroller(context, new LinearInterpolator());
        postDelayed(new Runnable() {
            @Override
            public void run() {
                addHeaderView();

            }
        }, 100);

        mStartRefreshAction = new Runnable() {
            @Override
            public void run() {
                if (mOnRefreshListener != null) {
                    isRefreshing = true;
                    mOnRefreshListener.onRefresh();
                    mHeaderView.onRefreshing();
                }
            }
        };
    }

    /**
     * 是不是正在刷新，mStartRefreshAction里置为true，调用finishRefresh时候isRefreshing=fasle
     */
    boolean isRefreshing;
    /**
     * 是不是完成了刷新操作，调用finishRefresh时候isRefreshing=true,刷新完成隐藏刷新头之后，重新置为false
     */
    boolean isRefreshFinish;

    public void addHeaderView() {
        if (getChildAt(0) instanceof BaseHeadView) {
            mHeaderView = (BaseHeadView) getChildAt(0);
            mHeaderView.setVisibility(GONE);
            mHeadViewLp = (MarginLayoutParams) mHeaderView.getLayoutParams();//必须先把布局添加到父布局，getLayoutParams才不会为null
            mHeaderViewHeight = getViewHeight(mHeaderView);
            mMaxPullDownHeight = mHeaderViewHeight * 5;
            mHeadViewLp.topMargin = -mHeaderViewHeight;
            mHeaderView.setLayoutParams(mHeadViewLp);
            mHeaderView.setVisibility(VISIBLE);
        } else {
            throw new RuntimeException("====***+++JXRefreshLayout的第一个子View必须是BaseHeadView的子类+++***====");
        }
    }


    /**
     * 这个获取view宽高的方法，只适合于wrap_content，和具体数值的情况（例如100dp）
     * 不支持布局里使用match_parent的情况(严格讲，如果父布局为match_parent，子布局有明确的尺寸值（例如100dp），也是可以测量出来的)
     * 详情见《android开发艺术探索》第191页码
     *
     * @param view
     * @return
     */
    private int getViewHeight(View view) {
        view.measure(0, 0);
        return view.getMeasuredHeight();
    }


    /**
     * 他只负责拦截事件
     * 不要在onInterceptTouchEvent里处理触摸事件，因为不是每次move事件都会调用该方法
     * 会导致你的事件滑动间断
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {


        boolean intercepted = false;
        if (getChildCount() >= 2 && mBodyView == null) {
            mBodyView = getChildAt(1);
        }


        int y = (int) ev.getRawY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                intercepted = false;
                if (!mScroller.isFinished()) {//如果有弹性动画在执行,那么在让这个动画在当前终止
                    mScroller.abortAnimation();
                    if (mStartRefreshAction != null) {
                        //手指再次触摸屏幕时，移除开始刷新的Runnable
                        removeCallbacks(mStartRefreshAction);
                    }
                    intercepted = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mBodyView == null) {
                    break;
                }

                if (!mBodyView.canScrollVertically(-1) && y - mLastInterceptY > 0) {
                    intercepted = true;
                } else {
                    intercepted = false;
                }
                break;

            case MotionEvent.ACTION_UP:
                intercepted = false;
                break;
            default:

                break;
        }
        mLastInterceptY = y;
        isFirst = true;

        return intercepted;
    }

    /**
     * JXRefreshLayout这个布局是不是第一次接受到触摸事件
     * 1. 当子view的触摸事件被XRefreshLayout拦截后，XRefreshLayout会第一次接收到触摸事件
     * --->所以在onInterceptTouchEvent里要： isFirst = true;
     * 2. 当直接触摸XRefreshLayout时候的down事件，XRefreshLayout会第一次接收到触摸事件
     * --->所以onTouchEvent的down事件里要：  isFirst = true;
     * <p>
     * 记得：在onTouchEvent的move事件里第一次验证之后要： mLastY = moveY;isFirst = false;
     * <p>
     * 为什么要有这个标识符呢
     * 因为JXRefreshLayout通过拦截子view第一次收到事件后（这时候不会走JXRefreshLayout的onTouchEvent的down事件，所以mLastY=0），
     * 需要mLastY = moveY，不然mLastY为0，你的手指的moveY会很大，导致布局位移突变
     */
    boolean isFirst = true;


    /**
     * 当本布局得到事件时候，事件消费在这里处理
     *
     * @param event
     * @return
     */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int moveY = 0;
        switch (event.getAction()) {

//      注意：当事件从子view转接到本view时，这个本view的down事件不执行
            case MotionEvent.ACTION_DOWN:
                mLastY = (int) event.getRawY();
                isFirst = true;
                break;

            case MotionEvent.ACTION_MOVE:
                moveY = (int) event.getRawY();
                if (isFirst || mLastY == 0) {
                    //其实只用mLastY==0就可以解决isFirst所解决的突变问题，
                    // 他还能解决多指触摸带来的突变问题（但是这个方法不够彻底，只是讨巧了）
                    mLastY = moveY;
                    isFirst = false;
                }

//               1. 计算阻尼系数
//                随着下拉高度越来越大，下拉的阻尼度越来越大，将下拉高度换算为90°~0°的数值所对应的弧度（因为Math.cos的参数单位为弧度）
//                下拉高度为0px---->0°----->余弦为1
//                下拉高度为mMaxPullDownHeight px---->90°---->余弦为0
                double damping = -(getScrollY() / (mMaxPullDownHeight / 90) * Math.PI / 180);
//                阻尼比（符合余弦函数）
                float dampingRatio = (float) (Math.cos(damping) * 0.75);
                int deltaY = (int) ((moveY - mLastY) * dampingRatio);


                //  2.处理边界情况，完成滑动

//                上边界：下拉距离-deltaY超出了最大下拉高度，且手指继续下滑
                boolean isTopBoundary = getScrollY() - deltaY < -mMaxPullDownHeight && deltaY > 0;

//                下边界：上拉距离-deltaY回到了原始状态，且手指继续上滑
                boolean isBottomBoundary = getScrollY() - deltaY >= 0 && deltaY <= 0;

                if (isTopBoundary) {
                    scrollTo(0, -mMaxPullDownHeight);
                } else if (isBottomBoundary) {
                    scrollTo(0, 0);
                } else {
                    scrollBy(0, -deltaY);
                }

//                mHeadViewLp.topMargin += deltaY;
//                mHeaderView.setLayoutParams(mHeadViewLp);


                if (!isRefreshing && !isRefreshFinish) {
                    //当不是正在刷新，也不是刷新完成时，才会有松手刷新或者下拉刷新的操作
                    if (getScrollY() < -mHeaderViewHeight) {
                        mHeaderView.onReleaseRefresh(-getScrollY(),mHeaderViewHeight);
                    } else {
                        mHeaderView.onPullDownRefresh(-getScrollY(),mHeaderViewHeight);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
//                mLastY = moveY;
                //处理回弹效果


                if (getScrollY() < -mHeaderViewHeight*1.5){
                    smoothScrollTo(0, -mHeaderViewHeight*7, mReleaseToRefreshAnimationDuration);

                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            smoothScrollTo(0, 0, mReleaseToRefreshAnimationDuration);

                        }
                    },3000);
                }                  else

                if (getScrollY() < -mHeaderViewHeight) {
                    smoothScrollTo(0, -mHeaderViewHeight, mReleaseToRefreshAnimationDuration);
                    postDelayed(mStartRefreshAction, mReleaseToRefreshAnimationDuration);

                }
                else {
                    smoothScrollTo(0, 0, mReleaseToRefreshAnimationDuration);
                }



                break;
            default:
                break;
        }

        mLastY = moveY;
        return true;
    }


    /**
     * 延时n毫秒后，执行刷新完成操作
     * 不得已使用了很多postDelayed，因为Scroller的弹性滑动无法监听中间过程和结束时间点，但是Scroller有一个弹性滑动的时长
     * 所以利用时长，来监听到动画的结束时机
     * 建议需要监听的话，不要使用Scroller，用属性动画代替实现弹性滑动
     *
     * @param delayMillis 延时毫秒数
     */
    public void finishRefresh(int delayMillis) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                isRefreshing = false;
                isRefreshFinish = true;
                smoothScrollTo(0, 0, mRefreshFinishToHideHeaderAnimationDuration);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isRefreshFinish = false;
                        mHeaderView.onRefreshFinish();
                    }
                }, mRefreshFinishToHideHeaderAnimationDuration);

            }
        }, delayMillis);
    }

    /**
     * duration毫秒内弹性滚动到目的地destX，destY
     *
     * @param destX
     * @param destY
     * @param duration
     */
    private void smoothScrollTo(int destX, int destY, int duration) {

        int scrollX = getScrollX();
        int scrollY = getScrollY();

        int deltaX = destX - scrollX;
        int deltaY = destY - scrollY;

        mScroller.startScroll(scrollX, scrollY, deltaX, deltaY, duration);
        invalidate();
    }

    @Override
    public void computeScroll() {

        if (mScroller.computeScrollOffset()) {
            Log.e("ccc----", getScrollY() + "---" + mHeaderViewHeight);
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();

        }


    }


    private OnRefreshListener mOnRefreshListener;

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    public interface OnRefreshListener {
        void onRefresh();
    }


}
