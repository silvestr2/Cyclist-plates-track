package com.aber.ac.uk.sym1.cyclisttrack;
/**
 * Activity which holds menu buttons. It is a first activity that starts when application is opened. 
 * @author Sylwester Mazur
 */
import com.aber.ac.uk.sym1.cyclisttrack.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

public class MenuActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		View start_button = findViewById(R.id.start_button);
		start_button.setOnClickListener(this);
		View list_button = findViewById(R.id.list_button);
		list_button.setOnClickListener(this);
		View quit_button = findViewById(R.id.quit_button);
		quit_button.setOnClickListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start_button:
			Intent in1 = new Intent(MenuActivity.this, MainActivity.class);
			startActivity(in1);
			// finish();
			break;
		case R.id.list_button:
			Intent in2 = new Intent(MenuActivity.this, ListActivity.class);
			startActivity(in2);
			// finish();
			break;
		case R.id.quit_button:
			finish();
			System.exit(0);

		}
	}

}
