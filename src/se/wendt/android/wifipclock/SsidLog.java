package se.wendt.android.wifipclock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import se.wendt.android.util.Logger;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class SsidLog extends SQLiteOpenHelper {

	private static final String WHERE_WLAN_AND_END = "wlan=? AND end=?";

	private static final Logger logger = Logger.getLogger(SsidLog.class);

	/** Name of this database. */
	public static final String DATABASE_NAME = "WlanPunchClock";

	/** Version 1 of the schema. */
	private static final int DATABASE_SCHEMA_VERSION = 1;

	/** All columns, used for querying. */
	private static final String[] columns = "_id,wlan,begin,end".split(",");

	/** The only table used. */
	private static final String TABLE_RECORDS = "records";

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private final Scheduler scheduler;

	public SsidLog(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_SCHEMA_VERSION);
		scheduler = new Scheduler();
		logger.debug("Setup finished");
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		Cursor tables = db.rawQuery("select name from sqlite_master where type = 'table'", null);
		logger.debug("Found %s tables in database:", tables.getCount());
		List<String> tablesFound = new ArrayList<String>();
		while (tables.moveToNext()) {
			logger.debug("Found table %s in database", tables.getString(0));
			tablesFound.add(tables.getString(0));
		}
		if (!tablesFound.contains(TABLE_RECORDS)) {
			logger.error("Missing table %s", TABLE_RECORDS);
			onCreate(db);
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		boolean wasAlreadyInTransaction = db.inTransaction();
		if (!wasAlreadyInTransaction) {
			db.beginTransaction();
		}
		db.execSQL("CREATE TABLE " + TABLE_RECORDS + " (" + "_id INTEGER PRIMARY KEY, " + "wlan TEXT NOT NULL, "
				+ "begin DATETIME NOT NULL, " + "end DATETIME NOT NULL" + ");");
		// FIXME: add unique constraint on wlan+start
		if (!wasAlreadyInTransaction) {
			db.setTransactionSuccessful();
			db.endTransaction();
		}
	}
	
	public void recordSeenWlans(Collection<String> wlanNames, long discoveryTime) {
		// FIXME: either update the last record (if the end date matches "last"
		// call to this method)
		// or INSERT with start = now, end = now
		// FIXME: and we also need to "stop" tracking in time - meaning we only
		// updated the latest record to "not seen any longer"
		Date discoveryTimeAsDate = new Date(discoveryTime);
		String discoveryTimeAsString = dateFormat.format(discoveryTimeAsDate);
		long previousRunMillis = scheduler.getPreviousRunInMilliSeconds(discoveryTime);
		Date previousRunDate = new Date(previousRunMillis);
		String previousRunDateAsString = dateFormat.format(previousRunDate);

		SQLiteDatabase database = getWritableDatabase();
		for (String wlanName : wlanNames) {
			try {
				database.beginTransaction();
				ContentValues values = new ContentValues();
				values.put("wlan", wlanName);
				values.put("end", discoveryTimeAsString);
				if (databaseContainsRecordToUpdate(database, wlanName, previousRunDateAsString)) {
					logger.debug("Updating %s (%s - %s)", wlanName, previousRunDateAsString, discoveryTimeAsDate);
					database.update(TABLE_RECORDS, values, WHERE_WLAN_AND_END, new String[] { wlanName, previousRunDateAsString });
				} else {
					logger.info("Inserting %s (%s - %s)", wlanName, previousRunDateAsString, discoveryTimeAsDate);
					values.put("begin", discoveryTimeAsString);
					database.insert(TABLE_RECORDS, null, values);
					notifyUserOfNewTimeSlice(wlanName, discoveryTimeAsString);
				}
				database.setTransactionSuccessful();
			} catch (SQLException e) {
				logger.error(e, "Failed to record information about wlan %s at %s", wlanName, discoveryTimeAsDate);
			} finally {
				database.endTransaction();
			}
		}
	}

	private void notifyUserOfNewTimeSlice(String wlanName, String dateAsString) {
		sendIntent("Starting to track precense of %s (%s)", wlanName, dateAsString);
	}

	private void sendIntent(String string, String wlanName, String dateAsString) {
		// FIXME: send intent and let an activity notify
	}

	private boolean databaseContainsRecordToUpdate(SQLiteDatabase database, String wlanName,
			String dateAtLastUpdateAsString) {
		Cursor result = database.query(TABLE_RECORDS, null, WHERE_WLAN_AND_END, new String[] { wlanName,
				dateAtLastUpdateAsString }, null, null, null);
		int numberOfRecordsFound = result.getCount();
		boolean rowExists = numberOfRecordsFound > 0;
		result.close();
		logger.debug("Looking for %s (%s), found %s", wlanName, dateAtLastUpdateAsString, numberOfRecordsFound);
		return rowExists;
	}

	public Cursor getRecords() {
		return getReadableDatabase().query(TABLE_RECORDS, columns, null, null, null, null,
				"begin DESC, wlan ASC, _id ASC");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		throw new UnsupportedOperationException("This application uses only one database format");
	}

}
