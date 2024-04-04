package dnt.parkrun.database.weekly;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.database.BaseDao;
import dnt.parkrun.datastructures.Athlete;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

import static dnt.parkrun.database.VolunteerDao.getMostVolunteersSubQuery1;
import static dnt.parkrun.database.VolunteerDao.getMostVolunteersSubQuery2;

public class VolunteerCountDao extends BaseDao
{
    private final Date date;

    public static VolunteerCountDao getInstance(DataSource dataSource, Date date)
    {
        VolunteerCountDao mostVolunteersDao = new VolunteerCountDao(dataSource, date);
        mostVolunteersDao.init();
        return mostVolunteersDao;
    }
    private VolunteerCountDao(DataSource dataSource, Date date)
    {
        super(dataSource);
        this.date = date;
        init();
    }
    private void init()
    {
        String sql =
                "create table if not exists " + getTableName() + " ( " +
                        "    athlete_id                     INT               NOT NULL," +
                        "    total_global_volunteer_count   INT               NOT NULL," +
                        " PRIMARY KEY (athlete_id) " +
                        ") DEFAULT CHARSET=utf8mb4";
        jdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }

    public void insertVolunteerCount(int athleteId, int volunteerCount)
    {
        String sql = "insert into " + getTableName() + " (" +
                "athlete_id, total_global_volunteer_count" +
                ") values ( " +
                ":athleteId, :volunteerCount" +
                ")";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("athleteId", athleteId)
                .addValue("volunteerCount", volunteerCount)
        );
    }

    private String getTableName()
    {
        return "volunteer_count_" + DateConverter.formatDateForDbTable(date);
    }

    public List<Object[]> getMostVolunteers()
    {
        String sql = "select a.name, a.athlete_id, sub1.count as different_region_course_count, sub2.count as total_region_volunteers, vc.total_global_volunteer_count " +
                "from " + athleteTable() + " a\n" +
                "join  \n" +
                "(\n" +
                getMostVolunteersSubQuery1(volunteerTable()) +
                ") as sub1 on sub1.athlete_id = a.athlete_id\n" +
                "join\n" +
                "(\n" +
                getMostVolunteersSubQuery2(volunteerTable()) +
                ") as sub2 on sub2.athlete_id = a.athlete_id\n" +
                "left join " + getTableName() + " vc on vc.athlete_id = a.athlete_id " +
                "where a.name is not null\n" +
                "order by different_region_course_count desc, total_region_volunteers desc, a.athlete_id desc;\n";
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) -> new Object[] {
                Athlete.from(rs.getString("name"), rs.getInt("athlete_id")),
                rs.getInt("different_region_course_count"),
                rs.getInt("total_region_volunteers"),
                rs.getInt("total_global_volunteer_count")
        });
    }
}
