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
		Log.i(tag, format(message, args));
	}

	public void debug(String message, Object... args) {
		Log.d(tag, format(message, args));
	}

	public void error(Throwable e, String messageFormat, Object... args) {
		Log.e(tag, format(messageFormat, args), e);
	}

	public void error(String message, Object... args) {
		Log.e(tag, format(message, args));
	}

	public static Logger getLogger(Class<?> klass) {
		return new Logger(klass.getSimpleName());
	}

	public void warn(String messageFormat, Object... args) {
		Log.w(tag, format(messageFormat, args));
	}

	private String format(String format, Object... args) {
		if (args == null) {
			args = new String[] { "null" };
		}
		for (int j = 0; j < args.length; j++) {
			if (args[j] == null) {
				args[j] = "null";
			}
		}

		// other exceptions can occur
		try {
			return String.format(format, args);
		} catch (Exception e) {
			return format + args;
		}
	}

}
