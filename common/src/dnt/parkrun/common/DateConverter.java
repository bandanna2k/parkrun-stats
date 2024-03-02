package dnt.parkrun.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class DateConverter
{
    private static final SimpleDateFormat WEBSITE_DATE_PARSER = new SimpleDateFormat("dd/MM/yyyy");


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
}
