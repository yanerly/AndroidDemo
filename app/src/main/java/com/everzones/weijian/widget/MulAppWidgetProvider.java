package com.everzones.weijian.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.everzones.weijian.MainActivity;
import com.everzones.weijian.R;
import com.everzones.weijian.service.ListViewService;

public class MulAppWidgetProvider extends AppWidgetProvider {
    public static final String CHANGE_IMAGE = "com.example.joy.action.CHANGE_IMAGE";

    private RemoteViews views;
    private ComponentName mComponentName;

    private int[] imgs = new int[]{
            R.drawable.a1,
            R.drawable.a2,
            R.drawable.a3,
            R.drawable.a4,
            R.drawable.a5,
            R.drawable.a6
    };

    /**
     根据 updatePeriodMillis 定义的定期刷新操作会调用该函数，此外当用户添加 Widget 时
     也会调用该函数，此方法一般处理widget的创建布局和更新UI操作

     在widget上给某个控件设置点击事件采用PendingIntent，通过new一个延时意图，然后remoteViews.setOnClickPendingIntent()来注册点击事件。
     更新布局可以获得用WidgetManager..updateAppWidget(thisWidget, remoteViews)来加载或更新widget布局，也可以通过onReceive()
     收到一个自定义的广播来调用此方法更新布局也可以。
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.e("test","----onUpdate------");
        // 获取Widget的组件名
        mComponentName = new ComponentName(context, MulAppWidgetProvider.class);

        // 创建一个RemoteView
        views = new RemoteViews(context.getPackageName(), R.layout.mul_app_widget_provider);
        views.setImageViewResource(R.id.iv_test, R.mipmap.great);
        views.setTextViewText(R.id.btn_test, "点击跳转");

        // Click button
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 200, mainIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_test, pi);

        // 把这个Widget绑定到RemoteViewsService
        Intent lvIntent = new Intent(context, ListViewService.class);
        lvIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[0]);
        // 给listview设置适配器
        views.setRemoteAdapter(R.id.lv_test, lvIntent);
        // 设置当显示的widget为空显示的View
        views.setEmptyView(R.id.lv_test,android.R.id.empty);

        // 点击列表触发事件
        Intent toIntent = new Intent(context,MulAppWidgetProvider.class);
        toIntent.setAction(CHANGE_IMAGE);
        toIntent.setData(Uri.parse(toIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 200, toIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.lv_test, pendingIntent);

        // 更新Wdiget
        appWidgetManager.updateAppWidget(mComponentName, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.e("test","----onReceive------");

        if(TextUtils.equals(CHANGE_IMAGE,intent.getAction())){
            Bundle extras = intent.getExtras();
            int position = extras.getInt(ListViewService.EXTRA_DATA);
            views = new RemoteViews(context.getPackageName(), R.layout.mul_app_widget_provider);
            views.setImageViewResource(R.id.iv_test, imgs[position]);
            mComponentName = new ComponentName(context, MulAppWidgetProvider.class);
            AppWidgetManager.getInstance(context).updateAppWidget(mComponentName, views);
        }
    }

    /*
    当 Widget 第一次被添加时调用，例如用户添加了两个你的 Widget，
    那么只有在添加第一个 Widget 时该方法会被调用。
    所以该方法比较适合执行你所有 Widgets 只需进行一次的操作
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.e("test","----onEnabled------");
    }

    /**
     * 与 onEnabled 恰好相反，当你的最后一个 Widget 被删除时调用该方法，所以这里用来清理之前在 onEnabled() 中进行的操作。
     * @param context
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.e("test","----onDisabled------");
    }

    //当 Widget 被删除时调用该方法
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.e("test","----onDeleted------");
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
        Log.e("test","----onRestored------");
    }

    //当 Widget 第一次被添加或者大小发生变化时调用该方法，可以在此控制 Widget 元素的显示和隐藏。
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        Log.e("test","----onAppWidgetOptionsChanged------");
    }

}
