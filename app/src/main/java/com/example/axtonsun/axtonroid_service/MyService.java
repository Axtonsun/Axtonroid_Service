package com.example.axtonsun.axtonroid_service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

/**
 * Created by AxtonSun on 2016/6/7.
 */

/**
 * Service中再创建一个子线程，然后去处理耗时逻辑
 * 所有的Activity都可以与Service进行关联，然后可以很方便地操作其中的方法，
 * 即使Activity被销毁了，之后只要重新与Service建立关联，
 * 就又能够获取到原有的Service中Binder的实例
 * 使用Service来处理后台任务，Activity就可以放心地finish
 */
public class MyService extends Service {//Service运行在主线程

    public static final String TAG = "MyService";

    //private MyBinder mBinder = new MyBinder();
    /**
     * Service中唯一的一个抽象方法
     * 必须要在子类里实现
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * 只有你允许客户端从不同的应用程序为了进程间的通信而去访问你的service，以及想在你的service处理多线程
     */
    MyAIDLService.Stub mBinder = new MyAIDLService.Stub(){

        @Override
        public String toUpperCase(String str) throws RemoteException {
            if (str != null) {
                return str.toUpperCase();
            }
            return null;
        }

        @Override
        public int plus(int a, int b) throws RemoteException {
            return a + b;
        }
    };

    private int notifyId = 110;

    /**
     * 会在服务创建的时候调用
     */
    @Override
    public void onCreate() {
        Log.d("MyService", "MyService thread id is " + Thread.currentThread().getId());
        Log.d("TAG", "process id is " + Process.myPid());
        super.onCreate();
        Log.d(TAG, "onCreate() executed");
        //让线程睡眠60秒 程序就会阻塞住并无法进行任何其它操作
        /*try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        //创建前台Service
        //NotificationManager 是一个系统Service 必须通过 getSystemService(NOTIFICATION_SERVICE)方法来获取
        //获取状态通知栏管理
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //实例化通知栏构造器NotificationCompat.Builder
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        //对Builder进行配置
        mBuilder.setContentTitle("测试标题")//设置通知栏标题
                .setContentText("测试内容") //设置通知栏显示内容
                .setTicker("测试通知来啦!!") //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                .setSmallIcon(R.mipmap.ic_launcher);//设置通知小ICON

        //在执行了点击通知之后要跳转到指定的XXX的Activity的时候，可以设置以下方法来相应点击事件
        Intent intent = new Intent(this, MainActivity.class);
        //PendingIntent设置执行次数，主要用于远程服务通信、闹铃、通知、启动器、短信中，在一般情况下用的比较少
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        mBuilder.setContentIntent(pendingIntent);

        Notification notify = mBuilder.build();
        mNotificationManager.notify(notifyId,notify);
        startForeground(notifyId, notify);//使用前台服务

    }

    /**
     * 服务一旦启动就立刻去执行某个动作 就可以将逻辑写在此方法内
     * 会在每次服务启动的时候调用
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() executed");
        //创建子线程  不在Activity中创建是因为Activity对Thread很难控制
        //当Activity被销毁之后，
        // 就没有任何其它的办法可以再重新获取到之前创建的子线程的实例。
        // 而且在一个Activity中创建的子线程，另一个Activity无法对其进行操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 开始执行后台任务
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 当服务销毁时 应该在此方法中回收那些不再使用的资源
     * 会在服务的销毁的时候调用
     * 清理掉那些不再使用的资源，
     * 防止在Service被销毁后还会有一些不再使用的对象仍占用着内存。
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() executed");
    }

    class MyBinder extends Binder {

        public void startDownload() {
            Log.d(TAG, "startDownload() executed");
            // 用于在后台执行具体的下载任务

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 执行具体的下载任务
                }
            }).start();
        }

    }
}
