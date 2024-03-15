package dnt.parkrun.database;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.AthleteCourseSummary;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.stats.RunsAtEvent;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AthleteCourseSummaryDao extends BaseDao
{
    final String tableName;

    public AthleteCourseSummaryDao(DataSource statsDataSource, Date date)
    {
        super(statsDataSource);
        tableName = "athlete_course_summary_" + DateConverter.formatDateForDbTable(date);
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

    public void writeAthleteCourseSummary(AthleteCourseSummary acs)
    {
        String sql = "insert into " + tableName + " (" +
                "athlete_id, course_id, run_count" +
                ") values ( " +
                ":athleteId, :courseId, :runCount" +
                ")";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("athleteId", acs.athlete.athleteId)
                .addValue("courseId", acs.course.courseId)
                .addValue("runCount", acs.countOfRuns)
        );

    }

    public List<Object[]> getAthleteCourseSummariesMap()
    {
        List<Object[]> results = new ArrayList<>();
        String sql = "select * from " + athleteTable()  +
                " join " + tableName + " using (athlete_id)";
        jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            results.add(new Object[]{
                    rs.getString("name"),
                    rs.getInt("athlete_id"),
                    rs.getInt("course_id"),
                    rs.getInt("run_count")
            });
            return null;
        });
        return results;
    }

    public List<Object[]> getAthleteCourseSummaries()
    {
        String sql = "select * from " + tableName +
                " join " + athleteTable() + " using (athlete_id)";
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
                new Object[] {
                        rs.getInt("athlete_id"),
                        rs.getInt("course_id"),
                        rs.getInt("run_count")
                });
    }

    public List<RunsAtEvent> getMostRunsInRegion()
    {
        String sql = "select athlete_id, name, course_long_name, run_count\n" +
                "from athlete_course_summary_2024_03_09\n" + // TODO
                "\n" +
                "right join course c using (course_long_name)\n" +
                "right join athlete a using (athlete_id)\n" +
                "\n" +
                "where c.country_code = 65\n" +
                "order by run_count desc limit 50;\n";
        return Collections.emptyList();
    }

    public List<RunsAtEvent> getMostRunsAtEvent()
    {
        // TODO Only includes pIndex and Most Events. Not necessarily the max runners.
        String sql =
                "select sub1.course_long_name, c.course_id, c.course_name, c.country_code, sub2.athlete_id, sub3.name, sub1.max_run_count\n" +
                        "from course c\n" +
                        "left join\n" +
                        "(\n" +
                        "    select course_long_name, max(run_count) as max_run_count\n" +
                        "    from athlete_course_summary_2024_03_09\n" + // TODO
                        "    group by course_long_name\n" +
                        ") as sub1 using (course_long_name)\n" +
                        "left join\n" +
                        "(\n" +
                        "    select athlete_id, course_long_name, run_count\n" +
                        "    from athlete_course_summary_2024_03_09\n" +
                        ") as sub2 on c.course_long_name = sub2.course_long_name and sub1.max_run_count = sub2.run_count\n" +
                        "left join\n" +
                        "(\n" +
                        "    select athlete_id, name\n" +
                        "    from athlete\n" +
                        ") as sub3 on sub2.athlete_id = sub3.athlete_id\n" +
                        "where c.country_code = 65 " +
                        "order by course_long_name";
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            Athlete athlete = Athlete.from(rs.getString("name"), rs.getInt("athlete_id"));
            Country country = Country.valueOf(rs.getInt("country_code"));
            Course course = new Course(
                    rs.getInt("course_id"),
                    rs.getString("course_name"),
                    country,
                    rs.getString("course_long_name"),
                    null);
            return new RunsAtEvent(
                    athlete,
                    course,
                    rs.getInt("max_run_count")
            );
        });
    }
}
