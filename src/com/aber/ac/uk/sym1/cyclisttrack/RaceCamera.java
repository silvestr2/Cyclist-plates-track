package com.aber.ac.uk.sym1.cyclisttrack;
/**
 * This class sets up camera to the given parameters and passes stream of data to the native method PlateDetect 
 * This class is based on object tracking from: http://projectproto.blogspot.co.uk/
 * SVN source code: http://yus-repo.googlecode.com/svn/trunk/Android/apps/objtrack
 * Changed: processFrame method to make it suitable for my project purpose, PlateDetect method added, loading additional
 * library added: opencv_java to use opencv specific objects (in this example Mat images)
 */

import java.io.IOException;
import java.util.List;

import org.opencv.core.Mat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class RaceCamera extends SurfaceView implements SurfaceHolder.Callback,
		Runnable {
	private static final String TAG = "Sample::SurfaceView";

	private Camera mCamera;
	private SurfaceHolder mHolder;
	private int mFrameWidth;
	private int mFrameHeight;
	private byte[] mFrame;
	private boolean mThreadRun;
	private byte[] mBuffer;
	private int mFrameSize;
	private Bitmap mBitmap;
	private Mat plate;
	private int[] mRGBA, mRGBAPlate;

	public RaceCamera(Context baseLoaderCallback) {
		super(baseLoaderCallback);

		mHolder = getHolder();
		mHolder.addCallback(this);
		Log.i(TAG, "Instantiated new " + this.getClass());
	}
/**
 * This method gets camera frame width
 * @return mFrameWidth
 */
	public int getFrameWidth() {
		return mFrameWidth;
	}
/**
 * This method gets camera frame height
 * @return mFrameHeight
 */
	public int getFrameHeight() {
		return mFrameHeight;
	}
/**
 * This methods sets camera review
 * @throws IOException
 */
	public void setPreview() throws IOException {
		mCamera.setPreviewDisplay(null);
	}
/**
 * This method is on when surface is changed, camera parameters are set here to get camera optimal preview size
 * 
 */
	@Override
	public void surfaceChanged(SurfaceHolder _holder, int format, int width,
			int height) {
		Log.i(TAG, "surfaceCreated");
		if (mCamera != null) {
			Camera.Parameters params = mCamera.getParameters();
			List<Camera.Size> sizes = params.getSupportedPreviewSizes();
			mFrameWidth = width;
			mFrameHeight = height;

			// selecting optimal camera preview size
			{
				int minDiff = Integer.MAX_VALUE;
				for (Camera.Size size : sizes) {
					if (Math.abs(size.height - height) < minDiff) {
						mFrameWidth = size.width;
						mFrameHeight = size.height;
						minDiff = Math.abs(size.height - height);
					}
				}
			}

			params.setPreviewSize(getFrameWidth(), getFrameHeight());

			List<String> FocusModes = params.getSupportedFocusModes();
			if (FocusModes
					.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
				params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
			}

			mCamera.setParameters(params);
			// ******************************************
			/* Now allocate the buffer */
			params = mCamera.getParameters();
			int size = params.getPreviewSize().width * params.getPreviewSize().height;
			size = size * ImageFormat.getBitsPerPixel(params.getPreviewFormat())/ 8;
			mBuffer = new byte[size];
			/* The buffer where the current frame will be coppied */
			mFrame = new byte[size];
			mCamera.addCallbackBuffer(mBuffer);

			try {
				setPreview();
			} catch (IOException e) {
				Log.e(TAG,
						"mCamera.setPreviewDisplay/setPreviewTexture fails: "
								+ e);
			}

			/*
			 * Notify that the preview is about to be started and deliver
			 * preview size
			 */
			onPreviewStarted(params.getPreviewSize().width,
					params.getPreviewSize().height);

			/* Now we can start a preview */
			mCamera.startPreview();
		}
	}
/**
 * Method runs when the surface of camera preview is changed.
 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG, "surfaceCreated");
		mCamera = Camera.open();

		mCamera.setPreviewCallbackWithBuffer(new PreviewCallback() {
			@Override
			public void onPreviewFrame(byte[] data, Camera camera) {
				synchronized (RaceCamera.this) {
					try {
						System.arraycopy(data, 0, mFrame, 0, data.length);
						RaceCamera.this.notify();
						camera.addCallbackBuffer(mBuffer);
					} catch (Exception e) {
						System.out.println("array error !!!");
					}
				}
			}
		});

		(new Thread(this)).start();

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, "surfaceDestroyed");
		mThreadRun = false;
		if (mCamera != null) {
			synchronized (this) {
				mCamera.stopPreview();
				mCamera.setPreviewCallback(null);
				mCamera.release();
				mCamera = null;
			}
		}
		onPreviewStopped();
	}

	/**
	 * This method process the frame taken from camera. Processing occurs in native method defined below.
	 * @param data camera preview data (frames)
	 * @return
	 */
	protected Bitmap processFrame(byte[] data) {
		int[] rgba = mRGBA;

		UnrecognizedPicture unPic = new UnrecognizedPicture();

		plate = new Mat();

		try {
			//long timeStart = System.currentTimeMillis();
			int succes = PlateDetect(getFrameWidth(), getFrameHeight(), data, rgba, plate.getNativeObjAddr()); //native method to detect the plate number
			//long timeEnd = System.currentTimeMillis();
			//System.out.println("plate found and image processed in "+ (timeEnd - timeStart));
			
			if (succes == 1 && !plate.empty()) {
				unPic.setPlate(plate);
				MainActivity.unrecognized.add(unPic);
				
			}

			Bitmap bmp = mBitmap;
			bmp.setPixels(rgba, 0/* offset */, getFrameWidth() /* stride */, 0, 0, getFrameWidth(), getFrameHeight());
			return bmp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	void onPreviewStarted(int previewWidtd, int previewHeight) {
		mFrameSize = previewWidtd * previewHeight;
		mRGBA = new int[mFrameSize];
		mBitmap = Bitmap.createBitmap(previewWidtd, previewHeight,
				Bitmap.Config.ARGB_8888);
	}

	void onPreviewStopped() {
		if (mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}
		mRGBA = null;
	}

	@Override
	public void run() {
		 mThreadRun = true;

		Log.i(TAG, "Starting processing thread");
		while (MainActivity.ThreadRun) {
			Bitmap bmp = null;

			synchronized (this) {
				try {
					this.wait();
					//System.out.println("TIME (frames)"+System.currentTimeMillis());
					bmp = processFrame(mFrame);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (bmp != null) {
				Canvas canvas = mHolder.lockCanvas();
				if (canvas != null) {
					canvas.drawBitmap(bmp, (canvas.getWidth() - getFrameWidth()) / 2, (canvas.getHeight() - getFrameHeight()) / 2, null);
					mHolder.unlockCanvasAndPost(canvas);
				}
			
			}
		}
	}

	public native int PlateDetect(int width, int height, byte yuv[], int[] rgba, long processedFrame);

	static {

		System.loadLibrary("opencv_java");
		System.loadLibrary("cyclisttrack_opencv_jni");

	}
}