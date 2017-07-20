package com.example.a13522.xiazai;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask extends AsyncTask<String,Integer,Integer> {
    HttpURLConnection connection;
    InputStream in;
    public static final int TYPE_SUCCESS=0;
    public static final int TYPE_FAILED=1;
    public static final int TYPE_PAUSED=2;
    public static final int TYPE_CANCELED=3;

    private DownloadListener listener;
    private boolean isCanceled=false;
    private boolean isPause = false;
    private int lastProgress;
    public DownloadTask(DownloadListener listener){
        this.listener=listener;
    }

    @Override
    protected Integer doInBackground(String... params) {
        RandomAccessFile saveFile = null;
        File file=null;
        try {
            //记录文件长度
            long downloadLength = 0;
            //获取url
            String downloadUrl = params[0];
            //获取文件名
           String fileName= downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            //指定下载路径
          String dierctory=  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            //拼接
            file=new File(dierctory+fileName);
            //判断文件是否存在如果存在读取文件字节大小
            if (file.exists()){
                downloadLength=file.length();
            }
            //如果没有文件通过getContentLength（）方法获取文件大小
            long contentLength = getContentLength(downloadUrl);
            if (contentLength==0){
                return TYPE_FAILED;
            }else if (contentLength==downloadLength){
                return TYPE_SUCCESS;
            }
            //开始下载


            URL url = new URL(downloadUrl);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("RANGE","bytes="+downloadLength+"-");

            in = connection.getInputStream();

            if (in!=null){

                saveFile =new RandomAccessFile(file,"rw");
                //跳过已经下载的字节
                saveFile.seek(downloadLength);
                //写入
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len=in.read(b))!=-1){
                    if (isCanceled){
                        return TYPE_CANCELED;
                    }else if (isPause){
                        return TYPE_PAUSED;
                    }else {
                        total+=len;
                        saveFile.write(b,0,len);
                        //计算一下载的百分比
                        int progress= (int) ((total+downloadLength)*100/contentLength);
                        //以进度条的形式显示
                        publishProgress(progress);
                    }
                }
                //下载完成关闭获取数据流
                in.close();
                return TYPE_SUCCESS;
            }

        }catch (Exception e){

        }finally {
            try {
            //关闭写入流
            if (in!=null){
                    in.close();
                }
                if (saveFile!=null){
                    saveFile.close();
                }
                if (isCanceled && file!=null){
                    file.delete();
                }
            }catch (Exception e){}
        }
        return TYPE_FAILED;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress>lastProgress){
            listener.onProgress(progress);
            lastProgress=progress;
        }
    }

    @Override
    protected void onPostExecute(Integer status) {
        switch (status){
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
            case TYPE_FAILED:
                listener.onFailed();
                break;
            case TYPE_PAUSED:
                listener.onPause();
                break;
            case TYPE_CANCELED:
                listener.onCanceled();
                default:
                break;
        }
    }
    //通知下载更新进度
    public void pauseDownload(){
        isPause =true;
    }
    public void canceDownload(){
        isCanceled = true;
    }
    private long getContentLength(String downloadUri) throws IOException{
        //发送url
        URL url = new URL(downloadUri);
        connection= (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        InputStream is = connection.getInputStream();

        //读取返回的文件
        if (is!=null){
            //获取文件大小

            long contentLength=  connection.getContentLength();
            is.close();//关闭请求
            return contentLength;
        }
        return 0;
    }
}
