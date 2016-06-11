# Axtonroid_Service
###资料来源
[Android Service完全解析，关于服务你所需知道的一切(上)](http://blog.csdn.net/guolin_blog/article/details/11952435 " 郭霖的博客")  
[Android Service完全解析，关于服务你所需知道的一切(下)](http://blog.csdn.net/guolin_blog/article/details/9797169 " 郭霖的博客")  
[Android 通知栏Notification的整合 全面学习](http://blog.csdn.net/vipzjyno1/article/details/25248021 " vipra的博客")  
###Service
* 主要用于在后台处理一些耗时的逻辑，或者去执行某些需要长期运行的任务。
* 必要的时候我们甚至可以在程序退出的情况下，让Service在后台继续保持运行状态  

###Service的基本用法
先建一个类然后`继承Service`并重写父类的`onCreate()`、`onStartCommand()`和`onDestroy()`方法
* `onBind()` 
  * Service中唯一的一个抽象方法 必须要在子类里实现
* `onCreate()` 
  * 会在服务创建的时候调用
* `onStartCommand` 
  * 服务一旦启动就立刻去执行某个动作 就可以将逻辑写在此方法内 会在每次服务启动的时候调用
* `onDestroy()`
  * 当服务销毁时 应该在此方法中回收那些不再使用的资源 会在服务的销毁的时候调用  
    清理掉那些不再使用的资源，防止在Service被销毁后还会有一些不再使用的对象仍占用着内存  

* 每一个Service都必须在`AndroidManifest.xml`中注册
  ```
  <service android:name=".MyService">  
  </service>
  ```
* `main_activity.xml`设置Button 然后在`MainActivity.class`内设置点击事件  
```
  case R.id.start_service:  
            Intent startIntent = new Intent(this, MyService.class);  
            startService(startIntent);  
            break;  
  case R.id.stop_service:  
            Intent stopIntent = new Intent(this, MyService.class);  
            stopService(stopIntent);  
            break;  
```
  *   
    * 当启动一个Service的时候，会调用该Service中的`onCreate()`和`onStartCommand()`方法
    * 当再次点击就只会有`onStartCommand()`方法
  
###Service和Activity通信
* onBind()方法就是用于和Activity建立关联的 修改MyService中的代码
* `private MyBinder mBinder = new MyBinder(); `
```
@Override  
    public IBinder onBind(Intent intent) {  
        return mBinder;  
    }  
```
```
class MyBinder extends Binder {  
  
        public void startDownload() {  
            Log.d("TAG", "startDownload() executed");  
            // 执行具体的下载任务  
        }  
  
    }  
```
* MyBinder类继承自Binder类 然后在MyBinder中添加了一个`startDownload()`方法用于在后台执行下载任务
* 修改MainActivity中的代码，让MainActivity和MyService之间建立关联
```
   /**
     *  创建了一个ServiceConnection的匿名类
     *  重写了onServiceConnected()方法和onServiceDisconnected()方法
     *  分别会在Activity与Service建立关联和解除关联的时候调用
     *  向下转型得到了MyBinder的实例，有了这个实例，Activity和Service之间的关系就变得非常紧密
     */

    private MyService.MyBinder myBinder;  
  
    private ServiceConnection connection = new ServiceConnection() {  
  
        @Override  
        public void onServiceDisconnected(ComponentName name) {  
        }  
  
        @Override  
        public void onServiceConnected(ComponentName name, IBinder service) {  
            myBinder = (MyService.MyBinder) service;  //MyService类 MyBinder类
            myBinder.startDownload();  
        }  
    };  
  ```
*  `main_activity.xml`设置Button 然后在`MainActivity.class`内设置点击事件
```
case R.id.bind_service:  
            Intent bindIntent = new Intent(this, MyService.class);  
            bindService(bindIntent, connection, BIND_AUTO_CREATE);  
            /**
              * 第一个参数就是刚刚构建出的Intent对象，
              * 第二个参数是前面创建出的ServiceConnection的实例，
              * 第三个参数是一个标志位，这里传入BIND_AUTO_CREATE表示在Activity和Service建立关联后自动创建Service，
              * 这会使得MyService中的onCreate()方法得到执行，但onStartCommand()方法不会执行。
              */
            break;  
case R.id.unbind_service:  
            unbindService(connection);  
            break;  
```
#####任何一个Service在整个应用程序范围内都是通用的，即MyService不仅可以和MainActivity建立关联，还可以和任何一个Activity建立关联，而且在建立关联时它们都可以获取到相同的MyBinder实例。

###如何销毁Service
* 点击Start Service按钮启动Service，再点击Stop Service按钮停止Service,这样MyService就被销毁了
* 先点击一下Bind Service按钮，再点击一下Unbind Service按钮,这样MyService就被销毁了

>既点击了Start Service按钮，又点击了BindService按钮不管你是单独点击StopService按钮还是UnbindService按钮Service都不会被销毁必要将两个按钮都点击一下，Service才会被销毁。
>>也就是说，点击StopService按钮只会让Service停，点击UnbindService按钮只会让Service和Activity解除关联，一个Service必须要在既没有和任何Activity关联又处理停止状态的时候才会被销毁。 

###Service和Thread的关系
* Service运行在主线程里，也就是说如果你在Service里编写了非常耗时的代码，程序必定会出现ANR的。
* Android的后台就是指，它的运行是完全不依赖UI的。即使Activity被销毁，或者程序被关闭，只要进程还在，Service就可以继续运行。
* 我们可以在Service中再创建一个子线程，然后在这里去处理耗时逻辑就就不会阻塞主线程的运行。
 * 既然在Service里也要创建一个子线程，那为什么不直接在Activity里创建呢？
    * 因为Activity很难对Thread进行控制，当Activity被销毁之后，就没有任何其它的办法可以再重新获取到之前创建的子线程的实例
    * 在一个Activity中创建的子线程，另一个Activity无法对其进行操作  
 * Service就不同了，所有的Activity都可以与Service进行关联
       * 然后可以很方便地操作其中的方法，即使Activity被销毁了，之后只要重新与Service建立关联，
       * 就又能够获取到原有的Service中Binder的实例。  
* 因此，使用Service来处理后台任务，Activity就可以放心地finish，完全不需要担心无法对后台任务进行控制的情况。  

#####一个标准的Service
```
    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) {  
        new Thread(new Runnable() {  
            @Override  
            public void run() {  
                // 开始执行后台任务  
            }  
        }).start();  
        return super.onStartCommand(intent, flags, startId);  
    }  
      
    class MyBinder extends Binder {  
      
        public void startDownload() {  
            new Thread(new Runnable() {  
                @Override  
                public void run() {  
                    // 执行具体的下载任务  
                }  
            }).start();  
        }  
    }  
```
###创建前台Service
>Service几乎都是在后台运行的，但是Service的系统优先级还是比较低的，
>>当系统出现内存不足情况时，就有可能会回收掉正在后台运行的Service。
>>>如果你希望Service可以一直保持运行状态，而不会由于系统内存不足的原因导致被回收，就可以考虑使用前台Service。
>>>>前台Service和普通Service最大的区别就在于，它会一直有一个正在运行的图标在系统的状态栏显示，下拉状态栏后可以看到更加详细的信息，非常类似于通知的效果。
>>>>>当然有时候你也可能不仅仅是为了防止Service被回收才使用前台Service，有些项目由于特殊的需求会要求必须使用前台Service。

#####由于[Android Service完全解析，关于服务你所需知道的一切(上)](http://blog.csdn.net/guolin_blog/article/details/11952435 " 郭霖的博客")所给的代码比较落后
#####所以我根据[Android 通知栏Notification的整合 全面学习](http://blog.csdn.net/vipzjyno1/article/details/25248021 " vipra的博客")在`MyService`的`onCreate()`方法中
```
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
 ``` 
###远程Service的用法
 * Service其实是运行在主线程里的，如果直接在Service中处理一些耗时的逻辑，就会导致程序ANR。
 * 在`MyService`的`onCreate()`方法中让线程睡眠60秒。 
 
 ```
        try {  
            Thread.sleep(60000);  
        } catch (InterruptedException e) {  
            e.printStackTrace();  
        }  
 ```    
 * 重新运行后，点击一下StartService按钮或BindService按钮，程序就会阻塞住并无法进行任何其它操作，过一段时间后就会弹出ANR的提示框
 * 转化为远程Service只需要在注册的时候添加`android:process=":remote"`
 * 重新运行程序，并点击一下StartService按钮，你会看到控制台立刻打印了onCreate()executed的信息，
   * 而且主界面并没有阻塞住，也不会出现ANR。大概过了一分钟后，又会看到onStartCommand() executed打印了出来。
 * 那是因为进程id不同了，就连应用程序包名也不一样了  

#####弊端 
>首先将MyService的onCreate()方法中让线程睡眠的代码去除掉，然后重新运行程序，并点击一下BindService按钮，你会发现程序崩溃了！
>>为什么点击Start Service按钮程序就不会崩溃，而点击Bind Service按钮就会崩溃呢？
>>>这是由于在Bind Service按钮的点击事件里面我们会让MainActivity和MyService建立关联，
>>>>但是目前MyService已经是一个远程Service了，Activity和Service运行在两个不同的进程当中，
>>>>>这时就不能再使用传统的建立关联的方式，程序也就崩溃了。

###Activity与一个远程Service建立关联
* 使用AIDL（Android Interface Definition Language）是Android接口定义语言 来进行跨进程通信
 * 新建一个AIDL文件
 ```
 interface MyAIDLService {  
     int plus(int a, int b);  
     String toUpperCase(String str);  
 }  
 ```
 * 然后进行调试gen目录下就会生成一个对应的Java文件
 * 接着修改`MyService`
 ```
 MyAIDLService.Stub mBinder = new Stub() {  
  
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
 ```
  * 修改`MainActivity`
  ```
  /**
     *  创建了一个ServiceConnection的匿名类
     *  重写了onServiceConnected()方法和onServiceDisconnected()方法
     *  分别会在Activity与Service建立关联和解除关联的时候调用
     *  向下转型得到了MyBinder的实例，有了这个实例，Activity和Service之间的关系就变得非常紧密
     */

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //myBinder = (MyService.MyBinder) service;
            //myBinder.startDownload();
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
  ``` 
