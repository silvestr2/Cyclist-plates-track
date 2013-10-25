#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgproc/imgproc_c.h>
#include <opencv2/features2d/features2d.hpp>
#include <vector>
#include <highgui.h>
#include <android/log.h>
#include <cmath>
#define LOG_TAG "foo"

using namespace std;
using namespace cv;

/**
 * This class (find squares/rectangles and printing them off on the screen)
 *was based on the openCV squares.cpp example to detect those kind of
 * objects. Online repository is:
 *https://code.ros.org/trac/opencv/browser/trunk/opencv/samples/cpp/squares.cpp?rev=4079
 * I have adjusted find squares function to look for those in color range
 *specified by me, not as in squares.cpp where it was looking for all square/rectangle patterns
 * in a picture.
 */

// Calculate cosine from dot product between two vectors made by three points
double getCosine(Point pt1, Point pt2, Point pt0) {
	double dx1 = pt1.x - pt0.x;
	double dy1 = pt1.y - pt0.y;
	double dx2 = pt2.x - pt0.x;
	double dy2 = pt2.y - pt0.y;

	double nominator = (dx1 * dx2 + dy1 * dy2);

	// add a small epsilon in denominator to make sure we don't divide by zero
	double denominator = sqrt(
			(dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);

	return (nominator / denominator);
}

Rect getRegionOfInterest(const vector<Point> &theBiggestSq, Mat image) {

	RotatedRect box = minAreaRect(Mat(theBiggestSq));
	Rect roi;

	//http://stackoverflow.com/questions/13803728/opencv-crop-function-fatal-signal-11
	// Proposed data to create rectangle
	int propX = box.center.x - ((box.size.width - (box.size.width * 0.2)) / 2);
	int propY = box.center.y
			- ((box.size.height - (box.size.height * 0.2)) / 2);
	int propW = box.size.width - (box.size.width * 0.2);
	int propH = box.size.height - (box.size.height * 0.2);

	// check if top-left edge is within image
	if (propX < 0) {
		roi.x = 0;
	} else {
		roi.x = propX;
	}

	if (propY < 0) {
		roi.y = 0;
	} else {
		roi.y = propY;
	}

	// check if bottom-right edge is within image
	if ((roi.x - 1 + propW) > image.cols) { // Will this roi exceed image?
		roi.width = (image.cols - 1 - roi.x); // YES: make roi go to image edge
	} else {
		roi.width = propW;
	// NO: continue as proposed
}

// Similar for height
	if ((roi.y - 1 + propH) > image.rows) {
		roi.height = (image.rows - 1 - roi.y);
	} else {
		roi.height = propH;
	}

	//rectangle/square angle
	double angle = box.angle;
	if (angle < -45.)
		angle += 90.;

	//rotating the rectangle/square by an angle
	Mat rot_mat = getRotationMatrix2D(box.center, angle, 1);
	warpAffine(image, image, rot_mat, image.size(), INTER_CUBIC);

	// imwrite("/sdcard/CyclistAppData/rotated.jpg",image);

	// __android_log_print(ANDROID_LOG_INFO, LOG_TAG,"cords %d %d %d %d",roi.x, roi.y, roi.width, roi.height);
	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, "angle %f", angle);

	return roi;
}

// Compute and return threshold that will be used to cast grayscale image to
// binary version
int computeCutoutThreshold(const Mat &image) {
	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, "color %s", "***");
	// Image submitted to this function should be a grayscale version of image
	// which has only two colors - black for text and yellow-ish for background.
	// However, light illumination can distort those a bit, so to establish
	// 'color of black' in actual image we computed binned
	// (ten intensities per bin) histogram of color intensities
	// Then find first local maximum starting from intensity = 0 side and use
	// n elements away (can adjust, at the moment code was written used n = 5)
	// as a threshold that will be later on used to cast image to binary

	// Create bins
	int a[26]; // each element represents an intensity range of 10 values
	for (int i = 0; i < 26; i++) {
		a[i] = 0;
	}

	// Compute binned histogram
	int color;
	for (int i = 0; i < image.rows; i++) {
		for (int j = 0; j < image.cols; j++) {
			color = image.at<uchar>(i, j);
			a[color / 10]++; // color in given range recorded at (i,j) pixel
		}
	}

	// Log
	for (int i = 0; i < 26; i++) {
		__android_log_print(ANDROID_LOG_INFO, LOG_TAG, "color %d", a[i]);
	}

	int c = 1;
	///example: if we computed that the most of black color in this picture is up to 35, there can still be more black pixels beyond that 35 (that were affected by illumination), so im setting up a safe line:in this case '4' means: as we are taking every 10, 10*4=40, so the threshold value returned will be 35+40=75, so above that line we will change every color to white

	int max_min = 5;
	while (a[c] > a[c - 1] || max_min > 0) {
		if (a[c] <= a[c - 1])
			max_min--;
		c++;
	}
	return (c * 10);
}

// Crop image to the ROI defined on it
void castBrightPixelsToWhite(Mat &croppedImage) {
	// Change to grayscale
	cvtColor(croppedImage, croppedImage, CV_BGR2GRAY);

	//imwrite("/sdcard/CyclistAppData/greybeforefinal.jpg",croppedImage);

	// Compute and return threshold that will be used to split image into black and bright pixels
	int threshold = computeCutoutThreshold(croppedImage);

	//change every pixel to white if it is sufficiently bright
	//#pragma omp parallel for
	for (int i = 0; i < croppedImage.rows; i++)
		for (int j = 0; j < croppedImage.cols; j++)
			if (croppedImage.at<uchar>(i, j) > threshold) {
				croppedImage.data[croppedImage.channels()
						* (croppedImage.cols * i + j)] = 255;
			}
	//imwrite("/sdcard/CyclistAppData/finalpicture.jpg",croppedImage);
}

