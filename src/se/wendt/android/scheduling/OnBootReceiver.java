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
/*
 * Minor modifications by Fredrik Wendt 2011.
 * Based on https://github.com/commonsguy/cw-advandroid/ 
 */

package se.wendt.android.scheduling;


import se.wendt.android.util.Logger;
import se.wendt.android.wifipclock.Scheduler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnBootReceiver extends BroadcastReceiver {

	private static final Logger logger = Logger.getLogger(OnBootReceiver.class);

	private final Scheduler scheduler;

	public OnBootReceiver() {
		scheduler = new Scheduler();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		logger.debug("device booted, setting up alarm");

		scheduler.scheduleNextScan(context);
	}
}
