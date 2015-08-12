package org.nita.cipher.hadoop;

/**
 * Created by havstack on 3/30/15.
 */

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

import java.io.IOException;

/**
 * <p>实现使用hadoop分析视频文件，该类将视频文件解析成帧数和帧图像的键值对</p>
 *
 */
public class VideoInputFormat extends FileInputFormat<Text, ImageWritable> {

    /* (non-Javadoc)
    * <p>Title: createRecordReader</p>
    * <p>Description: </p>
    * @param arg0
    * @param arg1
    * @return
    * @throws IOException
    * @throws InterruptedException
    * @see org.apache.hadoop.mapreduce.InputFormat#createRecordReader(org.apache.hadoop.mapreduce.InputSplit, org.apache.hadoop.mapreduce.TaskAttemptContext)
    */
    @Override
    public RecordReader<Text, ImageWritable> createRecordReader(
            InputSplit split, TaskAttemptContext context) throws IOException,
            InterruptedException {

        return new  VideoRecordReader();
    }

    //确保一个视频一个mapper处理
    protected boolean isSplitable(JobContext context, Path file) {
        return false;
    }

}
