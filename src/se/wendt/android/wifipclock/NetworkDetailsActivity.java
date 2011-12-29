package se.wendt.android.wifipclock;

import java.util.Date;

import se.wendt.android.util.Logger;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
	private ListAdapter listAdapter;

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

		setTitle(getTitleFromIntent());

		setContentView(R.layout.network_details);

		// FIXME : change to label / read only
		TextView noteView = (TextView) findViewById(R.id.network_details_note);
		noteView.setText("This is the note I wrote for this very fine day which apparently is something like "
				+ getTitleFromIntent() + " or something quite similar. This is a very long note.");

		ListView listView = (ListView) findViewById(R.id.network_details_record_list);
		setupListAdapter();
		listView.setAdapter(listAdapter);
		listView.setClickable(false);

		logger.debug("onCreate done");
	}

	private void setupListAdapter() {
		setupCursor();
		int[] to = { R.id.network_details_list_row_start, R.id.network_details_list_row_end,
				R.id.network_details_list_row_duration };
		String[] from = "start,end,duration".split(",");
		listAdapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.network_details_list_row, dataCursor,
				from, to);
	}

	private void setupCursor() {
		storage = new SsidLog(getApplicationContext());
		String networkName = getNetworkNameFromIntent();
		Date date = getDateFromIntent();
		dataCursor = storage.getRecordsForWifiNetworkAndDate(networkName, date);
	}

	private Date getDateFromIntent() {
		Intent intent = getIntent();
		Long datePointInTime = intent.getLongExtra(INTENT_EXTRA_KEY_DATE, -1);
		return new Date(datePointInTime);
	}

	private String getNetworkNameFromIntent() {
		Intent intent = getIntent();
		String networkName = intent.getStringExtra("ssid");
		return networkName;
	}

	private String getTitleFromIntent() {
		String networkName = getNetworkNameFromIntent();
		return getDateFromIntent() + " " + networkName;
	}

}
