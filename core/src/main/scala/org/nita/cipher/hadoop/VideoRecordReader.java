package org.nita.cipher.hadoop;

/**
 * Created by havstack on 3/30/15.
 */

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * <p>将Hadoop的一个InputSplit转化为 文件名_帧号，帧图像 的键值对 </p>
 *
 */
public class VideoRecordReader extends RecordReader<Text, ImageWritable> {

    private Path filePath;
    private long frameNum = 0;
    private BufferedImage frame = null;
    private Text key = new Text(); //文件名帧号
    private boolean _finished = false;
    private XugglerReader xr;

    /* (non-Javadoc)
    * <p>Title: getCurrentKey</p>
    * <p>Description: </p>
    * @return
    * @throws IOException
    * @throws InterruptedException
    * @see org.apache.hadoop.mapreduce.RecordReader#getCurrentKey()
    */
    @Override
    public Text getCurrentKey() throws IOException,
            InterruptedException {
        try{
            //System.out.println("key: "+key);
            return key;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /* (non-Javadoc)
    * <p>Title: getCurrentValue</p>
    * <p>Description: </p>
    * @return
    * @throws IOException
    * @throws InterruptedException
    * @see org.apache.hadoop.mapreduce.RecordReader#getCurrentValue()
    */
    @Override
    public ImageWritable getCurrentValue() throws IOException,
            InterruptedException {
        try{
            ImageWritable value = new ImageWritable(frame);
            return value;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /* (non-Javadoc)
    * <p>Title: getProgress</p>
    * <p>Description: </p>
    * @return
    * @throws IOException
    * @throws InterruptedException
    * @see org.apache.hadoop.mapreduce.RecordReader#getProgress()
    */
    @Override
    public float getProgress() throws IOException, InterruptedException {
        if (_finished) {
            return 1.0f;
        } else {
            return 0.0f;
        }
    }

    /* (non-Javadoc)
    * <p>Title: initialize</p>
    * <p>获取视频文件对应HDFS上路径，用来初始化XuggleReader </p>
    * @param genericSplit
    * @param context
    * @throws IOException
    * @throws InterruptedException
    * @see org.apache.hadoop.mapreduce.RecordReader#initialize(org.apache.hadoop.mapreduce.InputSplit, org.apache.hadoop.mapreduce.TaskAttemptContext)
    */
    @Override
    public void initialize(InputSplit genericSplit, TaskAttemptContext context)
            throws IOException, InterruptedException {
        try{

            FileSplit split = (FileSplit)genericSplit; // 获取split在HDFS上对应的文件名
            filePath = split.getPath();

            xr = new XugglerReader(filePath, context.getConfiguration());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
    * <p>Title: nextKeyValue</p>
    * <p>Description:获取下一个（文件名_帧数，帧）键值对 </p>
    * @return
    * @throws IOException
    * @throws InterruptedException
    * @see org.apache.hadoop.mapreduce.RecordReader#nextKeyValue()
    */
    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        if(!xr.nextFrame()){
            _finished = false;
            return false;
        }
        try{
            //设置Key
            key.set(filePath.getName() + "_" + xr.getCurrentFrameNum());

            //设置Value
            frame = xr.getCurrentFrame();
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }

    /* (non-Javadoc)
    * <p>Title: close</p>
    * <p>Description: </p>
    * @throws IOException
    * @see org.apache.hadoop.mapreduce.RecordReader#close()
    */
    @Override
    public void close() throws IOException {
        return;
    }
}
