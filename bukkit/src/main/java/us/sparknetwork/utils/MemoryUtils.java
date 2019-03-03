package us.sparknetwork.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Taken from stackoverflow(Code.IT)
 * https://stackoverflow.com/a/49488476
 */
public class MemoryUtils {
    private static final long MEGABYTE_FACTOR = 1024L * 1024L;
    private static final DecimalFormat ROUNDED_DOUBLE_DECIMALFORMAT;

    static {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        ROUNDED_DOUBLE_DECIMALFORMAT = new DecimalFormat("####0.00", otherSymbols);
        ROUNDED_DOUBLE_DECIMALFORMAT.setGroupingUsed(false);
    }


    public static double getTotalMemoryInMiB() {
        return bytesToMiB(getTotalMemory());
    }

    public static double getFreeMemoryInMiB() {
        return bytesToMiB(getFreeMemory());
    }

    public static double getUsedMemoryInMiB() {
        return bytesToMiB(getUsedMemory());
    }

    public static double getMaxMemoryInMiB() {
        return bytesToMiB(getMaxMemory());
    }

    public static double getPercentageUsed() {
        return ((double) getUsedMemory() / getMaxMemory()) * 100;
    }

    public static String getPercentageUsedFormatted() {
        double usedPercentage = getPercentageUsed();
        return ROUNDED_DOUBLE_DECIMALFORMAT.format(usedPercentage) + "%";
    }

    public static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    public static long getUsedMemory() {
        return getMaxMemory() - getFreeMemory();
    }

    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    private static double bytesToMiB(double bytes){
        return bytes / MEGABYTE_FACTOR;
    }
}
