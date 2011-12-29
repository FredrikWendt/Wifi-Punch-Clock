package se.wendt.android.wifipclock;

import java.util.Date;

import se.wendt.android.util.Logger;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class NetworkDetailsActivity extends Activity {

	private static final Logger logger = Logger.getLogger(NetworkDetailsActivity.class);
	private static final String INTENT_EXTRA_KEY_SSID = NetworkDetailsActivity.class.getName() + "_SSID";
	private static final String INTENT_EXTRA_KEY_DATE = NetworkDetailsActivity.class.getName() + "_Date";

	private SsidLog storage;
	private Cursor dataCursor;
	private SimpleCursorAdapter listAdapter;
	private TextView noteView;
	private ListView listView;

	public static Intent createIntentUsedToStartActivity(Activity parent, String networkName, Date date) {
		Intent intent = new Intent(parent, NetworkDetailsActivity.class);
		intent.putExtra(INTENT_EXTRA_KEY_SSID, networkName);
		intent.putExtra(INTENT_EXTRA_KEY_DATE, date.getTime());
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		logger.debug("onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.network_details);
		
		Button next = (Button) findViewById(R.id.network_details_button_next_day);
		next.setOnClickListener(NetworkDetailsBrowseDayListener.next(this));
		Button previous = (Button) findViewById(R.id.network_details_button_previous_day);
		previous.setOnClickListener(NetworkDetailsBrowseDayListener.previous(this));

		noteView = (TextView) findViewById(R.id.network_details_note);
		listView = (ListView) findViewById(R.id.network_details_record_list);
		listView.setClickable(false);
		logger.debug("onCreate done");
	}
	
	@Override
	protected void onResume() {
		logger.debug("onResume");
		super.onResume();
		
		setTitle(getTitleFromIntent());
		setupCursor();
		setupListAdapter();
		noteView.setText(storage.getNoteForDate(getDateFromIntent()));
	}
	
	@Override
	protected void onPause() {
		logger.debug("onPause");
		super.onPause();
		
		dataCursor.close();
		dataCursor = null;
		storage.close();
		storage = null;
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		logger.debug("onNewIntent");
		super.onNewIntent(intent);
		setIntent(intent);
	}

	private void setupListAdapter() {
		if (listAdapter == null) {
			int[] to = { R.id.network_details_list_row_start, R.id.network_details_list_row_end,
					R.id.network_details_list_row_duration };
			String[] from = "start,end,duration".split(",");
			listAdapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.network_details_list_row, dataCursor,
					from, to);
			listView.setAdapter(listAdapter);
		} else {
			listAdapter.changeCursor(dataCursor);
			listAdapter.notifyDataSetChanged();
		}
	}

	private void setupCursor() {
		if (storage == null) {
			storage = new SsidLog(getApplicationContext());
			String networkName = getNetworkNameFromIntent();
			Date date = getDateFromIntent();
			dataCursor = storage.getRecordsForWifiNetworkAndDate(networkName, date);
		}
	}

	Date getDateFromIntent() {
		Intent intent = getIntent();
		Long datePointInTime = intent.getLongExtra(INTENT_EXTRA_KEY_DATE, -1);
		return new Date(datePointInTime);
	}

	String getNetworkNameFromIntent() {
		Intent intent = getIntent();
		String networkName = intent.getStringExtra("ssid");
		return networkName;
	}

	private String getTitleFromIntent() {
		String networkName = getNetworkNameFromIntent();
		return getDateFromIntent() + " " + networkName;
	}

}