void findLargestSquare(const vector<vector<Point> > &squares,
		vector<Point> &theBiggestSquare) {

	int max_width = 0;
	int max_height = 0;
	int max_square_idx = 0;
	//#pragma omp parallel for
	for (size_t i = 0; i < squares.size(); i++) {

		// Convert a set of 4 unordered Points into a meaningful cv::Rect structure.
		Rect rectangle = boundingRect(Mat(squares[i]));

		// Store the index position of the biggest square found
		if ((rectangle.width >= max_width)
				&& (rectangle.height >= max_height)) {
			max_width = rectangle.width;
			max_height = rectangle.height;
			max_square_idx = i;
		}

	}

	// theBiggestSquare is used to return the result
	theBiggestSquare = squares[max_square_idx];
}

extern "C" {
JNIEXPORT int JNICALL Java_com_aber_ac_uk_sym1_cyclisttrack_RaceCamera_PlateDetect(JNIEnv* env, jobject thiz,
		jint width, jint height, jbyteArray yuv, jintArray bgra, jlong processedFrame)
{
	try {

		vector<vector<Point> > squares;

		// First transform data to CV objects
		jbyte* _yuv = env->GetByteArrayElements(yuv, 0);
		jint* _bgra = env->GetIntArrayElements(bgra, 0);

		Mat mYuv(height + height/2, width, CV_8UC1, (unsigned char *)_yuv);
		Mat mBgra(height, width, CV_8UC4, (unsigned char *)_bgra);
		Mat& pFrame = *((Mat*) processedFrame);

		CvSize size =cvSize(width, height);
		IplImage *hsv_frame = cvCreateImage(size, IPL_DEPTH_8U, 3);
		IplImage *thresholded = cvCreateImage(size, IPL_DEPTH_8U, 1);

		IplImage img_color = mBgra;

		//Pay attention to BGRA byte order
		//ARGB stored in java as int array becomes BGRA at native level
		cvtColor(mYuv, mBgra, CV_YUV420sp2BGR, 4);

		// convert to HSV color-space
		cvCvtColor(&img_color, hsv_frame, CV_BGR2HSV);

		// Filter out colors which are out of range BGR.
		cvInRangeS(hsv_frame, cvScalar(20, 70, 80, 0), cvScalar(70, 255, 255, 0), thresholded);

		// some smoothing of the image
		cvSmooth( thresholded, thresholded, CV_GAUSSIAN, 9, 9 );

		//Ipl to Mat picture convert
		Mat image = cvarrToMat(thresholded);
		//imwrite("/sdcard/CyclistAppData/filteredsmoothed.jpg",image);

		Mat pyr, timg;

		// down-scale and upscale the image to filter out the noise
		pyrDown(image, pyr, Size(image.cols/2, image.rows/2));
		pyrUp(pyr, timg, image.size());
		vector< vector<Point> > contours;

		// find contours
		findContours(image, contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);

		vector<Point> approx;

		// test each contour for being a square
		for( size_t i = 0; i < contours.size(); i++ )
		{
			// approximate contour with accuracy proportional
			// to the contour perimeter
			approxPolyDP( Mat(contours[i]), approx,
					arcLength( Mat(contours[i]) , true) * 0.02, true);

			// square contours should have 4 vertices after approximation
			// relatively large area (to filter out noisy contours)
			// and be convex.
			// Note: absolute value of an area is used because
			// area may be positive or negative - in accordance with the
			// contour orientation
			if( approx.size() == 4 &&
					fabs( contourArea( Mat(approx) ) ) > 1000 &&
					isContourConvex( Mat(approx) ) )
			{
				double maxCosine = 0;

				// Examine three out of four angles inside the contour object
				for( int j = 2; j < 5; j++ )
				{
					// find the maximum cosine of the angle between joint edges
					double cosine =
					fabs( getCosine(approx[j%4], approx[j-2], approx[j-1]) );

					maxCosine = MAX(maxCosine, cosine);
				}

				// if cosines of all angles are small
				// (all angles are ~90 degree) then contour represents square/rectangle
				if( maxCosine < 0.3 )
				squares.push_back(approx);

			}

		}

		//////find the largest one/////
		if ( !squares.empty() ) {

			vector<Point> theBiggestSq;
			findLargestSquare(squares, theBiggestSq);

			Rect roi = getRegionOfInterest(theBiggestSq, mBgra);

			pFrame = mBgra(roi);
			//imwrite("/sdcard/CyclistAppData/aftercrop.jpg",desc);
			// cast 'pFrame' pixels into white and rest
			castBrightPixelsToWhite(pFrame);

		}

		//draw//
		for( size_t i = 0; i < squares.size(); i++ )
		{
			const Point* p = &squares[i][0];
			int n = (int)squares[i].size();

			polylines(mBgra, &p, &n, 1, true, Scalar(255,255,0), 5, 10);
		}

		// cleanup resources
		cvReleaseImage(&hsv_frame);
		cvReleaseImage(&thresholded);
		//cvReleaseImage(&image);

		env->ReleaseIntArrayElements(bgra, _bgra, 0);
		env->ReleaseByteArrayElements(yuv, _yuv, 0);
		return 1;
	}
	catch(string w) {
		return 0;
	}
}
}

