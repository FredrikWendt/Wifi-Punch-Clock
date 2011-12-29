package se.wendt.android.wifipclock;

import static java.util.Arrays.asList;

import java.util.Date;

import se.wendt.android.util.Logger;
import se.wendt.android.wifipclock.services.WifiScanService;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * The main view that lists the monitored SSIDs.
 */
// TODO: options: add (SSID), export (backup to file, send via e-mail),
// settings, more (import, about)
// TODO: context: view records, start/stop track SSID, clear all records, delete
public class NetworkListActivity extends Activity {

	private static final Logger logger = Logger.getLogger(NetworkListActivity.class);
	private static final String[] columns = { "wlan", "_id" };
	private static final int[] uiFields = { R.id.list_ssid_row_name, R.id.list_ssid_row_billable_hours };
	private static final int DIALOG_ADD = 0;

	private SsidLog storage;
	private SimpleCursorAdapter adapter;
	private Cursor dataCursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		logger.debug("onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.network_list);
		Context context = getApplicationContext();
		
		setupStorage();
		setupCursor();
		adapter = new SimpleCursorAdapter(context, R.layout.network_list_row, dataCursor, columns, uiFields);
		ListView listView = (ListView) findViewById(R.id.network_list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new NetworkListItemClickListener(this, listView));
		
		new Scheduler().scheduleNextScan(context);
		
		logger.debug("onCreate done");
	}

	private void setupStorage() {
		storage = new SsidLog(getApplicationContext());
	}
	
	@Override
	protected void onResume() {
		logger.debug("onResume");
		super.onResume();
		setupStorage();
		setupCursor();
	}
	
	@Override
	protected void onPause() {
		logger.debug("onPause");
		super.onPause();
		releaseCursor();
		releaseStorage();
	}

	private void releaseStorage() {
		storage.close();
		storage = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem add = menu.add("Add");
		add.setIcon(android.R.drawable.ic_menu_add);
		
		MenuItem export = menu.add("Export");
		export.setIcon(android.R.drawable.ic_menu_save);
		
		MenuItem settings = menu.add("Settings");
		settings.setIcon(android.R.drawable.ic_menu_preferences);
		
		MenuItem about = menu.add("About");
		about.setIcon(android.R.drawable.ic_menu_help);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if ("Add".equals(item.getTitle())) {
			showDialog(DIALOG_ADD);
		}
		if ("About".equals(item.getTitle())) {
			storage.recordSeenWlans(asList("add"), new Date(System.currentTimeMillis()));
			refreshCursor();
			adapter.notifyDataSetChanged();
		}
		if (item.getTitle().equals("Scan")) {
			getApplicationContext().startService(new Intent(getApplicationContext(), WifiScanService.class));
		}
		return super.onOptionsItemSelected(item);
	}

	private void refreshCursor() {
		releaseCursor();
		setupCursor();
		adapter.changeCursor(dataCursor);
		adapter.notifyDataSetChanged();
	}

	private void setupCursor() {
		dataCursor = storage.getNetworks();
	}

	private void releaseCursor() {
		dataCursor.close();
		dataCursor = null;
	}

}
