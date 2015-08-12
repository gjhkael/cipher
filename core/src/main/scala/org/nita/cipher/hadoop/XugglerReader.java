package org.nita.cipher.hadoop;

/**
 * Created by havstack on 3/30/15.
 */

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IPixelFormat.Type;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;

/**
 * <p>使用Xuggler来读取HDFS上的视频文件，提供读取视频文件的码流packet，
 * 和逐个读取视频中帧的函数。学习资料见：http://blog.csdn.net/hpb21/article/details/7943372</p>
 *
 */
public class XugglerReader {
    private Path filePath;
    private BufferedImage frame = null;
    private Text key; //文件名帧号
    private IContainer container = null;  //当前读取的视频
    private IPacket packet = null;    //当前读入的码流
    private int videoStreamId = -1;   //视频中视频流id
    private IStreamCoder videoCoder = null;
    private IVideoResampler resampler = null; //用来转换视频帧为BGR24
    private int offsetInPacket = 0;
    private boolean _finished = false;
    private long frameNum = 0;
    private IConverter converter;


    public XugglerReader(String localFilePath) throws IOException {
        // To do.... 读取本地文件的Xuggle Reader
        String fileName = localFilePath.substring(localFilePath.lastIndexOf("/")+1);
        File file = new File(localFilePath);
        FileInputStream fs = new FileInputStream(file);
        BufferedInputStream bs = new BufferedInputStream(fs);
        DataInputStream in = new DataInputStream(bs);

        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        if(suffix.length() == fileName.length())
            throw new IOException("input is not a valid video");
        System.out.println(localFilePath);

        container = IContainer.make();

        IContainerFormat containerFormat = IContainerFormat.make();
        containerFormat.setInputFormat(suffix);

        container.setInputBufferLength(327680);
        System.out.println("the buffer size of container "+in.available());
        //开始读流
        if (container.open(in,containerFormat,true, true) < 0)
            throw new IOException("could not open video file: " + localFilePath);

        //获取流数
        int numStreams = container.getNumStreams();

        //从流中识别视频流
        for (int i = 0; i < numStreams; i++)
        {
            IStream stream = container.getStream(i);
            IStreamCoder coder = stream.getStreamCoder();

            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO)
            {
                videoStreamId = i;
                videoCoder = coder;
                break;
            }
        }

        if (videoStreamId == -1)
            throw new RuntimeException("could not find video stream in container: " + filePath);

        //读取视频流
        if (videoCoder.open() < 0)
            throw new RuntimeException("could not open video decoder for container: " + filePath);

