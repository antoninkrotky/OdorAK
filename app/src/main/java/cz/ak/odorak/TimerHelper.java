package cz.ak.odorak;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class TimerHelper {
	HashMap<String, Long> mStartTime;
	HashMap<String, Long> mEndTime;
	HashMap<String, Long> mIntervalMs;
	SimpleDateFormat mDateFormat;

	public TimerHelper() {
		mStartTime = new HashMap<String, Long>();
		mEndTime = new HashMap<String, Long>();
		mIntervalMs = new HashMap<String, Long>();
		mDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", Locale.US);
	}

	public void start(String key) {
		long cur = System.currentTimeMillis();
		mStartTime.put(key, Long.valueOf(cur));
		mEndTime.put(key, Long.valueOf(0));
		mIntervalMs.put(key, Long.valueOf(0));
	}

	public long end(String key) {
		long cur = System.currentTimeMillis();
		mEndTime.put(key, Long.valueOf(cur));
		Long start = mStartTime.get(key);
		if (start == null) {
			mIntervalMs.put(key, Long.valueOf(0));
			return 0;
		}
		long duration = cur - start.longValue();
		mIntervalMs.put(key, Long.valueOf(duration));
		return duration;
	}

	public String getStart(String key) {
		Long start = mStartTime.get(key);
		if (start == null || start.longValue() <= 0) {
			return "Unknown start time";
		} else {
			return mDateFormat.format(new Date(start.longValue()));
		}
	}

	public String getEnd(String key) {
		Long end = mEndTime.get(key);
		if (end == null || end.longValue() <= 0) {
			return "Unknown end time";
		} else {
			return mDateFormat.format(new Date(end.longValue()));
		}
	}

	public long getDuration(String key) {
		Long interval = mIntervalMs.get(key);
		if (interval == null) {
			return 0;
		} else {
			return interval.longValue();
		}
	}

	public String getCurTime() {
		return mDateFormat.format(new Date());
	}

}
