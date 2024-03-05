package dnt.parkrun.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class DateConverter
{
    private static final SimpleDateFormat WEBSITE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");
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
        try
        {
            return WEBSITE_FORMATTER.parse(date);
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
