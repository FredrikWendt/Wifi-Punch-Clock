package se.wendt.android.wifipclock;

import se.wendt.android.util.Logger;
import se.wendt.android.wifipclock.services.WifiScanService;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

/**
 * The main view that lists the monitored SSIDs.
 */
// TODO: options: add (SSID), export (backup to file, send via e-mail),
// settings, more (import, about)
// TODO: context: view records, start/stop track SSID, clear all records, delete
public class ListSsidActivity extends ListActivity {

	private static final Logger logger = Logger.getLogger(ListSsidActivity.class);

	private static final int DIALOG_ADD = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		logger.debug("Go wild");
		super.onCreate(savedInstanceState);
		SsidLog storage = new SsidLog(getApplicationContext());
		Cursor networks = storage.getNetworks();
		String[] columns = { "wlan", "_id" };
		int[] uiFields = { R.id.list_ssid_row_name, R.id.list_ssid_row_billable_hours };
		ListAdapter adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.list_ssid_row, networks,
				columns, uiFields);
		setListAdapter(adapter);
		new Scheduler().scheduleNextScan(getApplicationContext());
		logger.debug("onCreate done");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add("Add");
		menu.add("Export");
		menu.add("Settings");
		menu.add("Import");
		menu.add("About");
		menu.add("Scan");
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return super.onCreateDialog(id);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if ("Add".equals(item.getTitle())) {
			showDialog(DIALOG_ADD);
		}
		if (item.getTitle().equals("Scan")) {
			getApplicationContext().startService(new Intent(getApplicationContext(), WifiScanService.class));
		}
		return super.onOptionsItemSelected(item);
	}

}
