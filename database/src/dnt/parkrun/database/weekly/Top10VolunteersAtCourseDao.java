package dnt.parkrun.database.weekly;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.database.BaseDao;
import dnt.parkrun.database.Database;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.stats.AtEvent;
import dnt.parkrun.datastructures.stats.VolunteersAtEvent;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.Date;
import java.util.List;

public class Top10VolunteersAtCourseDao extends BaseDao
{
    private final Date date;

    private Top10VolunteersAtCourseDao(Database database, Date date)
    {
        super(database);
        this.date = date;
    }

    public static Top10VolunteersAtCourseDao getInstance(Database database, Date date)
    {
        Top10VolunteersAtCourseDao top10VolunteersAtCourseDao = new Top10VolunteersAtCourseDao(database, date);
        top10VolunteersAtCourseDao.createTable();
        return top10VolunteersAtCourseDao;
    }

    String tableName()
    {
        return weeklyDatabaseName + ".top_10_volunteers_at_course_" + DateConverter.formatDateForDbTable(date);
    }

    public void createTable()
    {
        String sql =
                "create table if not exists " + tableName() + " ( " +
                        "    athlete_id       INT               NOT NULL," +
                        "    course_id        INT               NOT NULL," +
                        "    volunteer_count  INT               NOT NULL" +
                        ") DEFAULT CHARSET=utf8mb4";
        jdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }

    public void writeVolunteersAtEvents(List<AtEvent> volunteersAtEvents)
    {
        String sql = "insert into " + tableName() + " (" +
                "athlete_id, course_id, volunteer_count" +
                ") values ( " +
                ":athleteId, :courseId, :volunteerCount" +
                ")";
        volunteersAtEvents.forEach(volunteersAtEvent -> {
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
                "        from " + tableName() +
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

    public List<Object[]> getTop10VolunteersInRegion()
    {
        String sql = "select a.name, sub1.athlete_id, sub1.course_id, sub1.total_volunteer_count\n" +
                "from " + athleteTable() + " a " +
                "join " +
                "(" +
                "    select athlete_id, course_id, sum(volunteer_count) as total_volunteer_count\n" +
                "    from " + tableName() +
                "    group by athlete_id, course_id\n" +
                "    order by total_volunteer_count desc\n" +
                "    limit 20" +
                ") as sub1 using (athlete_id)";
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
                new Object[]
                        {
                                Athlete.from(rs.getString("name"), rs.getInt("athlete_id")),
                                rs.getInt("course_id"),
                                rs.getInt("total_volunteer_count")
                        });
    }
}
