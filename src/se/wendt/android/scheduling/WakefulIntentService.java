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

import static android.os.PowerManager.PARTIAL_WAKE_LOCK;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

/**
 * Makes sure the device stays powered on until processing of the Intent is completed.
 */
abstract public class WakefulIntentService extends IntentService {
	
	public static final String LOCK_NAME_STATIC = WakefulIntentService.class.getName();
	
	private static WakeLock lockStatic = null;
	
	protected abstract void doWakefulWork(Intent intent);
	
	public WakefulIntentService(String name) {
		super(name);
	}

	@Override
	final protected void onHandleIntent(Intent intent) {
		try {
			doWakefulWork(intent);
		} finally {
			getLock(this).release();
		}
	}

	public static void acquireStaticLock(Context context) {
		getLock(context).acquire();
	}

	private static synchronized WakeLock getLock(Context context) {
		if (lockStatic == null) {
			PowerManager powerManager = getPowerManager(context);
			lockStatic = powerManager.newWakeLock(PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
			lockStatic.setReferenceCounted(true);
		}

		return lockStatic;
	}

	protected static PowerManager getPowerManager(Context context) {
		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		return powerManager;
	}

}
