package se.wendt.android.wifipclock;

import java.util.Date;

import se.wendt.android.util.Logger;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * Displays records for the given SSID.
 */
public class NetworkSummaryActivity extends ListActivity {

	private static final Logger logger = Logger.getLogger(NetworkSummaryActivity.class);
	private static final String INTENT_EXTRA_KEY_NETWORK_NAME = NetworkSummaryActivity.class.getName() + "_SSID";
	
	private SsidLog storage;
	private SimpleCursorAdapter adapter;
	private Cursor dataCursor;
	private static final String[] columns = "day,date,hours,minutes,note".split(",");
	private static final int[] uiFields = { R.id.network_summary_row_day, R.id.network_summary_row_date,
			R.id.network_summary_row_hours, R.id.network_summary_row_minutes, R.id.network_summary_row_note };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		logger.debug("onCreate");
		super.onCreate(savedInstanceState);

		setTitle(getNetworkNameFromIntent());
		setupStorage();
		setupDataCursor();

		Context context = getApplicationContext();
		adapter = new SimpleCursorAdapter(context, R.layout.network_summary_row, dataCursor, columns, uiFields);
		setListAdapter(adapter);

		logger.debug("onCreate done");
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		logger.debug("onListItemClick");
		super.onListItemClick(l, v, position, id);
		Cursor record = (Cursor) getListView().getItemAtPosition(position);
		String networkName = getNetworkNameFromIntent();
		Date date = new Date(System.currentTimeMillis());
		Intent intent = NetworkDetailsActivity.createIntentUsedToStartActivity(this, networkName, date);
		startActivity(intent);
		logger.debug("onListItemClick done");
	}

	private String getNetworkNameFromIntent() {
		Intent intent = getIntent();
		return intent.getStringExtra("ssid");
	}

	private void setupStorage() {
		if (storage == null) {
			storage = new SsidLog(getApplicationContext());
		}
	}

	private void setupDataCursor() {
		String networkName = getNetworkNameFromIntent();
		dataCursor = storage.getRecordsForWifiNetwork(networkName);
	}

	@Override
	protected void onPause() {
		super.onPause();
		logger.debug("onPause");
		dataCursor.close();
		dataCursor = null;
		storage.close();
		storage = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		logger.debug("onResume");
		setupStorage();
		setupDataCursor();
		adapter.changeCursor(dataCursor);
		adapter.notifyDataSetChanged(); // FIXME: not needed?
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add("FIXME");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	public static Intent createIntentUsedToStartActivity(Activity parentActivity, String networkName) {
		Intent intent = new Intent(parentActivity, NetworkSummaryActivity.class);
		intent.putExtra(INTENT_EXTRA_KEY_NETWORK_NAME, networkName);
		return intent;
	}

}
