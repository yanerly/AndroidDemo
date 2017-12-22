package com.everzones.weijian.service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.everzones.weijian.widget.MulAppWidgetProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * RemoteViewsService，是管理RemoteViews的服务。一般，当AppWidget 中包含 GridView、ListView、StackView 等集合视图时，才需要使用RemoteViewsService来进行更新、管理
 */
public class ListViewService extends RemoteViewsService {
    public static final String EXTRA_DATA = "extra_data";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    // 通过 RemoteViewsFactory来具体管理layout中集合视图的
    private class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        private List<String> mList = new ArrayList<>();
        private Context mContext;
        private int mAppWidgetId;

        public ListRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        /*
        * ListRemoteViewsFactory调用时执行，这个方法执行时间超过20秒回报错。
        * 如果耗时长的任务应该在onDataSetChanged或者getViewAt中处理
        */
        @Override
        public void onCreate() {
            mList.add("一");
            mList.add("二");
            mList.add("三");
            mList.add("四");
            mList.add("五");
            mList.add("六");
        }

        /*
         * 当调用notifyAppWidgetViewDataChanged方法时，触发这个方法
         * 例如：MyRemoteViewsFactory.notifyAppWidgetViewDataChanged();
         */
        @Override
        public void onDataSetChanged() {
        }

        @Override
        public void onDestroy() {
            mList.clear();
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        /*
         * 创建并且填充，在指定索引位置显示的View，这个和BaseAdapter的getView类似
         */
        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews views = new RemoteViews(mContext.getPackageName(), android.R.layout.simple_list_item_1);
            views.setTextViewText(android.R.id.text1, "数据:" + mList.get(position));

            Intent changeIntent = new Intent();
            Bundle extras = new Bundle();
            extras.putInt(ListViewService.EXTRA_DATA, position);
            changeIntent.setAction(MulAppWidgetProvider.CHANGE_IMAGE);
            changeIntent.putExtras(extras);
            views.setOnClickFillInIntent(android.R.id.text1, changeIntent);
            return views;
        }

        /* 在更新界面的时候如果耗时就会显示 正在加载... 的默认字样，但是你可以更改这个界面
         * 如果返回null 显示默认界面
         * 否则 加载自定义的，返回RemoteViews
         */
        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
