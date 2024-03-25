package dnt.parkrun.database.stats;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.database.BaseDao;
import dnt.parkrun.datastructures.Athlete;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

public class MostVolunteersDao extends BaseDao
{
    private final Date date;

    public static MostVolunteersDao getOrCreate(DataSource dataSource, Date date)
    {
        MostVolunteersDao mostVolunteersDao = new MostVolunteersDao(dataSource, date);
        mostVolunteersDao.init();
        return mostVolunteersDao;
    }
    private MostVolunteersDao(DataSource dataSource, Date date)
    {
        super(dataSource);
        this.date = date;
        init();
    }
    private void init()
    {
        String sql =
                "create table if not exists " + getTableName() + " ( " +
                        "    athlete_id             INT               NOT NULL," +
                        "    course_count           INT               NOT NULL," +
                        "    total_volunteers       INT               NOT NULL," +
                        " PRIMARY KEY (athlete_id) " +
                        ") DEFAULT CHARSET=utf8mb4";
        jdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }

    public List<Object[]> getMostVolunteers()
    {
        String sql = "select a.name, a.athlete_id, sub1.count as different_region_course_count, sub2.count as total_region_volunteers\n" +
                "from " + athleteTable() + " a\n" +
                "join  \n" +
                "(\n" +
                "    select athlete_id, count(course_id) as count\n" +
                "    from (select distinct athlete_id, course_id from " + volunteerTable() +") as sub1a\n" +
                "    group by athlete_id\n" +
                "    having count >= 10\n" +
                "    order by count desc, athlete_id asc \n" +
                ") as sub1 on sub1.athlete_id = a.athlete_id\n" +
                "join\n" +
                "(\n" +
                "    select athlete_id, count(concat) as count\n" +
                "    from (select athlete_id, concat(athlete_id, '-', course_id, '-', date) as concat from " + volunteerTable() + ") as sub2a\n" +
                "    group by athlete_id\n" +
                "    order by count desc, athlete_id asc \n" +
                ") as sub2 on sub2.athlete_id = a.athlete_id\n" +
                "where a.name is not null\n" +
                "order by different_region_course_count desc, total_region_volunteers desc, a.athlete_id desc;\n";

        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) -> new Object[] {
                Athlete.from(rs.getString("name"), rs.getInt("athlete_id")),
                rs.getInt("different_region_course_count"),
                rs.getInt("total_region_volunteers")
        });
    }

    private String getTableName()
    {
        return "most_volunteers_for_region_" + DateConverter.formatDateForDbTable(date);
    }
}
