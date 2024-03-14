package dnt.parkrun.database;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.CountryEnum;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.stats.RunsAtEvent;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

public class Top10AtCourseDao
{
    private final NamedParameterJdbcTemplate jdbc;

    final String tableName;

    public Top10AtCourseDao(DataSource dataSource, Date date)
    {
        jdbc = new NamedParameterJdbcTemplate(dataSource);
        tableName = "top_10_at_course_" + DateConverter.formatDateForDbTable(date);

        createTable();
    }

    public void createTable()
    {
        String sql =
                "create table if not exists " + tableName + " ( " +
                        "    athlete_id       INT               NOT NULL," +
                        "    course_id        INT               NOT NULL," +
                        "    run_count        INT               NOT NULL" +
                        ") DEFAULT CHARSET=utf8mb4";
        jdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }

    public void writeRunsAtEvents(List<RunsAtEvent> runsAtEvents)
    {
        String sql = "insert into " + tableName + " (" +
                "athlete_id, course_id, run_count" +
                ") values ( " +
                ":athleteId, :courseId, :runCount" +
                ")";
        runsAtEvents.forEach(runsAtEvent -> {
//            jdbc.batchUpdate(sql, new MapSqlParameterSource()
//                    .addValue("athleteId", runsAtEvent.athlete.athleteId)
//                    .addValue("courseId", runsAtEvent.course.courseId)
//                    .addValue("runCount", runsAtEvent.runCount)
//            );
            jdbc.update(sql, new MapSqlParameterSource()
                    .addValue("athleteId", runsAtEvent.athlete.athleteId)
                    .addValue("courseId", runsAtEvent.course.courseId)
                    .addValue("runCount", runsAtEvent.runCount)
            );
        });
    }

    public List<RunsAtEvent> getTop10AtCourse(String courseName)
    {
        String sql = "        select a.athlete_id, a.name, c.course_id, c.course_name, c.course_long_name, c.country_code, run_count \n" +
                "        from " + tableName +
                "        join parkrun_stats.course c using (course_id)\n" +
                "        join parkrun_stats.athlete a using (athlete_id)\n" +
                "where course_name = :courseName";
        return jdbc.query(sql, new MapSqlParameterSource("courseName", courseName), (rs, rowNum) -> {
            Athlete athlete = Athlete.from(rs.getString("name"), rs.getInt("athlete_id"));
            Course course = new Course(
                    rs.getInt("course_id"),
                    rs.getString("course_name"),
                    new Country(CountryEnum.valueOf(rs.getInt("country_code")), null),
                    rs.getString("course_long_name"),
                    null);
            return new RunsAtEvent(athlete, course, rs.getInt("run_count"));
        });
    }
}
