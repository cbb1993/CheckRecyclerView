package com.huanhong.checkrecyclerview.recycler;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

/**
 * Created by 坎坎.
 * Date: 2019/5/30
 * Time: 16:35
 * describe:
 */
public class ShowViewGroup extends RelativeLayout {

    public enum ScaleType {WIDTH, HEIGHT}

    private View child;

    public ShowViewGroup(Context context) {
        super(context);
    }

    public ShowViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShowViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    float downX, downY;
    float moveX, moveY;
    private float oldX, oldY;
    private ScaleType scaleType = ScaleType.WIDTH;
    private SortRecyclerView sortRecyclerView;
    private boolean layout = false;

    protected void init() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (child == null && !layout) {
                    if (getChildCount() == 2) {
                        if (getChildAt(0) instanceof SortRecyclerView) {
                            child = getChildAt(1);
                            sortRecyclerView = (SortRecyclerView) getChildAt(0);
                        } else {
                            child = getChildAt(0);
                            sortRecyclerView = (SortRecyclerView) getChildAt(1);
                        }
                        oldX = child.getX();
                        oldY = child.getY();

                        if (sortRecyclerView != null) {
                            sortRecyclerView.setShowViewGroup(ShowViewGroup.this);
                            layout = true;
                        }
                    }
                }
            }
        });
    }


    private void anim() {
        if (child == null) {
            return;
        }
        child.setX(oldX);
        child.setY(oldY);
        child.setScaleY(1);
        child.setScaleX(1);
    }


    private boolean isInChild(MotionEvent ev) {
        if (child == null) {
            return false;
        }
        if (ev.getX() > child.getLeft()
                && ev.getX() < child.getRight()
                && ev.getY() > child.getTop()
                && ev.getY() < child.getBottom()) {
            return true;
        }
        return false;
    }

    private boolean press;

    public boolean isPress() {
        return press;
    }

    private boolean changed;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                press = isInChild(event);
                downX = event.getRawX();
                downY = event.getRawY();
                if (press && !changed && sortRecyclerView != null &&sortRecyclerView.getItemSize()!=null) {
                    SortRecyclerView.ItemSize itemSize = sortRecyclerView.getItemSize();
                    switch (scaleType) {
                        case WIDTH:
                            if (child.getWidth() > itemSize.getWight()) {
                                float f = ((float) itemSize.getWight()) / child.getWidth();
                                child.setScaleX(f);
                                child.setScaleY(f);
                                changed = true;
                            }
                            break;
                        case HEIGHT:
                            if (child.getHeight() > itemSize.getHeight()) {
                                float f = ((float) itemSize.getHeight()) / child.getHeight();
                                child.setScaleX(f);
                                child.setScaleY(f);
                                changed = true;
                            }
                            break;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (press) {
                    if (listener != null) {
                        listener.move(event);
                    }
                    moveX = event.getRawX();
                    moveY = event.getRawY();
                    child.setX(child.getX() + (moveX - downX));
                    child.setY(child.getY() + (moveY - downY));
                    downX = moveX;
                    downY = moveY;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (press) {
                    if (listener != null) {
                        listener.move(event);
                    }
                }
                press = false;
                changed = false;
                anim();
                break;
        }
        return super.dispatchTouchEvent(event);
    }


    private OnMoveListener listener;

    public void setOnMoveListener(OnMoveListener listener) {
        this.listener = listener;
    }

    interface OnMoveListener {
        void move(MotionEvent event);
    }

}
