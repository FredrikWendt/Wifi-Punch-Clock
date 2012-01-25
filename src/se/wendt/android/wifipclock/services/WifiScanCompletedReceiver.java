package se.wendt.android.wifipclock.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import se.wendt.android.util.Logger;
import se.wendt.android.wifipclock.SsidLog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

public class WifiScanCompletedReceiver extends BroadcastReceiver {

	private static final Logger log = Logger.getLogger(WifiScanCompletedReceiver.class);
	private final WifiManager manager;
	private final WifiScanService scanService;
	private Date scanStartedAt;

	public WifiScanCompletedReceiver(WifiScanService wifiScanService, WifiManager manager) {
		this.scanService = wifiScanService;
		this.manager = manager;
	}
	
	public void startScan() {
		log.debug("Initiating scan");
		scanStartedAt = new Date(System.currentTimeMillis());
		makeSureWifiIsTurnedOn();
		manager.startScan();
	}

	private void makeSureWifiIsTurnedOn() {
		if (!manager.isWifiEnabled()) {
			boolean result = manager.setWifiEnabled(true);
			if (!result) {
				log.warn("Failed to turn WiFi on, not sure what to do");
			}
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			registerResults(context);
		} finally {
			scanService.scanCompleted();
		}
	}

	private void registerResults(Context context) {
		log.debug("Registering results of scan");

		List<ScanResult> scanResults = manager.getScanResults();
		SsidLog storage = new SsidLog(context);
		List<String> ssids = new ArrayList<String>();
		for (ScanResult scanResult : scanResults) {
			ssids.add(scanResult.SSID);
		}
		storage.recordSeenWlans(ssids, scanStartedAt);
		storage.close();
	}
}
