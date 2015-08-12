package org.nita.cipher.algorithm;

/**
 * Created by havstack on 4/23/15.
 */

import org.bytedeco.javacpp.Loader;

import org.bytedeco.javacpp.opencv_contrib.FaceRecognizer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_objdetect.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;
import static org.nita.cipher.algorithm.ImageUtils.buffertoIplImage;

public class FaceRecog {

    private static String[] name = {"gjh", "mxf", "cch", "lzx"};
    private static String CASCADE_FILE = "/etc/nita/haarcascade_frontalface_alt.xml";
    // 通过该文件判断是否需要更新预测使用的模型
    private static String SYNC_FILE = "/etc/nita/sync_file.xml";
    private static long SYNC_FILE_LAST_MODIFIED_TIME;

    // private static String MODEL_FILE =
    // "E:\\opencv\\data\\facedata_fisher_java.xml";
    static {
        File f = new File(SYNC_FILE);
        long modify = f.lastModified();
        SYNC_FILE_LAST_MODIFIED_TIME = modify;
    }


    public static String faceDetect(ArrayList<BufferedImage> images) {
        String result = "";
        for (BufferedImage image : images) {
            IplImage img = buffertoIplImage(image);
            try {
                result = decUsingHarrAndrecFaceUsingFisher(img);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String decUsingHarrAndrecFaceUsingFisher(IplImage im)
            throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");


        FaceRecognizer model = null;
        IplImage grayImage = IplImage.create(im.width(),
                im.height(), IPL_DEPTH_8U, 1);

        cvCvtColor(im, grayImage, CV_BGR2GRAY);
        IplImage image = grayImage;

        CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(
                cvLoad(CASCADE_FILE));

        //System.out.println("cascade " + cascade.isNull() + " " + cascade);
        if (cascade.isNull()) {
            System.err.println("Error loading classifier file \""
                    + CASCADE_FILE + "\".");
            System.exit(1);
        }
        String res = "test";
        Loader.load(opencv_objdetect.class);
        Loader.load(opencv_core.class);
        CvMemStorage storage = CvMemStorage.create();
        cvClearMemStorage(storage); // clear memory
        // 可以指定 搜索窗口的最大值和最小值
        //cvEqualizeHist(image, image);
        CvSeq faces = cvHaarDetectObjects(image, cascade, storage, 1.2, 2,
                CV_HAAR_DO_CANNY_PRUNING, cvSize(20, 20), cvSize(400, 400));
        // Start to recognize faces detected in detection step.
        int[] ids = new int[1];
        double[] distance = new double[1];
        int label = -1;

        if (faces.total() > 0) {
            for (int j = 0; j < faces.total(); j++) {
                // 取得检测到的人脸框，以矩形表示
                CvRect r = new CvRect(cvGetSeqElem(faces, j));

                IplImage roi = IplImage.create(cvSize(100, 120),
                        IPL_DEPTH_8U, 1);
                CvRect r1 = new CvRect();
                r1.x(r.x());
                r1.y(r.y());
                r1.width(r.width());
                r1.height(r.height());
                cvSetImageROI(image, r1);
                cvResize(image, roi, CV_INTER_LINEAR); // CV_INTER_CUBIC or
                Mat m = new Mat(roi);
                try {
                    model.predict(m, ids, distance);
                } catch (Throwable e) {
                    return res;
                }

                String arrivaltime = sdf.format(System.currentTimeMillis());
                if (distance[0] <= 75) {
                    label = ids[0];
                    String discoverdTime = sdf.format(new Date(System
                            .currentTimeMillis()));
                    // make sure that label not be outofArray's boundary
                    if (label >= name.length || label < 0) {
                        label = 0;
                    }

                    res = res + "PredictedClass " + name[label]
                            + " Distance " + distance[0] + " Position "
                            + " ArrivedTime "
                            + " DiscoverdTime " + discoverdTime + "\n";

                    cvSaveImage("/tmp/data_res/" + name[label]
                            + "-" + discoverdTime
                            + ".jpg", roi);

                    String path = "/tmp/data_res/" + name[label]
                            + "-" + discoverdTime
                            + ".jpg";

                    String imageName = name[label] + "-"
                            + arrivaltime + "-" + discoverdTime + ".jpg";
                    File file = new File(path);

                        /*Picture_W info = new Picture_W(name[label],
                                arrivaltime, path, imageName);

                        SendInfo toSend = new SendInfo();
                        try {
                            toSend.send(info);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }*/

                    roi.release();
                    cvResetImageROI(image);

                } else {

                    String discoverdTime = sdf.format(new Date(System
                            .currentTimeMillis()));

                    res = res + "PredictedClass " + "unknown"
                            + " Distance " + distance[0] + " Position "
                            + " ArrivedTime "
                            + " DiscoverdTime " + discoverdTime + "\n";

                    cvSaveImage("/tmp/data_res/" + "unknown" + "-"
                            + "-" + "-" + discoverdTime
                            + ".jpg", roi);

                        /*String path = "/tmp/data_res/" + "unknown" + "-"
                                + "-" + "-" + discoverdTime
                                + ".jpg";

                        String imageName = "unknown" + "-" + "-"
                                + "-" + discoverdTime + ".jpg";

                        Picture_W info = new Picture_W("unknown", uri,
                                arrivaltime, path, imageName);

                        SendInfo toSend = new SendInfo();

                        try {
                            toSend.send(info);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }*/

                    roi.release();
                    cvResetImageROI(image);
                }
            }
        } else {
            String discoverdTime = sdf.format(new Date(System
                    .currentTimeMillis()));
            res = res + "PredictedClass None Distance infinite Position "
                    + " ArrivedTime "
                    + " DiscoverdTime " + discoverdTime + "\n";
        }

        cvReleaseHaarClassifierCascade(cascade);
        image.release();
        return res;
    }


}