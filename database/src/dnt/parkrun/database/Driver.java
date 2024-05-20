package dnt.parkrun.database;

import java.sql.SQLException;

public abstract class Driver
{
    public static com.mysql.jdbc.Driver getDriver()
    {
        try
        {
            return new com.mysql.jdbc.Driver();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
}
