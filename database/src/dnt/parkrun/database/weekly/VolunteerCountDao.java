package dnt.parkrun.database.weekly;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.database.BaseDao;
import dnt.parkrun.database.Database;
import dnt.parkrun.datastructures.Athlete;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.Date;
import java.util.List;

import static dnt.parkrun.database.VolunteerDao.getMostVolunteersSubQuery2;
import static dnt.parkrun.database.VolunteerDao.getSqlForVolunteersAtDifferentCourse;

public class VolunteerCountDao extends BaseDao
{
    private final Date date;

    public static VolunteerCountDao getInstance(Database database, Date date)
    {
        VolunteerCountDao mostVolunteersDao = new VolunteerCountDao(database, date);
        mostVolunteersDao.init();
        return mostVolunteersDao;
    }
    private VolunteerCountDao(Database database, Date date)
    {
        super(database);
        this.date = date;
        init();
    }
    private void init()
    {
        String sql = STR."""
                        create table if not exists \{getTableName()} (
                            athlete_id                     INT               NOT NULL    PRIMARY KEY,
                            total_global_volunteer_count   INT               NOT NULL,
                            v_index                        INT               NOT NULL,
                            needed_for_next_v_index        INT               NOT NULL
                        )
                        DEFAULT CHARSET=utf8mb4;
                """;
        jdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }

    public void insertVolunteerCount(int athleteId, int volunteerCount, int vIndex, int neededForNextVIndex)
    {
        String sql = STR."""
                insert into \{getTableName()} (
                    athlete_id, total_global_volunteer_count, v_index, needed_for_next_v_index
                ) values (
                    :athleteId,
                    :volunteerCount,
                    :vIndex,
                    :neededForNextVIndex
                )
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("athleteId", athleteId)
                .addValue("volunteerCount", volunteerCount)
                .addValue("vIndex", vIndex)
                .addValue("neededForNextVIndex", neededForNextVIndex)
        );
    }

    private String getTableName()
    {
        // TODO Should be volunteer_data now as it includes vIndex
        return weeklyDatabaseName + ".volunteer_count_" + DateConverter.formatDateForDbTable(date);
    }

    public List<Object[]> getMostVolunteers()
    {
        String sql = STR."""
select a.name, a.athlete_id,
    sub1.count as different_region_course_count,
    sub2.count as total_region_volunteers,
    vc.total_global_volunteer_count, vc.v_index, vc.needed_for_next_v_index
from \{athleteTable()} a
join (
    \{getSqlForVolunteersAtDifferentCourse(volunteerTable())}
) as sub1 on sub1.athlete_id = a.athlete_id
join (
    \{getMostVolunteersSubQuery2(volunteerTable())}
) as sub2 on sub2.athlete_id = a.athlete_id
left join \{getTableName()} vc on vc.athlete_id = a.athlete_id where a.name is not null
order by different_region_course_count desc, total_region_volunteers desc, a.athlete_id desc
""";
        List<Object[]> query = jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) -> new Object[]{
                Athlete.from(rs.getString("name"), rs.getInt("athlete_id")),
                rs.getInt("different_region_course_count"),
                rs.getInt("total_region_volunteers"),
                rs.getInt("total_global_volunteer_count"),
                rs.getInt("v_index"),
                rs.getInt("needed_for_next_v_index")
        });
        query.forEach(result -> {
            Athlete athlete = (Athlete) result[0];
            int regionCourseCount = (int)result[1];
            int regionTotalVolunteers = (int)result[2];
            int globalTotalVolunteers = (int)result[3];
            assert globalTotalVolunteers >= regionTotalVolunteers :
                    String.format("Region volunteers: %d, Global volunteers: %d, Athlete: %s", regionTotalVolunteers, globalTotalVolunteers, athlete);

            // Not true. You can volunteer at 2 courses. Only 1 is counted for total. But course should be counted
            assert regionCourseCount <= regionTotalVolunteers : "Global volunteers should be greater than regional volunteers.";
        });
        return query;
    }
}
