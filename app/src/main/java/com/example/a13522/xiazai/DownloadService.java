package com.example.a13522.xiazai;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;
import java.io.File;

public class DownloadService extends Service {
    DownloadTask downloadTask;
    private String downloadUrl;
    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1,getNotification("准备下载",progress));
        }

        @Override
        public void onSuccess() {
            downloadTask =null;
            //关闭前台服务
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("下载成功",-1));
            Toast.makeText(DownloadService.this,"下载成功",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downloadTask=null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("下载失败",-1));
            Toast.makeText(DownloadService.this,"下载失败",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPause() {
            downloadTask=null;
            Toast.makeText(DownloadService.this,"下载暂停",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downloadTask=null;
            stopForeground(true);
            Toast.makeText(DownloadService.this,"下载停止",Toast.LENGTH_SHORT).show();
        }
    };
    private DownlosdBind mBind = new DownlosdBind();
    public IBinder onBind(Intent intent) {
        return mBind;
    }
    class DownlosdBind extends Binder{
        public void startDownload(String url){
            if (downloadTask==null){
                downloadUrl=url;
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(downloadUrl);
                startForeground(1,getNotification("Downloading...",0));
                Toast.makeText(DownloadService.this,"Downloading...",Toast.LENGTH_SHORT).show();
            }
        }

        public void pauseDownload(){
            if (downloadTask!=null){
                downloadTask.pauseDownload();
            }
        }
        public void cancelDownload(){
            if (downloadTask!=null){
                downloadTask.canceDownload();
            }else {
                if (downloadUrl!=null){
                    //取消下载的时候删除文件
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(dir+fileName);
                    if (file.exists()){
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownloadService.this,"Canceled 取消",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private NotificationManager getNotificationManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
    //设置进度条
    private Notification getNotification(String title,int progress){
      Intent intent= new Intent(this,MainActivity.class);
       PendingIntent pi= PendingIntent.getActivity(this,0,intent,0);
        //设置通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round));
        //设置点击
        builder.setContentIntent(pi);
        builder.setContentTitle(title);

        if (progress>=0){
            //显示下载进度
            builder.setContentText(progress+"%");
            builder.setProgress(100,progress,false);//false 使进度条显现出来
        }
        return builder.build();
    }
}
