package com.aber.ac.uk.sym1.cyclisttrack;

/**
 * This class recognizes the Mat image processed by openCV. It creates a folder on the external memory
 * for tesseract files.
 * Code created thanks to Gautam Gupta guide:
 * http://gaut.am/making-an-ocr-android-app-using-tesseract/
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

public class NumberDetect extends Thread {

	private TessBaseAPI baseApi;
	protected Context context;
	public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/CyclistAppData/";
	public static final String lang = "eng";
	AssetManager manager;

	public NumberDetect(Context context) {
		manager = context.getAssets();
	}

	public void run() {

		try {
			createDataFolder();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (MainActivity.ThreadRun) {

			UnrecognizedPicture p = MainActivity.unrecognized.poll();
			if (p != null) {
				//long timeStart = System.currentTimeMillis();
				String numberOnPlate = numPlateOCR(p.getPlate());
				//long timeEnd = System.currentTimeMillis();
				//System.out.println("*plate number recognized in "+ (timeEnd - timeStart));
				MainActivity.recognized.add(new RecognizedPicture(p.getTime(),numberOnPlate));
				System.gc();
				Highgui.imwrite(DATA_PATH + p.getMilisecTime() + ".jpg",p.getPlate()); // saves every detected plate picture on
										// the external storage with time in
										// milisec as name of the file
			}
			if (p == null) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * numPlateOCR method converts Mat plate to bitmap, then recognizes the
	 * number thanks to tesseract OCR
	 * 
	 * @param plate
	 *            - Mat image processed by openCV
	 * @return recognizedText: plate number
	 */
	public String numPlateOCR(Mat plate) {

		baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		baseApi.init(DATA_PATH, lang);

		Bitmap bitmap = Bitmap.createBitmap(plate.width(), plate.height(),Bitmap.Config.ARGB_8888);
		org.opencv.android.Utils.matToBitmap(plate, bitmap);
		// Convert bitmap to ARGB_8888 which is required by tesseract
		bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

		baseApi.setVariable("tessedit_char_whitelist", "1234567890"); // setting up tesseract to recognize only numbers
		baseApi.setImage(bitmap);
		String recognizedText = baseApi.getUTF8Text();
		baseApi.end();

		Log.v("OCR RESULT", "OCRED TEXT: " + recognizedText);
		return recognizedText;
	}

	/**
	 * This method creates folder for tesseract data required by tesseract on
	 * the external strorage
	 * 
	 * @throws IOException
	 */
	public void createDataFolder() throws IOException {
		File directory = new File(DATA_PATH + "tessdata/");
		if (!directory.exists()) {
			if (directory.mkdirs()) {
				copyTessData();
				Log.e("***PassLog*** ", "Folder has been created");
			} else
				Log.e("***FailLog*** ", "Could not create folder");
		}
	}

	/**
	 * This method copies eng.traineddata file from assets folder to the folder
	 * created by method createDataFolder(). This file is required by tesseract
	 * as contains trained data for character recognition.
	 * 
	 * @throws IOException
	 */
	public void copyTessData() throws IOException {
		InputStream inStream = manager.open("tessdata/eng.traineddata");
		OutputStream outStream = new FileOutputStream(DATA_PATH + "tessdata/eng.traineddata");
		// Bytes from In transfer to Out
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inStream.read(buffer)) > 0) {
			outStream.write(buffer, 0, length);
		}
		inStream.close();
		outStream.close();
		Log.e("***PassLog*** ", "File eng.traineddata has been copied");
	}

}
