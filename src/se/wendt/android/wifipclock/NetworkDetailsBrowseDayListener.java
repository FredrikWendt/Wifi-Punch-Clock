package se.wendt.android.wifipclock;

import java.util.Date;

import se.wendt.android.util.Logger;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class NetworkDetailsBrowseDayListener implements OnClickListener {

	private static final Logger logger = Logger.getLogger(NetworkDetailsBrowseDayListener.class);
	private static final long ONE_DAY_IN_MILLIS = 1000L * 60 * 60 * 24;
	private final NetworkDetailsActivity activity;
	private final int offsetAdjustment;

	private NetworkDetailsBrowseDayListener(NetworkDetailsActivity networkDetailsActivity, int offsetAdjustment) {
		this.activity = networkDetailsActivity;
		this.offsetAdjustment = offsetAdjustment;
	}

	public static NetworkDetailsBrowseDayListener next(NetworkDetailsActivity activity) {
		return new NetworkDetailsBrowseDayListener(activity, +1);
	}
	
	public static NetworkDetailsBrowseDayListener previous(NetworkDetailsActivity activity) {
		return new NetworkDetailsBrowseDayListener(activity, -1);
	}

	public void onClick(View v) {
		logger.debug("onClick %s", offsetAdjustment);
		Date date = activity.getDateFromIntent();
		String networkName = activity.getNetworkNameFromIntent();
		Date dateToJumpTo = new Date(date.getTime() + offsetAdjustment * ONE_DAY_IN_MILLIS);
		Intent intent = NetworkDetailsActivity.createIntentUsedToStartActivity(activity, networkName, dateToJumpTo);
		activity.startActivity(intent);
	}

}
