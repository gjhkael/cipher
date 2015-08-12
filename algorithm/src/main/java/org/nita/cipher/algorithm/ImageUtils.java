package org.nita.cipher.algorithm;

import org.bytedeco.javacpp.opencv_core.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgproc.cvEqualizeHist;

public class ImageUtils {

	@SuppressWarnings("deprecation") //将字节数组转换为IplImage对象
	public static IplImage bytesToIplImage(byte[] bytes, int width, int height)
			throws IOException {
		// IplImage img = IplImage.create(720, 576, IPL_DEPTH_8U, 3);
		IplImage img = IplImage.create(width, height, IPL_DEPTH_8U, 1);
		img.getByteBuffer().put(bytes);
		return img;
	}
    //将IplImage对象转换为字节数组
	public static byte[] IplImageToBytes(IplImage img) {
		@SuppressWarnings("deprecation")
		ByteBuffer bufFrame = img.getByteBuffer();
		byte[] bFrame = new byte[bufFrame.remaining()];
		bufFrame.get(bFrame);
		return bFrame;

	}
    //BufferedImage装换为IplImage对象
    public static IplImage buffertoIplImage(BufferedImage buf){
        IplImage img = IplImage.createFrom(buf);
        return img;
    }
    //iplImage转换为Mat
    public static Mat iplImageToMat(IplImage img){
        Mat mat = new Mat(img);
        return mat;
    }
    //将图片保存到本地
    public static void saveImage(String key,IplImage img){
        cvSaveImage(key,img);
    }

    public static IplImage preprocessImage(IplImage image, CvRect r) {

        // 1. set a gray image whose size is the same as original image.
        IplImage gray = IplImage.create(cvGetSize(image), IPL_DEPTH_8U, 1);
        // cvCreateImage(cvGetSize(image), IPL_DEPTH_8U, 1),此函数会造成内存泄漏

        // 2. set width and height of interesting rectangle
        IplImage roi = IplImage.create(cvSize(100, 120), IPL_DEPTH_8U, 1);

        // 3. adjust the position based on the input argument r
        CvRect r1 = new CvRect();
        r1.x(r.x());
        r1.y(r.y());
        r1.width(r.width());
        r1.height(r.height());

        // 4. convert form original image to gray image
        cvCvtColor(image, gray, CV_BGR2GRAY);

        // 5. set interesting rectangle in gray image
        cvSetImageROI(gray, r1);

        // 6. scale the interesting region to the roi region
        cvResize(gray, roi, CV_INTER_LINEAR); // CV_INTER_CUBIC or
        // CV_INTER_LINEAR is good for
        // enlarging

        // 7. 用来使灰度图象直方图均衡化
        cvEqualizeHist(roi, roi);

        gray.release();

        return roi;

    }

}
