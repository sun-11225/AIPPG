package com.viatom.bloodoxygendemo.util;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateConvert {
    public static String getDateTime(Date date, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.US);
        String dateStr = simpleDateFormat.format(date);
        return dateStr;
    }

    public static String getDateTime(int year, int month, int date, int hour, int minute, int second, String pattern) {
        Calendar localTime = Calendar.getInstance();
        localTime.set(Calendar.YEAR, year);
        localTime.set(Calendar.MONTH, month - 1);
        localTime.set(Calendar.DATE, date);
        localTime.set(Calendar.HOUR_OF_DAY, hour);
        localTime.set(Calendar.MINUTE, minute);
        localTime.set(Calendar.SECOND, second);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.US);
        String dateStr = simpleDateFormat.format(localTime.getTime());
        return dateStr;
    }

    public static Date getCurrentDate(int year, int month, int date, int hour, int minute, int second) {
        Calendar localTime = Calendar.getInstance();
        localTime.set(Calendar.YEAR, year);
        localTime.set(Calendar.MONTH, month - 1);
        localTime.set(Calendar.DATE, date);
        localTime.set(Calendar.HOUR_OF_DAY, hour);
        localTime.set(Calendar.MINUTE, minute);
        localTime.set(Calendar.SECOND, second);

        return localTime.getTime();
    }

    public static String getDateTime(int hour, int minute, int second, String pattern) {
        Calendar localTime = Calendar.getInstance();
        localTime.set(Calendar.HOUR_OF_DAY, hour);
        localTime.set(Calendar.MINUTE, minute);
        localTime.set(Calendar.SECOND, second);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.US);
        String dateStr = simpleDateFormat.format(localTime.getTime());
        return dateStr;
    }

    public static Date offset(Date date, int field, int offset) {
        Calendar localTime = Calendar.getInstance();
        localTime.setTime(date);
        localTime.add(field, offset);
        return localTime.getTime();
    }

    public static String getFormatTime(int recordingTime, char sep) {
//        int recordHour = recordingTime / 3600;
//        int recordMinute = (recordingTime % 3600) / 60;
        int recordMinute = recordingTime / 60;
        int recordSecond = recordingTime % 60;

        String recordMinuteStr = String.valueOf(recordMinute);
        if(recordMinute < 10) {
            recordMinuteStr = "0".concat(recordMinuteStr);
        }
        String recordSecondStr = String.valueOf(recordSecond);
        if(recordSecond < 10) {
            recordSecondStr = "0".concat(recordSecondStr);
        }

        return recordMinuteStr.concat(String.valueOf(sep)).concat(recordSecondStr);
    }

    public static String getRecordTime(int recordingTime) {
        int recordHour = recordingTime / 3600;
        int recordMinute = (recordingTime % 3600) / 60;
        int recordSecond = (recordingTime % 3600) % 60;

        String recordHourStr = String.valueOf(recordHour);
        String recordMinuteStr = String.valueOf(recordMinute);
        String recordSecondStr = String.valueOf(recordSecond);

        String recordTime = "";
        if(recordHour > 0) {
            recordTime = recordTime.concat(recordHourStr.concat("h"));
        }
        if(recordMinute > 0 || !TextUtils.isEmpty(recordTime)) {
            recordTime = recordTime.concat(recordMinuteStr.concat("m"));
        }
        recordTime = recordTime.concat(recordSecondStr.concat("s"));
        return recordTime;
    }

    public static String getRecordTime(int recordingTime, String sep) {
        return getRecordTime(recordingTime, sep, false);
    }

    public static String getRecordTime(int recordingTime, String sep, boolean ignoreHour) {
        if(recordingTime < 0) {
            return "00".concat(sep).concat("00").concat(sep).concat("00");
        }
        int recordHour = recordingTime / 3600;
        int recordMinute = (recordingTime % 3600) / 60;
        int recordSecond = (recordingTime % 3600) % 60;

        String recordHourStr = String.valueOf(recordHour);
        if(recordHour < 10) {
            recordHourStr = "0" + recordHourStr;
        }
        String recordMinuteStr = String.valueOf(recordMinute);
        if(recordMinute < 10) {
            recordMinuteStr = "0" + recordMinuteStr;
        }
        String recordSecondStr = String.valueOf(recordSecond);
        if(recordSecond < 10) {
            recordSecondStr = "0" + recordSecondStr;
        }

        String recordTime = "";
        if(recordHour > 0 || !ignoreHour) {
            recordTime = recordTime.concat(recordHourStr.concat(sep));
        }
        recordTime = recordTime.concat(recordMinuteStr.concat(sep));
        recordTime = recordTime.concat(recordSecondStr);
        return recordTime;
    }
}
