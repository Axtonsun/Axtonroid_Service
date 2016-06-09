package com.example.axtonsun.axtonroid_service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button startService;

    private Button stopService;

    private Button bindService;

    private Button unbindService;

    private MyService.MyBinder myBinder;

    private MyAIDLService myAIDLService;

    /**
     *  创建了一个ServiceConnection的匿名类
     *  重写了onServiceConnected()方法和onServiceDisconnected()方法
     *  分别会在Activity与Service建立关联和解除关联的时候调用
     *  向下转型得到了MyBinder的实例，有了这个实例，Activity和Service之间的关系就变得非常紧密
     *
     */

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
           /* myBinder = (MyService.MyBinder) service;
            myBinder.startDownload();*/
            // Activity和Service运行在两个不同的进程当中，这时就不能再使用传统的建立关联的方式，程序也就崩溃了
            // 修改manifest文件里边相关服务去掉android:process=":remote"

            myAIDLService = MyAIDLService.Stub.asInterface(service);//将传入的IBinder对象传换成了MyAIDLService对象
            try {
                int result = myAIDLService.plus(3, 5);
                String upperStr = myAIDLService.toUpperCase("hello world");
                Log.d("TAG", "result is " + result);
                Log.d("TAG", "upperStr is " + upperStr);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MyService", "MainActivity thread id is " + Thread.currentThread().getId());
        Log.d("TAG", "process id is " + Process.myPid());
        startService = (Button) findViewById(R.id.start_service);
        stopService = (Button) findViewById(R.id.stop_service);
        bindService = (Button) findViewById(R.id.bind_service);
        unbindService = (Button) findViewById(R.id.unbind_service);
        startService.setOnClickListener(this);
        stopService.setOnClickListener(this);
        bindService.setOnClickListener(this);
        unbindService.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.start_service:
                Intent startIntent = new Intent(this, MyService.class);
                startService(startIntent);
                break;
            case R.id.stop_service:
                Log.d("MyService", "click Stop Service button");
                Intent stopIntent = new Intent(this, MyService.class);
                stopService(stopIntent);
                break;
            case R.id.bind_service:
                Intent bindIntent = new Intent(this, MyService.class);
                bindService(bindIntent, connection, BIND_AUTO_CREATE);
                /**
                 * 第一个参数就是刚刚构建出的Intent对象，
                 * 第二个参数是前面创建出的ServiceConnection的实例，
                 * 第三个参数是一个标志位，这里传入BIND_AUTO_CREATE表示在Activity和Service建立关联后自动创建Service，这会使得MyService中的onCreate()方法得到执行，但onStartCommand()方法不会执行。
                 */
                break;
            case R.id.unbind_service:
                Log.d("MyService", "click Unbind Service button");
                unbindService(connection);
                break;
            default:
                break;
        }
    }
}
