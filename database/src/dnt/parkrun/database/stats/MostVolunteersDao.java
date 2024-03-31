package dnt.parkrun.database.stats;

import dnt.parkrun.database.BaseDao;

import javax.sql.DataSource;

public class MostVolunteersDao extends BaseDao
{
    public MostVolunteersDao(DataSource statsDataSource)
    {
        super(statsDataSource);
    }

}
