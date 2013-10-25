package com.aber.ac.uk.sym1.cyclisttrack;

/**
 * This class contains listview on which scores are shown
 * @author Sylwester Mazur
 */
import java.util.ArrayList;

import com.aber.ac.uk.sym1.cyclisttrack.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ListActivity extends Activity {

	ListView list;
	public ArrayList<String> listData = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		list = (ListView) findViewById(R.id.listView1);
		createListView();
	}

	private void createListView() {

		if (MainActivity.raceMan==null || MainActivity.raceMan.getSorted().isEmpty()) {
			setContentView(R.layout.empty_list);
		}

		 else {
			for (RecognizedPicture rp : MainActivity.raceMan.getSorted()) {
				listData.add(rp.getRaceNumber() + " - " + rp.getTime());
			}
			// Adding arraylist to the adapter by which data about cyclist will be shown in the listview
			list.setAdapter(new ArrayAdapter<String>(ListActivity.this, android.R.layout.simple_list_item_1, listData));
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list, menu);
		return true;
	}

}
