package se.wendt.android.wifipclock;

import se.wendt.android.util.Logger;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class NetworkListItemClickListener implements OnItemClickListener {

	private static final Logger logger = Logger.getLogger(NetworkListItemClickListener.class);

	private final ListView listView;
	private final NetworkListActivity parentActivity;

	public NetworkListItemClickListener(NetworkListActivity parentActivity, ListView listView) {
		this.parentActivity = parentActivity;
		this.listView = listView;
	}

	public void onItemClick(AdapterView<?> parentView, View clickedListItemView, int position, long idOfClickedListItem) {
		logger.debug("onListItemClick");
		Cursor itemAtPosition = (Cursor) listView.getItemAtPosition(position);
		int columnIndex = itemAtPosition.getColumnIndexOrThrow("wlan");
		String networkName = itemAtPosition.getString(columnIndex);
		Intent intent = NetworkSummaryActivity.createIntentUsedToStartActivity(parentActivity, networkName);
		parentActivity.startActivity(intent);
		logger.debug("onListItemClick done");
	}

}
