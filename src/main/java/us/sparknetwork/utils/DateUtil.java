

package us.sparknetwork.utils;

import us.sparknetwork.base.I18n;

import java.time.Duration;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class DateUtil {

    public static String getHumanReadableDate(long time, I18n i18n) {
        long remainingTime = time / 1000;

        StringJoiner joiner = new StringJoiner(" ");

        long years;
        long months;
        long weeks;
        long days;
        long hours;
        long minutes;
        long seconds;

        if (remainingTime >= TimeUnit.DAYS.toSeconds(1) * 365) {
            years = remainingTime / (TimeUnit.DAYS.toSeconds(1) * 365);
            remainingTime %= TimeUnit.DAYS.toSeconds(1) * 365;
            joiner.add(years + "");
            joiner.add(years > 1 ? i18n.translate("time.years") : i18n.translate("time.year"));
        }

        if (remainingTime >= TimeUnit.DAYS.toSeconds(1) * 30) {
            months = remainingTime / (TimeUnit.DAYS.toSeconds(1) * 30);
            remainingTime %= TimeUnit.DAYS.toSeconds(1) * 30;
            joiner.add(months + "");
            joiner.add(months > 1 ? i18n.translate("time.months") : i18n.translate("time.month"));
        }

        if (remainingTime >= TimeUnit.DAYS.toSeconds(1) * 7) {
            weeks = remainingTime / (TimeUnit.DAYS.toSeconds(1) * 7);
            remainingTime %= TimeUnit.DAYS.toSeconds(1) * 7;
            joiner.add(weeks + "");
            joiner.add(weeks > 1 ? i18n.translate("time.weeks") : i18n.translate("time.week"));
        }

        if (remainingTime >= TimeUnit.DAYS.toSeconds(1)) {
            days = remainingTime / TimeUnit.DAYS.toSeconds(1);
            remainingTime %= TimeUnit.DAYS.toSeconds(1);
            joiner.add(days + "");
            joiner.add(days > 1 ? i18n.translate("time.days") : i18n.translate("time.day"));
        }

        if (remainingTime >= TimeUnit.HOURS.toSeconds(1)) {
            hours = remainingTime / TimeUnit.HOURS.toSeconds(1);
            remainingTime %= TimeUnit.HOURS.toSeconds(1);
            joiner.add(hours + "");
            joiner.add(hours > 1 ? i18n.translate("time.hours") : i18n.translate("time.hour"));
        }

        if (remainingTime >= TimeUnit.MINUTES.toSeconds(1)) {
            minutes = remainingTime / TimeUnit.MINUTES.toSeconds(1);
            remainingTime %= TimeUnit.MINUTES.toSeconds(1);
            joiner.add(minutes + "");
            joiner.add(minutes > 1 ? i18n.translate("time.minutes") : i18n.translate("time.minute"));
        }

        if (remainingTime >= 0) {
            seconds = remainingTime;
            joiner.add(seconds + "");
            joiner.add(seconds > 1 ? i18n.translate("time.seconds") : i18n.translate("time.second"));
        }
        return joiner.toString();
    }


    public static String durationToPrettyDate(Duration duration, I18n i18n) {
        return getHumanReadableDate(duration.toMillis(), i18n);
    }

    public static String millisToStringDuration(final long time) {
        long remainingTime = time / 1000;

        StringBuilder stringBuilder = new StringBuilder();

        long years;
        long months;
        long weeks;
        long days;
        long hours;
        long minutes;
        long seconds;

        if (remainingTime >= TimeUnit.DAYS.toSeconds(1) * 365) {
            years = remainingTime / (TimeUnit.DAYS.toSeconds(1) * 365);
            remainingTime %= TimeUnit.DAYS.toSeconds(1) * 365;
            stringBuilder.append(years + "y");
        }

        if (remainingTime >= TimeUnit.DAYS.toSeconds(1) * 30) {
            months = remainingTime / (TimeUnit.DAYS.toSeconds(1) * 30);
            remainingTime %= TimeUnit.DAYS.toSeconds(1) * 30;
            stringBuilder.append(months + "M");
        }

        if (remainingTime >= TimeUnit.DAYS.toSeconds(1) * 7) {
            weeks = remainingTime / (TimeUnit.DAYS.toSeconds(1) * 7);
            remainingTime %= TimeUnit.DAYS.toSeconds(1) * 7;
            stringBuilder.append(weeks + "w");
        }

        if (remainingTime >= TimeUnit.DAYS.toSeconds(1)) {
            days = remainingTime / TimeUnit.DAYS.toSeconds(1);
            remainingTime %= TimeUnit.DAYS.toSeconds(1);
            stringBuilder.append(days + "d");
        }

        if (remainingTime >= TimeUnit.HOURS.toSeconds(1)) {
            hours = remainingTime / TimeUnit.HOURS.toSeconds(1);
            remainingTime %= TimeUnit.HOURS.toSeconds(1);
            stringBuilder.append(hours + "h");
        }

        if (remainingTime >= TimeUnit.MINUTES.toSeconds(1)) {
            minutes = remainingTime / TimeUnit.MINUTES.toSeconds(1);
            remainingTime %= TimeUnit.MINUTES.toSeconds(1);
            stringBuilder.append(minutes + "m");
        }

        if (remainingTime >= 0) {
            seconds = remainingTime;
            stringBuilder.append(seconds + "s");
        }
        return stringBuilder.toString();
    }

    public static long parseStringDuration(String input) {
        if (input == null || input.isEmpty()) {
            return -1L;
        }
        long result = 0L;
        StringBuilder number = new StringBuilder();

        for (int i = 0; i < input.length(); ++i) {
            final char c = input.charAt(i);

            if (Character.isDigit(c)) {
                number.append(c);
            } else {
                final String str = number.toString();

                if (Character.isLetter(c) && !str.isEmpty()) {
                    result += convert(Integer.parseInt(str), c);
                    number = new StringBuilder();
                }
            }
        }
        return result;
    }

    private static long convert(final int value, final char unit) {
        switch (unit) {
            case 'y': {
                return value * TimeUnit.DAYS.toMillis(365L);
            }
            case 'M': {
                return value * TimeUnit.DAYS.toMillis(30L);
            }
            case 'd': {
                return value * TimeUnit.DAYS.toMillis(1L);
            }
            case 'h': {
                return value * TimeUnit.HOURS.toMillis(1L);
            }
            case 'm': {
                return value * TimeUnit.MINUTES.toMillis(1L);
            }
            case 's': {
                return value * TimeUnit.SECONDS.toMillis(1L);
            }
            default: {
                return -1L;
            }
        }
    }
}
