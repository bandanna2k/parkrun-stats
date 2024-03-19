package dnt.parkrun.database;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.stats.AtEvent;
import dnt.parkrun.datastructures.stats.RunsAtEvent;
import dnt.parkrun.datastructures.stats.VolunteersAtEvent;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

public class Top10VoluteersAtCourseDao extends BaseDao
{
    final String tableName;

    public Top10VoluteersAtCourseDao(DataSource dataSource, Date date)
    {
        super(dataSource);
        tableName = "top_10_volunteers_at_course_" + DateConverter.formatDateForDbTable(date);

        createTable();
    }

    public void createTable()
    {
        String sql =
                "create table if not exists " + tableName + " ( " +
                        "    athlete_id       INT               NOT NULL," +
                        "    course_id        INT               NOT NULL," +
                        "    volunteer_count  INT               NOT NULL" +
                        ") DEFAULT CHARSET=utf8mb4";
        jdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }

    public void writeVolunteersAtEvents(List<AtEvent> volunteersAtEvents)
    {
        String sql = "insert into " + tableName + " (" +
                "athlete_id, course_id, volunteer_count" +
                ") values ( " +
                ":athleteId, :courseId, :runCount" +
                ")";
        volunteersAtEvents.forEach(volunteersAtEvent -> {
//            jdbc.batchUpdate(sql, new MapSqlParameterSource()
//                    .addValue("athleteId", runsAtEvent.athlete.athleteId)
//                    .addValue("courseId", runsAtEvent.course.courseId)
//                    .addValue("runCount", runsAtEvent.runCount)
//            );
            jdbc.update(sql, new MapSqlParameterSource()
                    .addValue("athleteId", volunteersAtEvent.athlete.athleteId)
                    .addValue("courseId", volunteersAtEvent.course.courseId)
                    .addValue("volunteerCount", volunteersAtEvent.count)
            );
        });
    }

    public List<AtEvent> getTop10VolunteersAtCourse(String courseName)
    {
        String sql = "        select a.athlete_id, a.name, c.course_id, c.course_name, c.course_long_name, c.country_code, volunteer_count \n" +
                "        from " + tableName +
                "        join " + courseTable() + " c using (course_id)\n" +
                "        join " + athleteTable() + " a using (athlete_id)\n" +
                "where " +
                "   course_name = :courseName " +
                "   and volunteer_count >= 2";
        return jdbc.query(sql, new MapSqlParameterSource("courseName", courseName), (rs, rowNum) -> {
            Athlete athlete = Athlete.from(rs.getString("name"), rs.getInt("athlete_id"));
            Course course = new Course(
                    rs.getInt("course_id"),
                    rs.getString("course_name"),
                    Country.valueOf(rs.getInt("country_code")),
                    rs.getString("course_long_name"),
                    null);
            return new VolunteersAtEvent(athlete, course, rs.getInt("volunteer_count"));
        });
    }

    public List<RunsAtEvent> getTop10VolunteersInRegion()
    {
        String sql = "select a.name, a.athlete_id, c.course_id, c.course_name, c.country_code, c.course_long_name, volunteers_count\n" +
                " from " + tableName +
                " join " + athleteTable() + " a using (athlete_id)\n" +
                " join " + courseTable() + " c using (course_id)\n" +
                " order by volunteers_count desc\n" +
                " limit 20;";
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) -> {
            Athlete athlete = Athlete.from(rs.getString("name"), rs.getInt("athlete_id"));
            Course course = new Course(
                    rs.getInt("course_id"),
                    rs.getString("course_name"),
                    Country.valueOf(rs.getInt("country_code")),
                    rs.getString("course_long_name"),
                    null);
            return new RunsAtEvent(athlete, course, rs.getInt("volunteers_count"));
        });
    }
}
