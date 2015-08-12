/*
 * Copyright (c) 2011. Philipp Wagner <bytefish[at]gmx[dot]de>.
 * Released to public domain under terms of the BSD Simplified license.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of the organization nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *   See <http://www.opensource.org/licenses/bsd-license>
 */

#include "obdetect.h"
/*
	g++ FaceRecognizer.cpp -lopencv_core -lopencv_highgui -lopencv_contrib -lopencv_objdetect -lopencv_imgproc -o recognizer
*/
using namespace cv;
using namespace std;


void detectAndPres(cv::Mat image, std::string position, std::string arriveTime) {
	
	Ptr<FaceRecognizer> model = createLBPHFaceRecognizer();
	std::cout << "Work method using LBPHFaceRecognizer : LBPH." <<std::endl;
	
	
	// ###################### Classifier Create Start #############################
	
	cv::CascadeClassifier faceCascade;
	if(!faceCascade.load("/etc/nita/haarcascade_frontalface_alt.xml")){
		std::cout << "detector file is wrong!" << std::endl;
		return ;
	}

	std::cout << "Loaded classifier_file from file: /etc/nita/haarcascade_frontalface_alt.xml" << std::endl;
	
	// ###################### Classifier Create End #############################
	
	// load model library from file
	model->load("/etc/nita/facedata_lbph_java.xml");
	std::cout << "Loaded model_library from file: /etc/nita/facedata_lbph_java.xml" << std::endl;
	
	// create a VideoCapture
	/*cv::VideoCapture capture(video_path);	
	if(!capture.isOpened()){
		
		std::cout << "Can't open the video!" <<std::endl;
		return 0;
	}
	std::cout << "Loaded video_file from file: " << video_path << std::endl;
	*/

	
	//
	cv::Mat originalFaceImg;
	cv::Mat analysisFaceImg;
	std::vector<cv::Rect> faces;
		
	// ###################### Face Detector Start #############################

	faceCascade.detectMultiScale(image, faces, 1.2, 2, CV_HAAR_SCALE_IMAGE, cv::Size(20,20), cv::Size(200,200));
		
	//cvReleaseMemStorage( &storage );	
	// ###################### Face Detector End #############################
	
	
	// ###################### Face Recognizer Start #############################
	for(std::vector<cv::Rect>::iterator iter = faces.begin(); iter != faces.end(); ++iter){
		
		// obtain a face presented a rectangle in frame
		originalFaceImg = cv::Mat(image, *iter);
		
		// set color
		cv::cvtColor(originalFaceImg,originalFaceImg,CV_RGB2GRAY);
		
		// construct a standard analysis face image
		Size dsize=Size(180,200);
		analysisFaceImg=cv::Mat(dsize,CV_32S);
		cv::resize(originalFaceImg,analysisFaceImg,dsize);
		
		// predict
	    	int predictedLabel = -1;
		double confidence = 0.0;
		
		model->predict(analysisFaceImg, predictedLabel, confidence);
	      
		string result_message = format("PredictedClass %d Distance %f", predictedLabel,confidence);
         	cout << result_message << endl;		
	}

}
