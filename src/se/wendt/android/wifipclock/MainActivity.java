package se.wendt.android.wifipclock;

import static java.util.Arrays.asList;
import se.wendt.android.util.Logger;
import se.wendt.android.wifipclock.services.WifiScanService;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends ListActivity {

	private static final Logger logger = Logger.getLogger(MainActivity.class);
	private SsidLog storage;
	private SimpleCursorAdapter adapter;
	private final Scheduler scheduler;

	public MainActivity() {
		scheduler = new Scheduler();
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Context context = getApplicationContext();
		storage = new SsidLog(context);
		Cursor records = setupAdapter(context);
		setListAdapter(adapter);
		setTitle(R.string.list_view_title);
		logger.info("onCreate done with %s records in the db", records.getCount());
		scheduler.scheduleNextScan(context);
	}

	@Override
	protected void onPause() {
		super.onPause();
		storage.close();
		storage = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.storage = new SsidLog(getApplicationContext());
		updateCursor();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Add");
		menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Refresh");
		menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Report bug");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		CharSequence title = item.getTitle();
		logger.debug("option %s selected", title);
		if (title.equals("Refresh")) {
			updateCursor();
		}
		if (title.equals("Add")) {
			storage.recordSeenWlans(asList("add"), System.currentTimeMillis());
		}
		if (title.equals("Report bug")) {
			getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_BUG_REPORT));
		}
		return true;
	}

	protected Cursor setupAdapter(Context context) {
		Cursor records = storage.getRecords();
		String[] columns = "wlan,begin,end".split(",");
		int[] textViews = { R.id.record_wlan, R.id.record_begin, R.id.record_end };
		adapter = new SimpleCursorAdapter(context, R.layout.rowlayout, records, columns, textViews);
		return records;
	}

	private void updateCursor() {
		Cursor records = storage.getRecords();
		logger.debug("updated cursor holds %s records", records.getCount());
		adapter.changeCursor(records);
	}

}
