/***
	Copyright (c) 2009 CommonsWare, LLC
	
	Licensed under the Apache License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may obtain
	a copy of the License at
		http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */

package se.wendt.android.wifipclock;

import java.util.ArrayList;
import java.util.List;

import se.wendt.android.scheduling.WakefulIntentService;
import se.wendt.android.util.Logger;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.SystemClock;


public class ScanWifiService extends WakefulIntentService {

	private static final Logger logger = Logger.getLogger(ScanWifiService.class);
	private final Scheduler scheduler;

	public ScanWifiService() {
		super(ScanWifiService.class.getSimpleName());
		scheduler = new Scheduler();
	}

	@Override
	protected void doWakefulWork(Intent intent) {
		WifiManager manager = getWifiManager();
		 makeSureWifiIsEnabled(manager);
		WifiLock lock = manager.createWifiLock(ScanWifiService.class.getName());
		lock.acquire();
		long now = scheduler.getNow();
		try {
			logger.debug("got intent, starting to scan Wifi");
			manager.startScan();
			SystemClock.sleep(15000); // FIXME: rewrite using BroadcastReceiver, WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
			List<ScanResult> scanResults = manager.getScanResults();
			SsidLog storage = new SsidLog(getApplicationContext());
			List<String> ssids = new ArrayList<String>();
			for (ScanResult scanResult : scanResults) {
				ssids.add(scanResult.SSID);
			}
			storage.recordSeenWlans(ssids, now);
			storage.close();
		} finally {
			lock.release();
		}
	}

	protected void makeSureWifiIsEnabled(WifiManager manager) {
		if (!manager.isWifiEnabled()) {
			if (manager.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
				// manager.setWifiEnabled(true);
				// FIXME: fire off a notification saying that WLAN is disabled - won't track wlan presence 
				logger.warn("Wifi is disabled");
			}
		}
	}

	protected WifiManager getWifiManager() {
		return (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
	}
}