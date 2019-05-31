package com.huanhong.checkrecyclerview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.TextView;

import com.huanhong.checkrecyclerview.recycler.CommonAdapter;
import com.huanhong.checkrecyclerview.recycler.SortRecyclerView;
import com.huanhong.checkrecyclerview.recycler.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<String> list = new ArrayList<>();
    private SortRecyclerView recyclerView;
    private TextView tv_test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for (int i = 0; i < 10; i++) {
            list.add(String.valueOf(i));
        }
        setContentView(R.layout.activity_main);
        tv_test = findViewById(R.id.tv_test);
        recyclerView = findViewById(R.id.sort);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setFlag(SortRecyclerView.Flag.TOP);  // 设置列表中Item滑动生效方向
        // 设置列表位置 上左 / 下右
        recyclerView.setOrientationFlag(SortRecyclerView.OrientationFlag.BOTTOM);
        // 设置监听  所有设置都需卸载adapter 前面
        recyclerView.setOnItemSelectListener(new SortRecyclerView.OnItemSelectListener() {
            @Override
            public void select(Object o) {
                if(o!=null){
                    String s = (String) o;
                    tv_test.setText(s);
                }
            }
        });
        recyclerView.setAdapter(new CommonAdapter<String>(this, list, R.layout.item_number) {
            @Override
            public void convert(ViewHolder holder, List<String> t) {
                TextView tv_number = holder.getView(R.id.tv_number);
                tv_number.setText(t.get(holder.getRealPosition()));
            }
        });


    }
}
