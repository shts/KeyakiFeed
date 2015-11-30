package jp.shts.android.keyakifeed.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    private static final String TAG = DateUtils.class.getSimpleName();

    private DateUtils() {}

    public static String dateToString(Date date) {
        Calendar datetime = Calendar.getInstance();
        datetime.setTime(date);
        return datetime.get(Calendar.YEAR) + "/" + (datetime.get(Calendar.MONTH) + 1)
                + "/" + datetime.get(Calendar.DATE) + "  "
                + datetime.get(Calendar.HOUR_OF_DAY) + ":"
                + addZeroIfNeed(datetime.get(Calendar.MINUTE));
    }

    private static String addZeroIfNeed(int minute) {
        if (minute < 10) {
            return "0" + String.valueOf(minute);
        }
        return String.valueOf(minute);
    }

}
