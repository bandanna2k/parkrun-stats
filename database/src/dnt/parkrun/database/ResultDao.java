package dnt.parkrun.database;

import dnt.parkrun.datastructures.AgeGroup;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.datastructures.Time;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class ResultDao
{
    private final NamedParameterJdbcOperations jdbc;

    public ResultDao(DataSource dataSource)
    {
        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    @Deprecated // Testing only. Do not use. Results too large
    List<Result> getResults()
    {
        String sql = "select * from result " +
                "right join athlete using (athlete_id)";
        List<Result> query = jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            return new Result(
                    rs.getInt("course_id"),
                    rs.getDate("date"),
                    rs.getInt("position"),
                    Athlete.from(
                            rs.getString("name"),
                            rs.getInt("athlete_id")
                    ),
                    Time.from(rs.getInt("time_seconds")),     // TODO Needs converting to int
                    null, AgeGroup.UNKNOWN, 0);
        });
        return query;
    }

    public void insert(Result result)
    {
        String sql = "insert into result (" +
                "athlete_id, course_id, date, position, time_seconds" +
                ") values ( " +
                ":athleteId, :courseId, :date, :position, :time_seconds" +
                ")";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("athleteId", result.athlete.athleteId)
                .addValue("courseId", result.courseId)
                .addValue("date", result.date)
                .addValue("position", result.position)
                .addValue("time_seconds", result.time.getTotalSeconds())
        );
    }

    public void tableScan(Consumer<Result> consumer)
    {
        String sql = "select * from result " +
                "right join athlete using (athlete_id)";
        jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            Result result = new Result(
                    rs.getInt("course_id"),
                    rs.getDate("date"),
                    rs.getInt("position"),
                    Athlete.from(
                            rs.getString("name"),
                            rs.getInt("athlete_id")
                    ),
                    Time.from(rs.getInt("time_seconds")),
                    null, AgeGroup.UNKNOWN, 0);
            consumer.accept(result);
            return null;
        });
    }

    public String getFirstRunsJsonArrays(int athleteId)
    {
        String sql = "select JSON_ARRAYAGG(course_id) as json_courses, JSON_ARRAYAGG(unix_timestamp(first_run)) as json_first_runs " +
                "from " +
                "( " +
                "    select athlete_id, course_id, min(date) as first_run  " +
                "    from result  " +
                "    group by athlete_id, course_id  " +
                "    having athlete_id = :athleteId " +
                ") as sub1; ";
        return jdbc.queryForObject(sql, new MapSqlParameterSource("athleteId", athleteId), (rs, rowNum) ->
                String.format("[%s,%s]", rs.getString("json_courses"), rs.getString("json_first_runs")));
    }

    @Deprecated
    public void backfillUpdateResultWithAgeGroup(Athlete athlete, int courseId, Date date, AgeGroup ageGroup, Integer ageGrade)
    {
        String sql = "update result " +
                "set " +
                "   age_group = :ageGroup, " +
                "   age_grade = :ageGrade " +
                "where athlete_id = :athleteId " +
                " and course_id = :courseId " +
                " and date = :date ";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("athleteId", athlete.athleteId)
                .addValue("courseId", courseId)
                .addValue("date", date)
                .addValue("ageGroup", ageGroup.dbCode)
                .addValue("ageGrade", ageGrade)
        );
    }
}
