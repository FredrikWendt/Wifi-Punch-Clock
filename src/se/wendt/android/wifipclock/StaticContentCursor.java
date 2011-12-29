package se.wendt.android.wifipclock;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

public class StaticContentCursor implements Cursor {

	private final String[] columnNames;
	private final String[][] data;
	private int currentRowIndex;

	public StaticContentCursor(String[] columnNames, String[][] data) {
		this.columnNames = columnNames;
		this.data = data;
		this.currentRowIndex = -1;
	}

	public void close() {
	}

	public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
		Object columnData = getObject(columnIndex);
		if (columnData == null) {
			buffer.data = "null".toCharArray();
		} else {
			buffer.data = columnData.toString().toCharArray();
		}
	}

	public void deactivate() {
	}

	public byte[] getBlob(int columnIndex) {
		return null;
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getColumnIndex(String columnName) {
		int counter = 0;
		for (String name : columnNames) {
			if (name.equalsIgnoreCase(columnName)) {
				return counter;
			}
			counter++;
		}
		return -1;
	}

	public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
		int index = getColumnIndex(columnName);
		if (index == -1) {
			throw new IllegalArgumentException("Column " + columnName + " is not recognized");
		}
		return index;
	}

	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public int getCount() {
		return data.length;
	}

	public double getDouble(int columnIndex) {
		return Double.parseDouble(getObject(columnIndex));
	}

	public Bundle getExtras() {
		// TODO Auto-generated method stub
		return null;
	}

	public float getFloat(int columnIndex) {
		return Float.parseFloat(getObject(columnIndex));
	}

	public int getInt(int columnIndex) {
		return Integer.parseInt(getObject(columnIndex));
	}

	public long getLong(int columnIndex) {
		return Long.parseLong(getObject(columnIndex));
	}

	private String getObject(int columnIndex) {
		return data[currentRowIndex][columnIndex];
	}

	public int getPosition() {
		return currentRowIndex;
	}

	public short getShort(int columnIndex) {
		return Short.parseShort(getObject(columnIndex));
	}

	public String getString(int columnIndex) {
		String result = "null";
		Object datObject = getObject(columnIndex);
		if (datObject != null) {
			result = datObject.toString();
		}
		return result;
	}

	public boolean getWantsAllOnMoveCalls() {
		return false;
	}

	public boolean isAfterLast() {
		return currentRowIndex >= data.length;
	}

	public boolean isBeforeFirst() {
		return currentRowIndex < 0;
	}

	public boolean isClosed() {
		return false;
	}

	public boolean isFirst() {
		return currentRowIndex == 0;
	}

	public boolean isLast() {
		return currentRowIndex == data.length - 1;
	}

	public boolean isNull(int columnIndex) {
		return data[currentRowIndex][columnIndex] == null;
	}

	public boolean move(int offset) {
		currentRowIndex += offset;
		if (currentRowIndex < 0) {
			currentRowIndex = -1;
			return false;
		}
		if (currentRowIndex >= data.length) {
			currentRowIndex = data.length;
			return false;
		}
		return true;
	}

	public boolean moveToFirst() {
		currentRowIndex = 0;
		return true;
	}

	public boolean moveToLast() {
		currentRowIndex = data.length - 1;
		return true;
	}

	public boolean moveToNext() {
		if (currentRowIndex >= data.length) {
			return false;
		}
		currentRowIndex++;
		return true;
	}

	public boolean moveToPosition(int position) {
		if (position > -1 && position < data.length) {
			currentRowIndex = position;
			return true;
		} else {
			return false;
		}
	}

	public boolean moveToPrevious() {
		if (currentRowIndex < 0) {
			return false;
		}
		currentRowIndex--;
		return true;
	}

	public void registerContentObserver(ContentObserver observer) {
		// data will never change, so this is pointless
	}

	public void registerDataSetObserver(DataSetObserver observer) {
		// data will never change, so this is pointless
	}

	public boolean requery() {
		return true;
	}

	public Bundle respond(Bundle extras) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setNotificationUri(ContentResolver cr, Uri uri) {
		// FIXME
	}

	public void unregisterContentObserver(ContentObserver observer) {
		// data will never change, so this is pointless
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		// data will never change, so this is pointless
	}

}
