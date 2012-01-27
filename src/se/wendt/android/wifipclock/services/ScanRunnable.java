package se.wendt.android.wifipclock.services;

import se.wendt.android.util.Logger;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

// TODO: give the locks, instead of managers
public class ScanRunnable implements Runnable {

	private static final Logger log = Logger.getLogger(ScanRunnable.class);

	private final Context context;
	private final WifiManager wifiManager;
	private final PowerManager powerManager;
	private WifiScanCompletedReceiver resultReceiver;
	private WifiLock wifiLock;
	private WakeLock wakeLock;
	private long startOfRun = Long.MAX_VALUE;

	public ScanRunnable(Context context, WifiManager wifiManager, PowerManager powerManager, WifiScanCompletedReceiver resultReceiver) {
		this.context = context;
		this.wifiManager = wifiManager;
		this.powerManager = powerManager;
		this.resultReceiver = resultReceiver;
	}

	public void run() {
		log.debug("Wifi scan starting");
		try {
			startOfRun = System.currentTimeMillis();
			acquireLocks();
			registerResultReceiver();
			resultReceiver.startScan();
			log.debug("Wifi scan started");
			// TODO: use a "locking" Future
			while (wakeLock != null) {
				try {
					Thread.sleep(5000);
					log.debug("Waiting for scan to complete (%s ms)", getMillisElapsedSinceStartOfScan());
					// FIXME: give up after 90 seconds?
				} catch (InterruptedException e) {
					e.printStackTrace();
					log.debug("Waiting for scan to complete interrupted (%s ms)", getMillisElapsedSinceStartOfScan());
				}
			}
		} catch (Throwable t) {
			throw new RuntimeException("Failed to start wifi scan", t);
		} finally {
			cleanup();
		}
		log.info("Wifi scan completed");
	}
	
	public void cleanup() {
		log.info("Cleaning up");
		releaseLocks();
		unregisterResultReceiver();
		context.stopService(new Intent(context, WifiScanService.class));
	}

	private void registerResultReceiver() {
		log.debug("Registering result receiver");
		IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		context.registerReceiver(resultReceiver, intentFilter);
	}

	private void acquireLocks() {
		log.debug("Acquiring locks");
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, ScanRunnable.class.getName());
		wakeLock.acquire();
		wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, ScanRunnable.class.getName());
		wifiLock.acquire();
		wakeLock.setReferenceCounted(false);
	}

	private void unregisterResultReceiver() {
		log.debug("Unregistering result receiver (%s)", resultReceiver);
		if (resultReceiver != null) {
			context.unregisterReceiver(resultReceiver);
			resultReceiver = null;
		}
	}

	private void releaseLocks() {
		log.info("Releasing locks");
		if (wifiLock != null) {
			log.debug("Releasing wifiLock");
			wifiLock.release();
			wifiLock = null;
		}
		if (wakeLock != null) {
			log.debug("Releasing wakeLock");
			wakeLock.release();
			wakeLock = null;
		}
		log.debug("Releasing locks done");
	}

	public long getMillisElapsedSinceStartOfScan() {
		return System.currentTimeMillis() - startOfRun;
	}

}
