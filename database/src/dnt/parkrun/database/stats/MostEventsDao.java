package dnt.parkrun.database.stats;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.database.BaseDao;
import dnt.parkrun.database.Database;
import dnt.parkrun.datastructures.stats.MostEventsRecord;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.Date;
import java.util.List;

import static dnt.parkrun.common.DateConverter.SEVEN_DAYS_IN_MILLIS;
import static java.util.Collections.emptyList;

public class MostEventsDao extends BaseDao
{
    private static final String MIN_DIFFERENT_REGION_COURSE_COUNT = "20";

    private static final String ORDER_BY = STR."""
        order by
            runs_needed_for_regionnaire asc,
            different_region_course_count desc, total_region_runs desc, 
            different_course_count desc, total_runs desc, 
            athlete_id asc
            """;

    private final Date date;

    public static MostEventsDao getOrCreate(Database database, Date date)
    {
        MostEventsDao mostVolunteersDao = new MostEventsDao(database, date);
        mostVolunteersDao.init();
        return mostVolunteersDao;
    }
    private MostEventsDao(Database database, Date date)
    {
        super(database);
        this.date = date;
        init();
    }
    private String getTableName()
    {
        return getTableName(date);
    }
    String getTableName(Date date)
    {
        return weeklyDatabaseName + ".most_events_for_region_" + DateConverter.formatDateForDbTable(date);
    }

    private void init()
    {
        String sql = STR."""
                create table if not exists \{getTableName()} (
                      athlete_id                     INT             NOT NULL,
                      different_region_course_count  INT             NOT NULL,
                      total_region_runs              INT             NOT NULL,
                      different_course_count         INT             NOT NULL,
                      total_runs                     INT             NOT NULL,
                      runs_needed_for_regionnaire    INT             NOT NULL,
                      inaugural_runs                 INT             NOT NULL,
                      regionnaire_count              INT             NOT NULL,
                      PRIMARY KEY (athlete_id)
                ) DEFAULT CHARSET=utf8mb4
                """;
        jdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }

    /*
    First pass
     */
    public void populateMostEventsTable()
    {
        try
        {
            String sql = STR."""
                insert into \{getTableName()}(
                    athlete_id, different_region_course_count, total_region_runs,
                    different_course_count, total_runs,
                    runs_needed_for_regionnaire, inaugural_runs, regionnaire_count)
                select a.athlete_id,
                    sub1.count as different_region_course_count,
                    sub2.count as total_region_runs,
                    0 as different_course_count,
                    0 as total_runs,
                    0 as runs_needed_for_regionnaire,
                    0 as inaugural_runs,
                    0 as regionnaire_count
                from \{athleteTable()} a
                join (
                    select athlete_id, count(course_id) as count
                    from (
                        select distinct athlete_id, course_id
                        from \{resultTable()}
                    ) as sub1a
                    group by athlete_id
                    having count >= :minDifferentRegionCourseCount
                    order by count desc, athlete_id asc
                ) as sub1 on sub1.athlete_id = a.athlete_id
                join
                (
                    select athlete_id, count(concat) as count
                    from (
                        select athlete_id, concat(athlete_id, '-', course_id, '-', date, '-', position) as concat
                        from \{resultTable()}
                    ) as sub2a
                    group by athlete_id
                    order by count desc, athlete_id asc
                ) as sub2 on sub2.athlete_id = a.athlete_id
                where a.name is not null
                \{ORDER_BY}
            """;
            jdbc.update(sql, new MapSqlParameterSource("minDifferentRegionCourseCount", MIN_DIFFERENT_REGION_COURSE_COUNT));
        }
        catch (DuplicateKeyException ex)
        {
            System.out.println("WARNING Most events table already populated.");
        }
    }

    /*
    Second pass to update empty columns
     */
    public void updateDifferentCourseRecord(int athleteId, int differentCourseCount, int totalRuns, int runsNeeded,
                                            int inauguralRuns, int regionnaireCount)
    {
        String sql = STR."""
            update \{getTableName()}
            set
                different_course_count = :differentCourseCount,
                total_runs = :totalRuns,
                runs_needed_for_regionnaire = :runsNeeded,
                inaugural_runs = :inauguralRuns,
                regionnaire_count = :regionnaireCount
                where athlete_id = :athleteId
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("athleteId", athleteId)
                .addValue("differentCourseCount", differentCourseCount)
                .addValue("runsNeeded", runsNeeded)
                .addValue("inauguralRuns", inauguralRuns)
                .addValue("regionnaireCount", regionnaireCount)
                .addValue("totalRuns", totalRuns);
        jdbc.update(sql, params);
    }

    public List<MostEventsRecord> getMostEvents()
    {
        return getMostEvents(date);
    }
    public List<MostEventsRecord> getMostEventsForLastWeek()
    {
        try
        {
            Date lastWeek = new Date();
            lastWeek.setTime(date.getTime() - SEVEN_DAYS_IN_MILLIS);
            return getMostEvents(lastWeek);
        }
        catch (Exception ex)
        {
            System.out.println("WARNING No different course count for last week");
        }
        return emptyList();
    }
    private List<MostEventsRecord> getMostEvents(Date date)
    {
        String mostEventsTable = weeklyDatabaseName + ".most_events_for_region_" + DateConverter.formatDateForDbTable(date);
        String sql = STR."""
        select a.name, a.athlete_id,
            different_region_course_count, total_region_runs,
            different_course_count, total_runs,
            runs_needed_for_regionnaire, inaugural_runs, regionnaire_count
        from \{mostEventsTable}
        join \{athleteTable()} a using (athlete_Id)
        \{ORDER_BY}
        """;
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
                new MostEventsRecord(
                        rs.getString("name"),
                        rs.getInt("athlete_id"),
                        rs.getInt("different_region_course_count"),
                        rs.getInt("total_region_runs"),
                        rs.getInt("different_course_count"),
                        rs.getInt("total_runs"),
                        rs.getInt("runs_needed_for_regionnaire"),
                        rs.getInt("inaugural_runs"),
                        rs.getInt("regionnaire_count")
                ));
    }

    /**
     * First runs for Most Event athletes
     **/
    public List<Object[]> getFirstRuns()
    {
        String sql = "select athlete_id, course_id, first_run " +
                "from " + getTableName() + " " +
                "join  " +
                "( " +
            "        select athlete_id, course_id, min(date) as first_run  " +
            "        from " + resultTable() +"  " +
            "        group by athlete_id, course_id  " +
                ") as sub2 using (athlete_id)";
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) -> new Object[] {
            rs.getInt("athlete_id"),
            rs.getInt("course_id"),
            rs.getDate("first_run")
        });
    }
}
