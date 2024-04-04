package dnt.parkrun.database.weekly;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.database.BaseDao;
import dnt.parkrun.datastructures.AthleteCourseSummary;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

public class AthleteCourseSummaryDao extends BaseDao
{
    private final Date date;

    private AthleteCourseSummaryDao(DataSource statsDataSource, Date date)
    {
        super(statsDataSource);
        this.date = date;
    }

    public static AthleteCourseSummaryDao getInstance(DataSource statsDataSource, Date date)
    {
        AthleteCourseSummaryDao athleteCourseSummaryDao = new AthleteCourseSummaryDao(statsDataSource, date);
        athleteCourseSummaryDao.createTable();
        return athleteCourseSummaryDao;
    }

    String tableName()
    {
        return "athlete_course_summary_" + DateConverter.formatDateForDbTable(date);
    }


    public void createTable()
    {
        String sql =
                "create table if not exists " + tableName() + " ( " +
                        "    athlete_id       INT               NOT NULL," +
                        "    course_id        INT               NOT NULL," +
                        "    run_count        INT               NOT NULL," +
                        "    global_volunteer_count     INT     NOT NULL        DEFAULT 0," +
                        "   PRIMARY KEY (athlete_id, course_id)" +
                        ") DEFAULT CHARSET=utf8mb4";
        jdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }

    public void writeAthleteCourseSummary(AthleteCourseSummary acs)
    {
        String sql = "insert into " + tableName() + " (" +
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

    public List<Object[]> getAthleteCourseSummaries()
    {
        String sql = "select * from " + athleteTable()  +
                " join " + tableName() + " using (athlete_id) order by athlete_id asc, course_id asc";
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
                new Object[]{
                        rs.getString("name"),
                        rs.getInt("athlete_id"),
                        rs.getInt("course_id"),
                        rs.getInt("run_count")
                });
    }
}
