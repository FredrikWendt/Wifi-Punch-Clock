/*
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

package se.wendt.android.wifipclock.services;

import se.wendt.android.util.Logger;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;

public class WifiScanService extends Service {

	private final Object scanThreadLock = new Object();
	private static final Logger logger = Logger.getLogger(WifiScanService.class);
	private static ScanRunnable scanRunnable;
	private static Thread scanThread;

	@Override
	public IBinder onBind(Intent intent) {
		logger.debug("onBind (%s)", intent);
		return null;
	}

	@Override
	public void onCreate() {
		logger.debug("onCreate");
		super.onCreate();
	}

	/**
	 * Returns {@link Service#START_NOT_STICKY} since in worst case the service
	 * will run again in 15 minutes.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String intentString = "null";
		if (intent != null) {
			intentString = intent.toString();
		}
		logger.debug("onStartCommand: %s, %s, %s", intentString, flags, startId);
		Context context = getApplicationContext();

		synchronized (scanThreadLock) {
			if (scanThread == null) {
				PowerManager powerManager = getPowerManager(context);
				WifiManager wifiManager = getWifiManager(context);
				WifiScanCompletedReceiver resultReceiver = new WifiScanCompletedReceiver(this, wifiManager);
				scanRunnable = new ScanRunnable(context, wifiManager, powerManager, resultReceiver);
				scanThread = new Thread(scanRunnable);
				scanThread.start();
			} else {
				String logFormat = "Scan already running - %s (%s) returning without doing anything";
				logger.info(logFormat, scanThread, scanRunnable.getMillisElapsedSinceStartOfScan());
			}
		}
		return Service.START_NOT_STICKY;
	}

	public void scanCompleted() {
		synchronized (scanThreadLock) {
			if (scanThread != null) {
				scanRunnable.cleanup();
				scanThread.interrupt();
				scanThread = null;
				scanRunnable = null;
			}
		}
	}

	protected WifiManager getWifiManager(Context context) {
		return (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}

	protected PowerManager getPowerManager(Context context) {
		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		return powerManager;
	}
}