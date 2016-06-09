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
* 既点击了Start Service按钮，又点击了Bind Service按钮 不管你是单独点击Stop Service按钮还是Unbind Service按钮，Service都不会被销毁，必要将两个按钮都点击一下，Service才会被销毁。也就是说，点击Stop Service按钮只会让Service停止，点击Unbind Service按钮只会让Service和Activity解除关联，一个Service必须要在既没有和任何Activity关联又处理停止状态的时候才会被销毁。   
###Service和Thread的关系
###创建前台Service
