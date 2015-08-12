package org.nita.cipher

import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.bytedeco.javacpp.BytePointer
import org.nita.cipher.hadoop.{ImageWritable, VideoInputFormat}
import org.apache.spark._
import org.nita.cipher.algorithm.{ImageUtils, FaceDetect}
/**
 * Created by guojianhua on 3/30/15.
 */
object AlgorithmOnSpark {
  def main(args: Array[String]) {
    if (args.length < 1) {
      System.err.println("Wrong hdfs path!!!")
      System.exit(1)
    }
    val sparkConf = new SparkConf().setAppName("coreTest").setMaster(args(0))
    val sc = new SparkContext(sparkConf)
    val file = sc.newAPIHadoopFile(args(1),classOf[VideoInputFormat], classOf[Text], classOf[ImageWritable])
      .map(key => AlgorithmOnSpark.detectAndPres(new BytePointer(ImageUtils.iplImageToMat(ImageUtils.buffertoIplImage(key._2.getBufferedImage)).asByteBuffer()),"192.168.1.211","1990909090")).foreach(println)
    sc.stop()
  }
}
