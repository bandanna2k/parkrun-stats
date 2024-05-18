package dnt.parkrun.database.stats;

import dnt.parkrun.database.BaseDao;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.stats.RunsAtEvent;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;
import java.util.List;

public class Top10RunsDao extends BaseDao
{
    public Top10RunsDao(Country country, DataSource dataSource)
    {
        super(country, dataSource);
    }

    public List<RunsAtEvent> getTop10AtEvent(int courseId)
    {
        String sql =
                "select name, athlete_id, c.course_id, c.course_name, c.country_code, course_long_name, run_count\n" +
                        "from " + athleteTable() + "\n" +
                        "join\n" +
                        "(\n" +
                        "    select athlete_id, course_id, count(time_seconds) as run_count\n" +
                        "    from " + resultTable() + " r\n" +
                        "    group by athlete_id, course_id\n" +
                        "    having\n" +
                        "        athlete_id > 0\n" +
                        "        and course_id = :courseId\n" +
                        "    order by run_count desc, athlete_id asc\n" +
                        "    limit 20\n" +
                        ") as sub1 using (athlete_id)\n" +
                        "join " + courseTable() + " c using (course_id)";
        return jdbc.query(sql, new MapSqlParameterSource("courseId", courseId), (rs, rowNum) ->
        {
            Athlete athlete = Athlete.from(rs.getString("name"), rs.getInt("athlete_id"));
            Course course = new Course(
                    rs.getInt("course_id"),
                    rs.getString("course_name"),
                    Country.valueOf(rs.getInt("country_code")),
                    rs.getString("course_long_name"),
                    null);
            return new RunsAtEvent(
                    athlete,
                    course,
                    rs.getInt("run_count")
            );
        });
    }
}
