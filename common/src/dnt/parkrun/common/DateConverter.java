package dnt.parkrun.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class DateConverter
{
    private static final SimpleDateFormat WEBSITE_DATE_PARSER = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat DATABASE_NAME_DATE_FORMATTER = new SimpleDateFormat("yyyy_MM_dd");

    /**
     * 25/12/2022
     */
    public static Date parseWebsiteDate(String date)
    {
        try
        {
            return WEBSITE_DATE_PARSER.parse(date);
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String formatDateForDbTable(Date date)
    {
        return DATABASE_NAME_DATE_FORMATTER.format(date);
    }
}
