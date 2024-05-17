package dnt.parkrun.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class DateConverter
{
    public static final int ONE_DAY_IN_MILLIS = (24 * 60 * 60 * 1000);
    public static final int SEVEN_DAYS_IN_MILLIS = (7 * ONE_DAY_IN_MILLIS);

    private static final SimpleDateFormat WEBSITE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat WEBSITE_FORMATTER_POST_MAY24 = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATABASE_NAME_DATE_FORMATTER = new SimpleDateFormat("yyyy_MM_dd");

    /**
     * 25/12/2022
     */
    public static Date parseWebsiteDate(String date)
    {
        if(date == null)
        {
            return null;
        }
        if(date.contains("/"))
        {
            return parseWebsiteDate(date, WEBSITE_FORMATTER);
        }
        if(date.contains("-"))
        {
            return parseWebsiteDate(date, WEBSITE_FORMATTER_POST_MAY24);
        }
        throw new UnsupportedOperationException("No date parser found.");
    }
    private static Date parseWebsiteDate(String date, SimpleDateFormat websiteFormatter)
    {
        try
        {
            return websiteFormatter.parse(date);
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String formatDateForDbTable(Date date)
    {
        if(date == null)
        {
            return null;
        }
        return DATABASE_NAME_DATE_FORMATTER.format(date);
    }

    public static String formatDateForHtml(Date date)
    {
        if(date == null)
        {
            return null;
        }
        return WEBSITE_FORMATTER.format(date);
    }
}
