package com.huanhong.checkrecyclerview.recycler;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by 坎坎.
 * Date: 2019/5/30
 * Time: 11:15
 * describe:
 */
public class SortRecyclerView extends RecyclerView {
    public enum Flag {TOP, LEFT, RIGHT, BOTTOM}

    public enum OrientationFlag {TOP, BOTTOM}

    private CommonAdapter adapter;
    private Flag flag = Flag.TOP;
    private int minDistance = 200;
    private boolean clear = false;
    private boolean press;
    private int position = -1;
    private Object cache;
    private ShowViewGroup showViewGroup;
    private OrientationFlag orientationFlag = OrientationFlag.BOTTOM;
    private boolean padding = false;
    private ItemSize itemSize ;

    public void setShowViewGroup(ShowViewGroup showViewGroup) {
        this.showViewGroup = showViewGroup;
        showViewGroup.setOnMoveListener(new ShowViewGroup.OnMoveListener() {
            @Override
            public void move(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int count = getChildCount();
                    List<ItemRect> list = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        View view = getChildAt(i);
                        list.add(new ItemRect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom()));
                    }
                    for (int i = 0; i < list.size(); i++) {
                        ItemRect rect = list.get(i);
                        if (event.getX() > rect.getLeft()
                                && event.getX() < rect.getRight()
                                && event.getY() > rect.getTop()
                                && event.getY() < rect.getBootom()) {
                            if (listener != null && adapter != null && i < adapter.getmDatas().size()) {
                                listener.select(adapter.getmDatas().get(i));
                                if (cache != null) {
                                    Object temp = adapter.getmDatas().get(i);
                                    adapter.getmDatas().set(i, cache);
                                    cache = temp;
                                    adapter.notifyItemChanged(i);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        });

    }

    public SortRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        super.setAdapter(adapter);
        this.adapter = (CommonAdapter) adapter;
        initFirst();
    }

    private void initFirst() {
        if (adapter != null && adapter.getmDatas().size() > 0) {
            Object o = adapter.getmDatas().remove(0);
            adapter.notifyItemRemoved(0);
            cache = o;
            if (listener != null) {
                listener.select(o);
            }
        }
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!padding) {
                    if (getChildCount() != 0) {
                        View view =getChildAt(0);
                        padding = true;
                        itemSize = new ItemSize(view.getWidth(),view.getHeight());
                        setRecyclerPadding(view);
                    }
                }

            }
        });
    }

    private void init() {
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public void onSelectedChanged(@Nullable ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (viewHolder != null) {
                    position = viewHolder.getAdapterPosition();
                }
            }

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder) {

                final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                final int swipeFlags = 0;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder, @NonNull ViewHolder target) {

                if (adapter == null) {
                    adapter = (CommonAdapter) getAdapter();
                }
                int fromPosition = viewHolder.getAdapterPosition();
                if (fromPosition == 0) {
                    return true;
                }
                //拿到当前拖拽到的item的viewHolder
                int toPosition = target.getAdapterPosition();
                if (fromPosition < toPosition) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        Collections.swap(adapter.getmDatas(), i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(adapter.getmDatas(), i, i - 1);
                    }
                }
                adapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                int distance = 0;
                if (!press) {
                    if (clear) {
                        if (adapter == null) {
                            adapter = (CommonAdapter) recyclerView.getAdapter();
                        }
                        if (position > -1 && position < adapter.getmDatas().size()) {
                            if (listener != null) {
                                listener.select(adapter.getmDatas().get(position));
                            }
                            Object o = adapter.getmDatas().remove(position);
                            if (cache != null) {
                                adapter.getmDatas().add(position, cache);
                                adapter.notifyItemChanged(position);
                            }
                            cache = o;
                            position = -1;
                            clear = false;
                        }
                    }
                } else {
                    clear = false;
                    switch (flag) {
                        case TOP:
                            if (dY < 0) {
                                distance = (int) dY;
                            }
                            break;
                        case LEFT:
                            if (dX < 0) {
                                distance = (int) dX;
                            }
                            break;
                        case RIGHT:
                            if (dX > 0) {
                                distance = (int) dX;
                            }
                            break;
                        case BOTTOM:
                            if (dY > 0) {
                                distance = (int) dY;
                            }
                            break;
                    }
                    distance = Math.abs(distance);
                    clear = distance > minDistance;
                }
            }

            @Override
            public void onSwiped(@NonNull ViewHolder viewHolder, int i) {
            }
        });

        mItemTouchHelper.attachToRecyclerView(this);
        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                press = true;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    press = false;
                }
                return false;
            }
        });
    }

    public ItemSize getItemSize() {
        return itemSize;
    }

    public void setOrientationFlag(OrientationFlag orientationFlag) {
        this.orientationFlag = orientationFlag;
    }

    private void setRecyclerPadding(View view) {
        LinearLayoutManager manager = (LinearLayoutManager) getLayoutManager();
        switch (manager.getOrientation()) {
            case LinearLayout
                    .HORIZONTAL:
                if (orientationFlag == OrientationFlag.BOTTOM) {
                    setPadding(0, getHeight() - 50 - view.getHeight(), 0, 0);
                }
                break;
            case LinearLayout
                    .VERTICAL:
                if (orientationFlag == OrientationFlag.BOTTOM) {
                    setPadding(getWidth() - 50 - view.getWidth(), 0, 0, 0);
                }
                break;

        }
    }

    public void setFlag(Flag flag) {
        this.flag = flag;
    }

    private OnItemSelectListener listener;

    public void setOnItemSelectListener(OnItemSelectListener listener) {
        this.listener = listener;
    }

    public interface OnItemSelectListener {
        void select(Object o);
    }

    static class ItemRect {
        int left, top, right, bootom;

        public ItemRect(int left, int top, int right, int bootom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bootom = bootom;
        }

        public int getLeft() {
            return left;
        }

        public int getTop() {
            return top;
        }

        public int getRight() {
            return right;
        }

        public int getBootom() {
            return bootom;
        }

        @Override
        public String toString() {
            return "ItemRect{" +
                    "left=" + left +
                    ", top=" + top +
                    ", right=" + right +
                    ", bootom=" + bootom +
                    '}';
        }
    }

    static class ItemSize {
        private int wight;
        private int height;

        public ItemSize(int wight, int height) {
            this.wight = wight;
            this.height = height;
        }

        public int getWight() {
            return wight;
        }

        public int getHeight() {
            return height;
        }

        @Override
        public String toString() {
            return "ItemSize{" +
                    "wight=" + wight +
                    ", height=" + height +
                    '}';
        }
    }
}
