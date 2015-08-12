package org.nita.cipher.hadoop;

/**
 * Created by havstack on 3/30/15.
 */

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.Iterator;

/**
 * <p> 使用Hadoop处理视频的Mapreduce例子程序</p>
 *
 */
public class MRVideoReader {

    public static class MRVideoReaderMapper extends Mapper<Text, ImageWritable, Text, Text> {

        /* (non-Javadoc)
        * <p>Title: map</p>
        * <p>Description: </p>
        * @param arg0
        * @param arg1
        * @param arg2
        * @param arg3
        * @throws IOException
        * @see org.apache.hadoop.mapred.Mapper#map(java.lang.Object, java.lang.Object, org.apache.hadoop.mapred.OutputCollector, org.apache.hadoop.mapred.Reporter)
        */
        public void map(Text key, ImageWritable value, Context context)throws IOException, InterruptedException {
            Text valueOutput = new Text();

            valueOutput.set(value.getBufferedImage().toString());

            //输出的key的格式为"文件名_帧数"，输出valueOutput为帧的元信息
            context.write(key, valueOutput);

        }

    }
    public static class MRVideoReaderReducer extends Reducer<Text, Text, Text, Text> {


        /* (non-Javadoc)
        * <p>Title: reduce</p>
        * <p>Description: </p>
        * @param arg0
        * @param arg1
        * @param arg2
        * @param arg3
        * @throws IOException
        * @see org.apache.hadoop.mapred.Reducer#reduce(java.lang.Object, java.util.Iterator, org.apache.hadoop.mapred.OutputCollector, org.apache.hadoop.mapred.Reporter)
        */
        public void reduce(Text key, Iterator<Text> values, Context context) throws IOException, InterruptedException {
            String csv = "";
            while(values.hasNext()){
                if(csv.length() > 0) csv += ",";
                csv += values.next().toString();
            }
            context.write(key, new Text(csv));
        }
    }


    public static void main(String[] args) throws Exception {

        //判断输入参数
        if (args.length != 2) {
            System.err.println("Usage: <input path> <output path>");
            System.exit(-1);
        }


        Configuration conf = new Configuration();

        //设置Master URL
        //conf.set("fs.defaultFS","hdfs://10.103.250.10:8020");
        conf.set("fs.defaultFS","hdfs://127.0.0.1:9000");
        Job job = new Job(conf, "MRVideoReader");
        job.setJarByClass(MRVideoReader.class);

        Path in = new Path(args[0]);
        Path out = new Path(args[1]);

        job.setInputFormatClass(VideoInputFormat.class);
        job.setMapperClass(MRVideoReaderMapper.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setReducerClass(MRVideoReaderReducer.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setJarByClass(MRVideoReader.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, in);
        FileOutputFormat.setOutputPath(job, out);

        System.exit(job.waitForCompletion(true)?0:1);
    }

}

