package se.wendt.android.util;

import android.database.SQLException;
import android.util.Log;

public class Logger {

	private String tag;

	private Logger(String tag) {
		this.tag = tag;
	}

	void warn(String string, SQLException e) {
		Log.w(tag, string, e);
	}

	void error(String message, SQLException e) {
		Log.e(tag, message, e);
	}

	void debug(String message, Throwable e) {
		Log.d(tag, message, e);
	}

	void debug(String message) {
		Log.d(tag, message);
	}

	public void info(String string, SQLException e) {
		Log.i(tag, string, e);
	}

	public void info(String message, Object... args) {
		Log.i(tag, String.format(message, args));
	}

	public void debug(String message, Object... args) {
		Log.d(tag, String.format(message, args));
	}

	public void error(Throwable e, String messageFormat, Object... args) {
		Log.e(tag, String.format(messageFormat, args), e);
	}

	public void error(String message, Object... args) {
		Log.e(tag, String.format(message, args));
	}

	public static Logger getLogger(Class<?> klass) {
		return new Logger(klass.getSimpleName());
	}

	public void warn(String messageFormat, Object... args) {
		Log.w(tag, String.format(messageFormat, args));
	}

}
