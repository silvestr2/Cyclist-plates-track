package com.aber.ac.uk.sym1.cyclisttrack;

/**
 * 
 * This class contains unrecognized picture data and recognized data.
 * @author Sylwester Mazur
 */


import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

	public static boolean ThreadRun = true;
	private static final String TAG = "ObjTrackActivity";
	LayoutInflater controlInflater = null;
	private MenuItem saveStats;
	public static boolean saveRaceStats = false;
	public static RaceManager raceMan;
	
	public static ConcurrentLinkedQueue<UnrecognizedPicture> unrecognized = new ConcurrentLinkedQueue<UnrecognizedPicture>();
	public static ConcurrentLinkedQueue<RecognizedPicture> recognized = new ConcurrentLinkedQueue<RecognizedPicture>();
	
	public MainActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(new RaceCamera(this));

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		NumberDetect numdet = new NumberDetect(this);
		numdet.start();

		raceMan = new RaceManager(this);
		raceMan.start();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "onCreateOptionsMenu");
		saveStats = menu.add("See statistics table");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "Menu Item selected " + item);
		if (item == saveStats) {
			try {
				raceMan.saveToXml();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Intent k = new Intent(this, ListActivity.class);
			startActivity(k);
		}
		return true;
	}

	public void onDestroy() {
		ThreadRun = false;
		super.onDestroy();
	}

	
}
