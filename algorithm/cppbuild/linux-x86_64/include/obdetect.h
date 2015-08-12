// g++ -shared -fPIC  -I ~/javacpp-presets-master/opencv/cppbuild/linux-x86_64/include  -I ~/javacpp-presets-master/ffmpeg/cppbuild/linux-x86_64/include  obdetect.cpp -o libDetection.so -lopencv_core -lopencv_highgui -lopencv_contrib -lopencv_objdetect -lopencv_imgproc


#include "iostream"
#include "opencv/cv.h"
#include "opencv2/contrib/contrib.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/features2d/features2d.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/contrib/detection_based_tracker.hpp"
#include <sys/time.h>
#include "fstream"


void detectAndPres(cv::Mat image, std::string position, std::string arriveTime);


