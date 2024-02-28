package dnt.parkrun.mostevents;

import java.io.IOException;
import java.sql.SQLException;

public class Main
{
    public static void main(String[] args) throws IOException, SQLException
    {
        MostEvents mostEvents = MostEvents.newInstance();
        mostEvents.collectMostEventRecords();
    }
}