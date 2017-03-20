#include <jni.h>
#include <string>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>

using namespace std;
using namespace cv;

extern "C"
{
jstring
Java_com_ness_sample1_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

void JNICALL Java_com_ness_sample1_MainActivity_Gray(JNIEnv *env, jobject instance,
                                                     jlong matAddGray, jint nbrElem) {

    Mat &mGr = *(Mat *) matAddGray;
    for (int k = 0; k < nbrElem; k++) {
        int i = rand() % mGr.cols;
        int j = rand() % mGr.rows;
        mGr.at<uchar>(j, i) = 255;
    }
}

JNIEXPORT void JNICALL Java_com_ness_sample1_OMRActivity_CannyEdgeDetection(JNIEnv *, jobject,
                                                                             jlong addrGray,
                                                                             jlong addrRgba) {

    Mat &matGray = *(Mat *) addrGray;
    Mat &matRgb = *(Mat *) addrRgba;

    // Perform Canny edge detection
    Mat matGrayCanny;
    Canny(matGray, matGrayCanny, 10, 100);

    // Copy edge image into output
    cvtColor(matGrayCanny, matRgb, CV_GRAY2RGB); // BGR
}

JNIEXPORT void JNICALL Java_com_ness_sample1_MainActivity_CircleDetect(JNIEnv*, jobject, jlong addrGray, jlong addrRgba) {
    Mat& matGray  = *(Mat*)addrGray;
    Mat& matRgb = *(Mat*)addrRgba;

    // Resize to half resolution for faster processing
    Mat matGraySmall;
    int resizefactor = 2;
    resize(matGray, matGraySmall, Size(matGray.cols/resizefactor, matGray.rows/resizefactor));

    // Smooth the image so that not too many circles are detected
    GaussianBlur(matGraySmall, matGraySmall, Size(5,5), 2, 2);

    // Detect circles by Hough transform and draw
    vector<Vec3f> circles;
    HoughCircles(matGraySmall, circles, CV_HOUGH_GRADIENT, 2, matGraySmall.rows/8, 200, 100,
                 matGraySmall.cols/25, matGraySmall.cols/6);
    Point offset(-resizefactor, -resizefactor);
    for (int n = 0; n < circles.size(); n++) {
        Point center(cvRound(resizefactor*circles[n][0]), cvRound(resizefactor*circles[n][1]));
        int radius = cvRound(resizefactor*circles[n][2]);

        // Draw circle center
        circle(matRgb, center + offset, 3, Scalar(0,0,0), -1, 8, 0);
        circle(matRgb, center, 3, Scalar(255,255,0), -1, 8, 0);

        // Draw circle outline
        circle(matRgb, center + offset, radius, Scalar(0,0,0), 3, 8, 0);
        circle(matRgb, center, radius, Scalar(255,255,0), 3, 8, 0);
    }
}



}
