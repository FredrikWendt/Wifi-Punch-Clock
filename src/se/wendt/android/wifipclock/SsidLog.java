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
	public static final String DATABASE_NAME = "WlanPunchClockDb";

	/** Version 1 of the schema. */
	private static final int DATABASE_SCHEMA_VERSION = 1;

	/** All columns, used for querying. */
	private static final String[] columns = "_id,wlan,begin,end".split(",");

	/** The only table used. */
	private static final String TABLE_RECORDS = "records";

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private static final String TABLE_RUNS = "scheduling";

	public SsidLog(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_SCHEMA_VERSION);
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
		db.execSQL("CREATE TABLE scheduling ( _id INTEGER PRIMARY KEY )");
		// FIXME: add unique constraint on wlan+start
		if (!wasAlreadyInTransaction) {
			db.setTransactionSuccessful();
			db.endTransaction();
		}
	}

	public void recordSeenWlans(Collection<String> wlanNames, Date discoveryTimeAsDate) {
		SQLiteDatabase database = getWritableDatabase();
		String discoveryTimeAsString = dateFormat.format(discoveryTimeAsDate);
		Date previousRunDate = getPreviousRun(database);
		recordThisRun(database, discoveryTimeAsDate);
		String previousRunDateAsString = dateFormat.format(previousRunDate);
		boolean allowUpdatingLastRecord = true;

		if (discoveryTimeAsDate.getDate() != previousRunDate.getDate()) {
			// force creation of a new record
			allowUpdatingLastRecord = false;
			logger.debug("New date since last run, forcing new record(s)");
		}

		for (String wlanName : wlanNames) {
			try {
				database.beginTransaction();
				ContentValues values = new ContentValues();
				values.put("wlan", wlanName);
				values.put("end", discoveryTimeAsString);
				if (allowUpdatingLastRecord
						&& databaseContainsRecordToUpdate(database, wlanName, previousRunDateAsString)) {
					logger.debug("Updating %s (%s - %s)", wlanName, previousRunDateAsString, discoveryTimeAsDate);
					database.update(TABLE_RECORDS, values, WHERE_WLAN_AND_END, new String[] { wlanName,
							previousRunDateAsString });
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

	private void recordThisRun(SQLiteDatabase database, Date discoveryTimeAsDate) {
		ContentValues values = new ContentValues();
		values.put("_id", discoveryTimeAsDate.getTime());
		database.insert(TABLE_RUNS, null, values);
	}

	private Date getPreviousRun(SQLiteDatabase database) {
		logger.debug("Date of previous run is being fetched");
		Date result = new Date();
		Cursor cursor = database.query(TABLE_RUNS, new String[] { "_id" }, null, null, null, null, "_id DESC", "1");
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
			long pointInTime = cursor.getLong(0);
			result = new Date(pointInTime);
		}
		cursor.close();
		logger.debug("Date of previous run returned is %s", dateFormat.format(result));
		return result;
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

	public Cursor getNetworks() {
		// FIXME: use aggregate to calculate sum of billable hours?
//		Cursor cursor = getReadableDatabase().rawQuery(
//				"SELECT DISTINCT wlan, _id FROM " + TABLE_RECORDS + " ORDER BY wlan ASC", null);
		Cursor cursor = new StaticContentCursor("_id,wlan".split(","), new String[][] {
			"0,Network 0".split(","),
			"1,Network 1".split(","),
			"2,Network 2".split(","),
			"3,Network 3".split(","),
			"4,Network 4".split(","),
			"5,Network 5".split(","),
			"6,Network 6".split(","),
			"7,Network 7".split(","),
			"8,Network 8".split(","),
			"9,Network 9".split(","),
			"10,Network 10".split(","),
			"11,Network 11".split(","),
			"12,Network 12".split(","),
			"13,Network 13".split(","),
			"14,Network 14".split(","),
			"15,Network 15".split(","),
			"16,Network 16".split(","),
			"17,Network 17".split(","),
			"18,Network 18".split(","),
			"19,Network 19".split(","),
			"20,Network 20".split(","),
			"21,Network 21".split(","),
			"22,Network 22".split(","),
			"23,Network 23".split(","),
			"24,Network 24".split(",")
		});
		logger.debug("found %s ssids", cursor.getCount());
		return cursor;
	}

	public Cursor getRecordsForWifiNetwork(String networkName) {
//		Cursor cursor = getReadableDatabase().rawQuery("SELECT 'day', 'date', 8h', '15m', 'x'", null);
		Cursor cursor = new StaticContentCursor("_id,day,date,hours,minutes,note".split(","), new String[][] {
			"1,mon,8 dec,8h,15m,0".split(","),
			"2,tue,9 dec,6h,0m,1".split(","),
			"3,wed,10 dec,7h,45m,1".split(",")
		});
		logger.debug("found %s records for ssid %s", cursor.getCount(), networkName);
		return cursor;
	}

	public Cursor getRecordsForWifiNetworkAndDate(String networkName, Date date) {
		Cursor cursor = new StaticContentCursor("_id,start,end,duration".split(","), new String[][] {
			"1,08:15,08:15,15m".split(","),
			"2,09:00,09:30,45m".split(","),
			"3,10:00,10:30,45m".split(","),
			"4,11:00,12:30,1h 45m".split(","),
			"5,13:15,17:30,4h 30m".split(","),
			"6,18:00,21:15,3h 30m".split(",")
		});
		logger.debug("found %s records for ssid %s on date %s", cursor.getCount(), networkName, date);
		return cursor;
	}

	public String getNoteForDate(Date dateFromIntent) {
		return "This is a fake note.\nBut it could've been very real actually, if I'd been really bored and didn't have anything better to do ...";
	}

}
