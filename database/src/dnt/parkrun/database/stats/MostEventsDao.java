package dnt.parkrun.database.stats;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.database.BaseDao;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Country;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

import static dnt.parkrun.common.DateConverter.SEVEN_DAYS_IN_MILLIS;
import static java.util.Collections.emptyList;

public class MostEventsDao extends BaseDao
{
    private static final String MIN_DIFFERENT_REGION_COURSE_COUNT = "20";

    private final Date date;

    public static MostEventsDao getOrCreate(Country country, DataSource dataSource, Date date)
    {
        MostEventsDao mostVolunteersDao = new MostEventsDao(country, dataSource, date);
        mostVolunteersDao.init();
        return mostVolunteersDao;
    }
    private MostEventsDao(Country country, DataSource dataSource, Date date)
    {
        super(country, dataSource);
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
                    different_course_count, total_runs)
                select a.athlete_id,
                    sub1.count as different_region_course_count,
                    sub2.count as total_region_runs,
                    0 as different_course_count, 0 as total_runs from \{athleteTable()} a
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
                order by different_region_course_count desc, total_region_runs desc, a.athlete_id desc
            """;
            jdbc.update(sql, new MapSqlParameterSource("minDifferentRegionCourseCount", MIN_DIFFERENT_REGION_COURSE_COUNT));
        }
        catch (DuplicateKeyException ex)
        {
            System.out.println("WARNING Most events table already populated.");
        }
    }

    /*
    Second pass
     */
    public void updateDifferentCourseRecord(int athleteId, int differentCourseCount, int totalRuns)
    {
        String sql = "update " + getTableName() + " set " +
                "different_course_count = :differentCourseCount, " +
                "total_runs = :totalRuns " +
                "where athlete_id = :athleteId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("athleteId", athleteId)
                .addValue("differentCourseCount", differentCourseCount)
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
            different_course_count, total_runs
        from \{mostEventsTable}
        join \{athleteTable()} a using (athlete_Id)
        order by different_region_course_count desc, total_region_runs desc, athlete_id asc
        """;
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
                new MostEventsRecord(
                        Athlete.from(
                                rs.getString("name"),
                                rs.getInt("athlete_id")
                        ),
                        rs.getInt("different_region_course_count"),
                        rs.getInt("total_region_runs"),
                        rs.getInt("different_course_count"),
                        rs.getInt("total_runs")
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

    public static class MostEventsRecord
    {
        public final Athlete athlete;
        public final int differentRegionCourseCount;
        public final int totalRegionRuns;
        public final int differentCourseCount;
        public final int totalRuns;

        public int positionDelta = 0;
        public boolean isNewEntry = false;

        public MostEventsRecord(Athlete athlete,
                                int differentRegionCourseCount,
                                int totalRegionRuns,
                                int differentCourseCount,
                                int totalRuns)
        {
            this.athlete = athlete;
            this.differentRegionCourseCount = differentRegionCourseCount;
            this.totalRegionRuns = totalRegionRuns;
            this.differentCourseCount = differentCourseCount;
            this.totalRuns = totalRuns;
        }

        @Override
        public String toString()
        {
            return "MostEventsRecord{" +
                    "athleteId=" + athlete +
                    ", differentRegionCourseCount=" + differentRegionCourseCount +
                    ", totalRegionRuns=" + totalRegionRuns +
                    ", differentCourseCount=" + differentCourseCount +
                    ", totalRuns=" + totalRuns +
                    ", positionDelta=" + positionDelta +
                    ", isNewEntry=" + isNewEntry +
                    '}';
        }
    }
}