        //重新编码图片格式
        if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24)
        {
            resampler =
                    IVideoResampler.make(videoCoder.getWidth(),
                            videoCoder.getHeight(),
                            IPixelFormat.Type.BGR24,
                            videoCoder.getWidth(),
                            videoCoder.getHeight(),
                            videoCoder.getPixelType());
            if (resampler == null)
                throw new RuntimeException("could not create color space " + "resampler for: " + filePath);
        }

        packet = IPacket.make();
        nextPacket(); //读入下一个码流
        

        //创建能被opencv处理的帧
        frame = new BufferedImage(videoCoder.getWidth(),videoCoder.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
        converter = ConverterFactory.createConverter(frame, IPixelFormat.Type.BGR24);
    }


    public XugglerReader(Path hdfsFilePath, Configuration conf) throws IOException {

        filePath = hdfsFilePath;
        String fileName = hdfsFilePath.getName();
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        if(suffix.length() == fileName.length())
            throw new IOException("input is not a valid video");
        System.out.println(filePath);
        FileSystem fs = filePath.getFileSystem(conf);

        DataInputStream videoStream = new DataInputStream(fs.open(filePath));

        container = IContainer.make();
        IContainerFormat containerFormat = IContainerFormat.make();
        containerFormat.setInputFormat(suffix);

        //视频文件太大，这个也太大
        //container.setInputBufferLength(videoStream.available());
        //container.setInputBufferLength(327680);
        container.setInputBufferLength(100000);

        //开始读流
        if (container.open(videoStream,containerFormat) < 0)
            throw new IOException("could not open video file: " + filePath);


        //获取流数
        int numStreams = container.getNumStreams();

        //从流中识别视频流
        for (int i = 0; i < numStreams; i++)
        {
            IStream stream = container.getStream(i);
            IStreamCoder coder = stream.getStreamCoder();

            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO)
            {
                videoStreamId = i;
                videoCoder = coder;
                break;
            }
        }

        if (videoStreamId == -1)
            throw new RuntimeException("could not find video stream in container: " + filePath);

        //读取视频流
        if (videoCoder.open() < 0)
            throw new RuntimeException("could not open video decoder for container: " + filePath);

        //重新编码图片格式
        if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24)
        {
            resampler =
                    IVideoResampler.make(videoCoder.getWidth(),
                            videoCoder.getHeight(),
                            IPixelFormat.Type.BGR24,
                            videoCoder.getWidth(),
                            videoCoder.getHeight(),
                            videoCoder.getPixelType());
            if (resampler == null)
                throw new RuntimeException("could not create color space " + "resampler for: " + filePath);
        }

        packet = IPacket.make();
        nextPacket(); //读入下一个码流

        //创建能被opencv处理的帧
        frame = new BufferedImage(videoCoder.getWidth(),videoCoder.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
        converter = ConverterFactory.createConverter(frame, IPixelFormat.Type.BGR24);
    }

    /**
     * <p>获取视频中的流个数（一个视频文件可能包含视频流或者音频流）</p>
     * @return int    返回类型
     */
    public int getNumStreams(){
        return container.getNumStreams();
    }
    public long getDuration(){
        return container.getDuration(); //获取持续时间（ms）
    }

    public long getBitRate(){
        return container.getBitRate();
    }

    public double getFrameRate(){
        return videoCoder.getFrameRate().getDouble();
    }

    public int getWidth(){
        return videoCoder.getWidth();
    }
    public int getHeight(){
        return videoCoder.getHeight();
    }

    public String getPixelType(){
        return videoCoder.getPixelType().toString();
    }

    public long getFrameCount(){
        return (long) (getDuration()/1000*getFrameRate());
    }


    public long getCurrentFrameNum(){
        return frameNum;
    }


    public BufferedImage getCurrentFrame(){
        return frame;
    }

    /**
     * <p>获取下一个码流</p>
     * @param
     * @return boolean    返回类型,成功读取返回true，失败返回false，文件已经读取完毕
     * @throws
     */
    public boolean nextPacket(){
        if(_finished == true){
            return false;
        }
        while (true){
            int state = container.readNextPacket(packet);
            //直到获取视频流中的一段码流
            if(state < 0){
                _finished = true; //视频已经读取完毕
                return false;
            }else{
                if(packet.getStreamIndex() == videoStreamId){
                    offsetInPacket = 0;
                    return true;  //读取视频码流完毕
                }else{
                    continue;  //不是视频流，读取下一个码流packet
                }
            }
        }
    }

    public boolean nextFrame(){
        System.out.println(frameNum);

        IVideoPicture picture =
                IVideoPicture.make(videoCoder.getPixelType(), videoCoder.getWidth(), videoCoder.getHeight());
        while(true){
            if(offsetInPacket >= packet.getSize()){
                //该码流读取完毕
                if(!nextPacket()) //读取下一个视频码流
                    return false;
                continue;
            }
            int bytesDecoded = videoCoder.decodeVideo(picture, packet, offsetInPacket);
            if (bytesDecoded < 0)
                return false;
            //throw new RuntimeException("got error decoding video in: " + filePath);

            offsetInPacket += bytesDecoded;
            if (picture.isComplete()){
                IVideoPicture newPic = picture;
                if (resampler != null)
                {
                    newPic =
                            IVideoPicture.make(resampler.getOutputPixelFormat(),
                                    picture.getWidth(),
                                    picture.getHeight());
                    if (resampler.resample(newPic, picture) < 0)
                        throw new RuntimeException("could not resample video from: " + filePath);
                }
                if (newPic.getPixelType() != IPixelFormat.Type.BGR24)
                    throw new RuntimeException("could not decode video" + " as BGR 24 bit data in: " + filePath);
                frame = converter.toImage(newPic);
                frameNum++;
                return true;
            }
        }
    }

    public static void main(String[] args) throws IOException {

        //测试本地文件系统
        String localFilePath = "/Users/zhaoxm/test.avi";

        XugglerReader xr1 = new XugglerReader(localFilePath);
        System.out.println(xr1.getNumStreams());
        System.out.println(xr1.getBitRate());
        System.out.println(xr1.getFrameRate());
        System.out.println(xr1.getHeight());


        //测试HDFS上的文件
        Configuration conf = new Configuration();

        String hdfsFilePath = "/user/zhaoxm/wssu1gj8npw3_1382687229_1382688565.avi";
        Path path  = new Path(hdfsFilePath);
        //设置master路径
        //conf.set("fs.defaultFS","hdfs://video-data-center-server-0:8020");
        conf.set("fs.defaultFS","hdfs://127.0.0.1:9000"); //correspond with core-site.xml
        XugglerReader xr2 = new XugglerReader(path,conf);
        System.out.println(xr2.getNumStreams());
        System.out.println(xr2.getBitRate());
        System.out.println(xr2.getFrameRate());
        System.out.println(xr2.getHeight());
    }
}
