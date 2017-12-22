##### 1.写一个类 TestWidgetProvider 继承 AppWidgetProvider，因为AppWidgetProvider是继承BroadCastReceiver，所以要在manifest中注册自定义的类。
```java
 <receiver android:name=".TestWidgetProvider">
            <intent-filter>
				<!--这个action必须要有，且不能更改，属于系统规范，是作为小部件的标识而存在的-->
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
				<!--接收到的广播-->
                <action android:name="com.everzones.widget2.CLICK"/>
            </intent-filter>

            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/test_widget_provider_info" />
 </receiver>
```
##### 2.创建TestWidgetProvider后会自动在res/xml中生成文件：test_widget_provider_info.xml，用来配置微件的基本信息：
```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:initialKeyguardLayout="@layout/widget_layout"
    android:initialLayout="@layout/widget_layout"
    android:minHeight="40dp"
    android:minWidth="40dp"
    android:previewImage="@drawable/example_appwidget_preview"
    android:resizeMode="horizontal|vertical"
    android:updatePeriodMillis="0"
    android:widgetCategory="home_screen"/>
```

- minwidth和minheight：表示桌面上微件的最小宽度和高度,计算公式：70n-30(n是网格大小，1代表1格)
- previewImage：当用户选择添加 Widget 时的预览图片,该属性是在 3.0 以后引入的
- initialLayout：Widget 的布局 Layout 文件。
- configure：定义了用户在添加 Widget 时弹出的配置页面的 Activity，用户可以在此进行 Widget 的一些配置，该 Activity 是可选的，如果不需要可以不进行声明。
- resizeMode：Widget 在水平和垂直方向是否可以调整大小，值可以为：horizontal（水平方向可以调整大小），vertical（垂直方向可以调整大小），none（不可以调整大小），也可以 horizontal|vertical 组合表示水平和垂直方向均可以调整大小。
- widgetCategory：表示 Widget 可以显示的位置，包括 home_screen（桌面），keyboard（锁屏），keyboard 属性需要 5.0 或以上 Android 版本才可以。
- updatePeriodMillis：定义了 Widget 的刷新频率，也就是 App Widget Framework 多久请求一次 AppWidgetProvider 的 onUpdate() 回调函数。Android 系统默认最小更新周期是 30 分钟，也就是说：如果您的程序需要实时更新数据，设置这个更新周期是 2 秒，那么您的程序是不会每隔 2 秒就收到更新通知的，而是要等到 30 分钟以上才可以，要想实时的更新 Widget，一般可以采用 Service 和 AlarmManager 对 Widget 进行更新。

##### 3.自定义类主要有以下几个方法：

- onEnable() ：当小部件第一次被添加到桌面时回调该方法，可添加多次，但只在第一次调用。对用广播的 Action 为 ACTION_APPWIDGET_ENABLE。

- onUpdate():  当小部件被添加时或者每次小部件更新时都会调用一次该方法，配置文件中配置小部件的更新周期 updatePeriodMillis，每次更新都会调用。对应广播 Action 为：ACTION_APPWIDGET_UPDATE 和 ACTION_APPWIDGET_RESTORED 。此方法一般处理widget的创建布局和更新UI操作

- onDisabled(): 当最后一个该类型的小部件从桌面移除时调用，对应的广播的 Action 为 ACTION_APPWIDGET_DISABLED。

- onDeleted(): 每删除一个小部件就调用一次。对应的广播的 Action 为： ACTION_APPWIDGET_DELETED 。

- onRestored(): 当小部件从备份中还原，或者恢复设置的时候，会调用，实际用的比较少。对应广播的 Action 为 ACTION_APPWIDGET_RESTORED。

- onAppWidgetOptionsChanged(): 当小部件布局发生更改的时候调用。对应广播的 Action 为 ACTION_APPWIDGET_OPTIONS_CHANGED。

##### 4.对于单个控件（button）
```java
 @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
      
        // 获取Widget的组件名
        ComponentName mComponentName = new ComponentName(context, MulAppWidgetProvider.class);

        // 创建布局
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.mul_app_widget_provider);
		
		//文字
        views.setTextViewText(R.id.btn_test, "点击跳转");

        // 点击文字跳转到MainActivity
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(context, 200, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_test, pending);

        // 更新Wdiget
        appWidgetManager.updateAppWidget(mComponentName, views);
    }
```

##### 5.对于集合视图（GridView、ListView、StackView等）


(1)写一个类继承 RemoteViewsService：

RemoteViewsService，是管理RemoteViews的服务。一般，当AppWidget 中包含集合视图时，才需要使用RemoteViewsService来进行更新、管理。
```java
public class ListViewService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    // 通过 RemoteViewsFactory来具体管理layout中集合视图的
    private class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
     
        public ListRemoteViewsFactory(Context context, Intent intent) {
          
		  ...
		  
        }

    }
}
```

(2)在manifest中注册自定义的类：
```xml
 <service android:name=".service.ListViewService"
 			//必须加权限，用于绑定视图
            android:permission="android.permission.BIND_REMOTEVIEWS"
            android:exported="false"
            android:enabled="true"/>
```

(3)实现RemoteViewsFactory时必须实现以下几个方法：
```java
        /*
        * ListRemoteViewsFactory调用时执行，这个方法执行时间超过20秒回报错。
        * 如果耗时长的任务应该在onDataSetChanged或者getViewAt中处理
        */
        @Override
        public void onCreate() {
           .....
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
            views.setTextViewText(android.R.id.text1,mList.get(position));

            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt("data", position);
            intent.setAction("update");
            intent.putExtras(bundle);
			//点击item
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
```

(4)在onUpdate中：
```java
 @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // 获取Widget的组件名
       ComponentName mComponentName = new ComponentName(context, MulAppWidgetProvider.class);

        // 加载布局
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.mul_app_widget_provider);

        // 把这个Widget绑定到RemoteViewsService
        Intent lvIntent = new Intent(context, ListViewService.class);
        lvIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[0]);
        // 给listview设置适配器
        views.setRemoteAdapter(R.id.lv_test, lvIntent);
        // 设置当显示的widget为空显示的View
        views.setEmptyView(R.id.lv_test,android.R.id.empty);

        // 点击列表item
        Intent toIntent = new Intent(context,MulAppWidgetProvider.class);
        toIntent.setAction(CHANGE_IMAGE);
        toIntent.setData(Uri.parse(toIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 200, toIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.lv_test, pendingIntent);

        // 更新Wdiget
        appWidgetManager.updateAppWidget(mComponentName, views);
    }
```
(5)在onReceiver()中接收广播：
```java
 @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if(TextUtils.equals("update",intent.getAction())){
            Bundle extras = intent.getExtras();
            int position = extras.getInt("data");
			//加载布局
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.mul_app_widget_provider);
            mComponentName = new ComponentName(context, MulAppWidgetProvider.class);
            AppWidgetManager.getInstance(context).updateAppWidget(mComponentName, views);
        }
    }
```


