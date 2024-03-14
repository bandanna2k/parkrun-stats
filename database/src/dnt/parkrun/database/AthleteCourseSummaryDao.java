package dnt.parkrun.database;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.AthleteCourseSummary;
import dnt.parkrun.datastructures.stats.RunsAtEvent;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.*;

public class AthleteCourseSummaryDao
{
    private final NamedParameterJdbcTemplate jdbc;

    final String tableName;

    public AthleteCourseSummaryDao(DataSource dataSource, Date date)
    {
        jdbc = new NamedParameterJdbcTemplate(dataSource);
        tableName = "athlete_course_summary_" + DateConverter.formatDateForDbTable(date);

        createTable();
    }

    public void createTable()
    {
        String sql =
                "create table if not exists " + tableName + " ( " +
                        "    athlete_id       INT               NOT NULL," +
                        "    course_long_name VARCHAR(255)      NOT NULL," +
                        "    run_count        INT               NOT NULL" +
                        ") DEFAULT CHARSET=utf8mb4";
        jdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }

    public void writeAthleteCourseSummary(AthleteCourseSummary acs)
    {
        String sql = "insert into " + tableName + " (" +
                "athlete_id, course_long_name, run_count" +
                ") values ( " +
                ":athleteId, :courseLongName, :runCount" +
                ")";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("athleteId", acs.athlete.athleteId)
                .addValue("courseLongName", acs.courseLongName)
                .addValue("runCount", acs.countOfRuns)
        );

    }

    public Map<Athlete, List<AthleteCourseSummary>> getAthleteCourseSummariesMap()
    {
        Map<Athlete, List<AthleteCourseSummary>> result = new TreeMap<>(Comparator.comparingInt(a -> a.athleteId));
        String sql = "select * from athlete "  +
                " join " + tableName + " using (athlete_id)";
        jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            Athlete athlete = Athlete.from(
                    rs.getString("name"),
                    rs.getInt("athlete_id")
            );
            List<AthleteCourseSummary> summaries = result.computeIfAbsent(athlete, k -> new ArrayList<>());
            summaries.add(
                    new AthleteCourseSummary(
                            athlete,
                            rs.getString("course_long_name"),
                            rs.getInt("run_count")));
            return null;
        });
        return result;
    }

    public List<AthleteCourseSummary> getAthleteCourseSummaries()
    {
        String sql = "select * from " + tableName +
                " join athlete using (athlete_id)";
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
                new AthleteCourseSummary(
                        Athlete.from(
                                rs.getString("name"),
                                rs.getInt("athlete_id")
                        ),
                        rs.getString("course_long_name"),
                        rs.getInt("run_count")
                ));
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
                "select sub1.course_long_name, c.course_name, c.country_code, sub2.athlete_id, sub3.name, sub1.max_run_count\n" +
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
            return new RunsAtEvent(
                    athlete,
                    rs.getString("course_long_name"),
                    rs.getString("course_name"),
                    rs.getInt("max_run_count")
            );
        });
    }
}
