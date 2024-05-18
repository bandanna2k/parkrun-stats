package dnt.parkrun.database.stats;

import dnt.parkrun.database.BaseDao;
import dnt.parkrun.datastructures.Country;

import javax.sql.DataSource;

public class MostVolunteersDao extends BaseDao
{
    public MostVolunteersDao(Country country, DataSource statsDataSource)
    {
        super(country, statsDataSource);
    }

}
