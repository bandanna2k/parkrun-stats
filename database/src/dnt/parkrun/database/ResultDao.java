package dnt.parkrun.database;

import dnt.parkrun.datastructures.*;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ResultDao extends BaseDao
{

    public static final String SQL_FOR_INSERT = "insert into result (" +
            "athlete_id, course_id, date, position, time_seconds, age_group, age_grade" +
            ") values ( " +
            ":athleteId, :courseId, :date, :position, :time_seconds, :ageCategory, :ageGrade" +
            ")";

    public ResultDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Deprecated // Testing only. Do not use. Results too large
    List<Result> getResults()
    {
        String sql = "select * from result " +
                "right join athlete using (athlete_id) " +
                "order by course_id asc, date desc, position asc, athlete_id asc";
        List<Result> query = jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            Integer ageCategory = rs.getInt("age_group");
            Integer ageGrade = rs.getInt("age_grade");
            return new Result(
                    rs.getInt("course_id"),
                    rs.getDate("date"),
                    rs.getInt("position"),
                    Athlete.from(
                            rs.getString("name"),
                            rs.getInt("athlete_id")
                    ),
                    Time.from(rs.getInt("time_seconds")),     // TODO Needs converting to int
                    AgeCategory.from(ageCategory),
                    AgeGrade.newInstanceFromDb(ageGrade));
        });
        return query;
    }

    public List<Result> getResults(int courseId, Date date)
    {
        String sql = "select * from result " +
                "left join athlete using (athlete_id) " +
                "where course_id = :courseId " +
                "  and date = :date " +
                "order by course_id asc, date desc, position asc, athlete_id asc";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("courseId", courseId)
                .addValue("date", date);
        return jdbc.query(sql, params, (rs, rowNum) ->
        {
            Integer ageCategory = rs.getInt("age_group");
            Integer ageGrade = rs.getInt("age_grade");
            return new Result(
                    rs.getInt("course_id"),
                    rs.getDate("date"),
                    rs.getInt("position"),
                    Athlete.from(
                            rs.getString("name"),
                            rs.getInt("athlete_id")
                    ),
                    Time.from(rs.getInt("time_seconds")),     // TODO Needs converting to int
                    AgeCategory.from(ageCategory),
                    AgeGrade.newInstanceFromDb(ageGrade));
        });
    }


    public void insert(Result result)
    {
        jdbc.update(SQL_FOR_INSERT, new MapSqlParameterSource()
                .addValue("athleteId", result.athlete.athleteId)
                .addValue("courseId", result.courseId)
                .addValue("date", result.date)
                .addValue("position", result.position)
                .addValue("time_seconds", result.time == null ? 0 : result.time.getTotalSeconds())
                .addValue("ageCategory", result.ageCategory.dbCode)
                .addValue("ageGrade", result.ageGrade.getAgeGradeForDb())
        );
    }

    public void insert(List<Result> results)
    {
        jdbc.batchUpdate(SQL_FOR_INSERT, results.stream().map(result -> new MapSqlParameterSource()
                .addValue("athleteId", result.athlete.athleteId)
                .addValue("courseId", result.courseId)
                .addValue("date", result.date)
                .addValue("position", result.position)
                .addValue("time_seconds", result.time == null ? 0 : result.time.getTotalSeconds())
                .addValue("ageCategory", result.ageCategory.dbCode)
                .addValue("ageGrade", result.ageGrade.getAgeGradeForDb())).toArray(MapSqlParameterSource[]::new)
        );
    }

    public void tableScan(Consumer<Result> consumer)
    {
        tableScan(consumer, "");
    }
    public void tableScan(Consumer<Result> consumer, String orderBy)
    {
        String sql = "select * from result " +
                "join athlete using (athlete_id) " +
                orderBy;
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
                    rs.getString("age_group") == null ? null : AgeCategory.from(rs.getInt("age_group")),
                    rs.getString("age_grade") == null ? null : AgeGrade.newInstanceFromDb(rs.getInt("age_grade")));
            consumer.accept(result);
            return null;
        });
    }
    public void tableScanResultAndEventNumber(BiConsumer<Result, Integer> consumer)
    {
        String sql = STR."""
                select
                    r.course_id, r.date, r.position, r.time_seconds, r.age_group, r.age_grade,
                    a.name, a.athlete_id,
                    ces.event_number
                from result r
                join \{athleteTable()} a using (athlete_id)
                join \{courseEventSummaryTable()} ces on r.date = ces.date and r.course_id = ces.course_id
                """;
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
                    rs.getString("age_group") == null ? null : AgeCategory.from(rs.getInt("age_group")),
                    rs.getString("age_grade") == null ? null : AgeGrade.newInstanceFromDb(rs.getInt("age_grade")));
            consumer.accept(result, rs.getInt("event_number"));
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
    public void backfillUpdateResultWithAgeCategory(Result result)
    {
        String sql = "update result " +
                "set " +
                "   athlete_id = :athleteId," +
                "   age_group = :ageCategory, " +
                "   age_grade = :ageGrade, " +
                "   time_seconds = :timeSeconds " +
                "where course_id = :courseId " +
                " and date = :date " +
                " and position = :position";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("athleteId", result.athlete.athleteId)
                .addValue("courseId", result.courseId)
                .addValue("date", result.date)
                .addValue("position", result.position)
                .addValue("ageCategory", result.ageCategory.dbCode)
                .addValue("ageGrade", result.ageGrade.getAgeGradeForDb())
                .addValue("timeSeconds", null == result.time ? 0 : result.time.getTotalSeconds())
        );
    }

    public void deleteResults(Integer courseId, Date date)
    {
        String sql = "delete from result " +
                "where course_id = :courseId " +
                "  and date = :date ";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("courseId", courseId)
                .addValue("date", date);
        jdbc.update(sql, params);
    }
}
