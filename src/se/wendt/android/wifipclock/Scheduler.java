package se.wendt.android.wifipclock;

import static android.app.AlarmManager.RTC_WAKEUP;

import java.util.Date;

import se.wendt.android.scheduling.OnAlarmReceiver;
import se.wendt.android.util.Logger;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class Scheduler {
	
	private static final Logger logger = Logger.getLogger(Scheduler.class);
	private static final int _15_MINUTES = 15 * 60 * 1000 / 15; // FIXME before release :)

	public void scheduleNextScan(Context context) {
		AlarmManager alarmManager = getAlarmManager(context);
		PendingIntent pendingIntent = createPendingIntent(context);
		long nextRun = getMillisecondsOfNextTimeToScan();
		alarmManager.set(RTC_WAKEUP, nextRun, pendingIntent);
	}

	public long getMillisecondsOfNextTimeToScan() {
		// FIXME: make this more sophisticated, allowing "silent" hours during
		// the night etc
		long now = getNow();
		long nextRun = now + _15_MINUTES;
		nextRun = nextRun - (nextRun % _15_MINUTES);
		logger.debug("Next run occurs in %s ms (%s)", nextRun - now, new Date(nextRun));
		return nextRun;
	}

	public long getNow() {
		long now = System.currentTimeMillis();
		return now;
	}

	protected PendingIntent createPendingIntent(Context context) {
		Intent intent = new Intent(context, OnAlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pi;
	}

	protected AlarmManager getAlarmManager(Context context) {
		AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		return mgr;
	}

	public long getPreviousRunInMilliSeconds(long runToRewindFrom) {
		long result = runToRewindFrom - _15_MINUTES;
		result = result - (result % _15_MINUTES);
		return result ;
	}

}
