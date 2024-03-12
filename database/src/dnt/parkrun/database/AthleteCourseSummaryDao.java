package dnt.parkrun.database;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.AthleteCourseSummary;
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

    public Map<Integer, List<AthleteCourseSummary>> getAthleteCourseSummariesMap()
    {
        Map<Integer, List<AthleteCourseSummary>> result = new HashMap<>();
        String sql = "select * from athlete "  +
                " join " + tableName + " using (athlete_id)";
        jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            int athleteId = rs.getInt("athlete_id");
            List<AthleteCourseSummary> summaries = result.computeIfAbsent(athleteId, k -> new ArrayList<>());
            summaries.add(
                    new AthleteCourseSummary(
                        Athlete.from(
                            rs.getString("name"),
                            athleteId
                        ),
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
}
