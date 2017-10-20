# http-s

## 下载，上传
android界优秀的http轮子不少，okhttp是其中最耀眼的一颗明星。其作为一种事实的android http标准实现甚至被吸纳到aosp中。  
所以在选型http框架时我觉得okhttp就足够了，不需要我们再构造出一种通用的模板去适配不同的http框架(如nohttp)。  
所以对于http的下载上传增强时我选择okhttp并与其的api强耦合，okhttp没有特别针对下载上传做过多的实现，而且okhttp里的一些流程也不是很适合下载上传。 
主要实现几个功能:  
1.下载上传时的谨慎实现，防止内存中放置过多的传输数据(即使用流传输).  
2.下载上传进度的监听，其中，上传的进度是指request post报文的进度，下载的进度是指response get报文的进度.  
3.使用统一的磁盘缓存去管理下载的文件.  
4.支持下载的断点续传，需要服务端同时支持range式的header，断点续传的文件也会归纳到统一缓存的管理.  

下载api使用:  
[DownloadCallback](https://github.com/earthgee/http-s/blob/master/app/src/main/java/com/earthgee/downloadokhttp/download/DownloadCallback.java):下载回调类  
[FileDownloader](https://github.com/earthgee/http-s/blob/master/app/src/main/java/com/earthgee/downloadokhttp/download/FileDownloader.java):下载工具类，单例，提供三个下载的方式.  
1.download 简单的下载  
2.downloadNeedProgress 带进度监听的下载，会回调onUpdate方法.  
3.downloadWithPause 支持断点续传的下载，需要上层业务做一些配合，可查看例子.  

上传api使用:  
[UploadCallback](https://github.com/earthgee/http-s/blob/master/app/src/main/java/com/earthgee/downloadokhttp/download/UploadCallback.java):上传回调类  
[FileUploader](https://github.com/earthgee/http-s/blob/master/app/src/main/java/com/earthgee/downloadokhttp/download/FileUploader.java):
上传工具类，单例，提供两种上传的方式，上传的报文类似form表单.  
1.uploadFile(final String url, String filePath, final UploadCallback uploadCallback) 单独文件的上传
2.uploadFileuploadFile(final String url, HashMap<String,String> formParams,String filePath, final UploadCallback uploadCallback) 带参数文件 的上传,即报文中有参数，文件，按照http的标准构造报文.


