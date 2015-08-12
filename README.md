# cipher

现有的大数据平台Hadoop、Spark等都在处理文本数据方面具有很好的支持，并且效率也经过了各种优化，所以在利用分布式框架来处理日志类数据，工作难度往往是如何对这些数据进行逻辑上的处理。但是对于非结构化数据，现有的并行处理架构是不支持的，如果只是简单的将数据存储到HDFS，然而并不能进行数据处理。本项目是基于HDFS来存储视频数据，利用Spark来对其进行机器视觉算法分析。我希望能够将其他非结构化数据处理也加入其中，有兴趣的希望加入我。

### 本项目分为四个模块：
  - **algorithm**：算法部分，将c++版本的opencv算法通过javacpp将其翻译成对应的java接口。
  - **core**：作为分布式计算的核心部分，继承FileInputFormat，实现了用于处理Video的Format。并实现了Spark分布式程序。
  - **spring**：利用Spring MVC将计算的结果展示到前端（这部分因为不涉及到计算方面，所以这里并没有给出源码）。
  - **sql**：数据库相关操作，将算法的结果插入到数据库中（mysql或mongdb）

### 如何使用
由于本项目是基于opencv机器视觉库，并且构建在hadoop和Spark之上，所以需要在本地安装好执行环境，可以参考我的另外一个项目中有详细的[环境配置文档](https://github.com/gjhkael/deployDoc)。最好能将本项目构建在分布式的集群之上。所以配置环境的顺序为：安装opencv->安装hadoop->安装Spark->安装好数据库（如果不想存入到数据库，把程序中的插入数据代码注释即可）。

1.将项目克隆到本地
```
git clone https://github.com/gjhkael/cipher.git
```
2.使用mvn进行打包
```
mvn clean packge
```
3.在target目录下找到core.jar、sql-server.jar。core.jar用来提交Spark程序的。sql-server.jar用来存储图片的。

### 运行程序
运行程序的方式有两种，一种是在idea中打开程序，然后配置好信息，直接本地运行，另外一种是提交到集群。下面分别介绍

1.使用Intellij IDEA 将本项目Import进去，
使用run进行执行

2.在spark集群中，利用指令 ./bin/spark-submit --master Spark://XXX:7077 --class org.nita.cipher.FaceDetect ./path/to/jar(jar包的路径) 程序参数（根据程序做相应修改）

3.每台机器使用java -jar ./sql-server.jar执行代码

### 模块代码详解
可以直接去看源码，源码中详细的解释，这里调一些重点内容进行分享

1.algorithm

首先看一下algorithm/cppbuild/linux-x86_64/include和lib目录，其中include中的程序是我们传统方式写的基于opencv的机器视觉程序（比如：人脸检测、人脸识别、移动检测、越界检测、视频摘要、车牌识别等，目前我们已经测试过5中以上的算法，并集成到平台中），这里需要注意的是：算法的函数接口传入的是平台负责传入的视频数据，比如一帧图片或则一个gop，算法函数参数需要改成IplImage或则Mat数据结构。这里有不明白的可以私信我，一起交流。然后需要写一个java翻译接口文件，在src下的presets目录下：这里的目的是将C++的Mat或则Iplimage数据结构翻译成java能认识的类（底层是使用的JNI调用），如果有朋友想知道原理，请看[JavaCpp](https://github.com/bytedeco/javacpp)开源项目。如果是搞视频处理的，建议对这个项目进行深入理解。

2.core

到org.nita.cipher.hadoop中可以看到，这里的核心是实现了一个叫做VideoInputFormat的类，其继承了FileInputFormat，并覆盖了RecordReader和isSplitable方法。
```
public RecordReader<Text, ImageWritable> createRecordReader(
            InputSplit split, TaskAttemptContext context) throws IOException,
            InterruptedException {

        return new  VideoRecordReader();
    }
    
 //确保一个视频一个mapper处理
protected boolean isSplitable(JobContext context, Path file) {
        return false;
}
```
这里需要给大家解释一下，为什么将isSplitable方法直接返回false，研究过Mapreduce的同学应该知道，进行文本处理往往使用的是TextFileInputFormat或则更加高效的SequenceFileInputFormat,这是hadoop实现了的基于文本处理的几种输入格式，到其源码中你会发现，isSplitable在输入的文件是支持分割的文件格式的时候或则不采用压缩格式（裸txt文件），这个方法才会返回true，而hadoop中只有bzip2是可支持分割的压缩格式，视频文件或则图片往往是采用了压缩格式h264、h265、mpeg等视频压缩格式。这种文件放入到Hdfs中是不支持分割的。所以应该直接返回false。那么如何让一个大的视频文件存放到HDFS中能够支持切分呢？自己去实现FileInputFormat的getSplits方法，这个函数是负责逻辑上的切分文件的，这里我可以提供一个思路，因为FFmpeg是支持任意位置开始解码的，这样在getSplits中实现将逻辑分割的位置信息传给ffmpeg这样就可以达到一个视频文件，分布式处理的效果了。

大家有兴趣的可以好好的看一下VideoRecordReader类，里面有详细的关于如何从HDFS中将文件取出并向上层提供
```<Text, ImageWritable> ```数据类型.



3.sql
数据存储模块，这里加入了数据存储模块，有人可能会质疑为什么不把存放数据代码放入到算法中去，其实在我们之前做Spark Streaming流式处理视频数据的时候遇到了算法产生结果太多，频繁的对数据库操作严重影响了流的实时处理，增加了数据流流失计算的延迟。所以在这里利用了消息通信机制zeromq，将数据交给zeromq的工作节点，让它负责将数据存储到数据库，从而不影响计算总时间，延迟自然减少，有兴趣的可以看一下代码。


























