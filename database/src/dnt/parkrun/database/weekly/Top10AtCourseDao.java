package dnt.parkrun.database.weekly;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.database.BaseDao;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.stats.AtEvent;
import dnt.parkrun.datastructures.stats.RunsAtEvent;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

public class Top10AtCourseDao extends BaseDao
{
    private final Date date;

    private Top10AtCourseDao(DataSource dataSource, Date date)
    {
        super(dataSource);
        this.date = date;
        createTable();
    }

    public static Top10AtCourseDao getInstance(DataSource dataSource, Date date)
    {
        Top10AtCourseDao top10AtCourseDao = new Top10AtCourseDao(dataSource, date);
        top10AtCourseDao.createTable();
        return top10AtCourseDao;
    }

    String tableName()
    {
        return "top_10_at_course_" + DateConverter.formatDateForDbTable(date);
    }

    private void createTable()
    {
        String sql =
                "create table if not exists " + tableName() + " ( " +
                        "    athlete_id       INT               NOT NULL," +
                        "    course_id        INT               NOT NULL," +
                        "    run_count        INT               NOT NULL" +
                        ") DEFAULT CHARSET=utf8mb4";
        jdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }

    public void writeRunsAtEvents(List<AtEvent> runsAtEvents)
    {
        String sql = "insert into " + tableName() + " (" +
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
                    .addValue("runCount", runsAtEvent.count)
            );
        });
    }

    public List<AtEvent> getTop10AtCourse(String courseName)
    {
        String sql = "        select a.athlete_id, a.name, c.course_id, c.course_name, c.course_long_name, c.country_code, run_count \n" +
                "        from " + tableName() +
                "        join " + courseTable() + " c using (course_id)\n" +
                "        join " + athleteTable() + " a using (athlete_id)\n" +
                "where " +
                "   course_name = :courseName " +
                "   and run_count >= 2";
        return jdbc.query(sql, new MapSqlParameterSource("courseName", courseName), (rs, rowNum) -> {
            Athlete athlete = Athlete.from(rs.getString("name"), rs.getInt("athlete_id"));
            Course course = new Course(
                    rs.getInt("course_id"),
                    rs.getString("course_name"),
                    Country.valueOf(rs.getInt("country_code")),
                    rs.getString("course_long_name"),
                    null);
            return new RunsAtEvent(athlete, course, rs.getInt("run_count"));
        });
    }

    public List<AtEvent> getTop10InRegion()
    {
        String sql = "select a.name, a.athlete_id, c.course_id, c.course_name, c.country_code, c.course_long_name, run_count\n" +
                " from " + tableName() +
                " join " + athleteTable() + " a using (athlete_id)\n" +
                " join " + courseTable() + " c using (course_id)\n" +
                " order by run_count desc\n" +
                " limit 20;";
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) -> {
            Athlete athlete = Athlete.from(rs.getString("name"), rs.getInt("athlete_id"));
            Course course = new Course(
                    rs.getInt("course_id"),
                    rs.getString("course_name"),
                    Country.valueOf(rs.getInt("country_code")),
                    rs.getString("course_long_name"),
                    null);
            return new RunsAtEvent(athlete, course, rs.getInt("run_count"));
        });
    }
}
