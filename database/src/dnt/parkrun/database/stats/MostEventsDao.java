package dnt.parkrun.database.stats;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.database.BaseDao;
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

    public static MostEventsDao getOrCreate(DataSource dataSource, Date date)
    {
        MostEventsDao mostVolunteersDao = new MostEventsDao(dataSource, date);
        mostVolunteersDao.init();
        return mostVolunteersDao;
    }
    private MostEventsDao(DataSource dataSource, Date date)
    {
        super(dataSource);
        this.date = date;
        init();
    }
    private String getTableName()
    {
        return "most_events_for_region_" + DateConverter.formatDateForDbTable(date);
    }

    private void init()
    {
        String sql =
                "create table if not exists " + getTableName() + " ( " +
                "    name                           VARCHAR(255)    NOT NULL," +
                "    athlete_id                     INT             NOT NULL," +
                "    different_region_course_count  INT             NOT NULL," +
                "    total_region_runs              INT             NOT NULL," +
                "    different_course_count         INT             NOT NULL," +
                "    total_runs                     INT             NOT NULL," +
                " PRIMARY KEY (athlete_id) " +
                ") DEFAULT CHARSET=utf8mb4";
        jdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }

    /*
    First pass
     */
    public void populateMostEventsTable()
    {
        try
        {
            String sql = "insert into " + getTableName() + "(name, athlete_id, different_region_course_count, total_region_runs, different_course_count, total_runs)" +
                    "select a.name, a.athlete_id, " +
                    "sub1.count as different_region_course_count, sub2.count as total_region_runs, " +
                    "0 as different_course_count, 0 as total_runs " +
                    "from " + athleteTable() + " a " +
                    "join   " +
                    "( " +
                    "    select athlete_id, count(course_id) as count " +
                    "    from (select distinct athlete_id, course_id from " + resultTable() + ") as sub1a " +
                    "    group by athlete_id " +
                    "    having count >= " + MIN_DIFFERENT_REGION_COURSE_COUNT +
                    "    order by count desc, athlete_id asc  " +
                    ") as sub1 on sub1.athlete_id = a.athlete_id " +
                    "join " +
                    "( " +
                    "    select athlete_id, count(concat) as count " +
                    "    from (select athlete_id, concat(athlete_id, '-', course_id, '-', date, '-', position) as concat from " + resultTable() + ") as sub2a " +
                    "    group by athlete_id " +
                    "    order by count desc, athlete_id asc  " +
                    ") as sub2 on sub2.athlete_id = a.athlete_id " +
                    "where a.name is not null " +
                    "order by different_region_course_count desc, total_region_runs desc, a.athlete_id desc ";

            jdbc.update(sql, EmptySqlParameterSource.INSTANCE);
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
        String differentCourseCountTableName = "most_events_for_region_" + DateConverter.formatDateForDbTable(date);
        String sql =
                "select name, athlete_id, " +
                        "different_region_course_count, total_region_runs," +
                        "different_course_count, total_runs  " +
                        "from " + differentCourseCountTableName +
                        " order by different_region_course_count desc, total_region_runs desc, athlete_id asc ";
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
                new MostEventsRecord(
                        rs.getString("name"),
                        rs.getInt("athlete_id"),
                        rs.getInt("different_region_course_count"),
                        rs.getInt("total_region_runs"),
                        rs.getInt("different_course_count"),
                        rs.getInt("total_runs")
                ));
    }

    public List<Object[]> getFirstRuns()
    {
        String sql = "select athlete_id, json_course_ids, json_first_runs " +
                "from most_events_for_region_2024_03_23 " +
                "join  " +
                "( " +
                "    select athlete_id, JSON_ARRAYAGG(course_id) as json_course_ids, JSON_ARRAYAGG(unix_timestamp(first_run)) as json_first_runs " +
                "    from " +
                "    ( " +
                "        select athlete_id, course_id, min(date) as first_run  " +
                "        from parkrun_stats.result  " +
                "        group by athlete_id, course_id  " +
                "    ) as sub1 " +
                "    group by athlete_id " +
                ") as sub2 using (athlete_id)";
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) -> new Object[] {
            rs.getInt("athlete_id"),
            rs.getString("json_course_ids"),
            rs.getString("json_first_runs")
        });
    }

    public static class MostEventsRecord
    {
        public final String name;
        public final int athleteId;
        public final int differentRegionCourseCount;
        public final int totalRegionRuns;
        public final int differentCourseCount;
        public final int totalRuns;
        public int positionDelta = 0;

        public MostEventsRecord(String name,
                                    int athleteId,
                                    int differentRegionCourseCount,
                                    int totalRegionRuns,
                                    int differentCourseCount,
                                    int totalRuns)
        {
            this.name = name;
            this.athleteId = athleteId;
            this.differentRegionCourseCount = differentRegionCourseCount;
            this.totalRegionRuns = totalRegionRuns;
            this.differentCourseCount = differentCourseCount;
            this.totalRuns = totalRuns;
        }

        @Override
        public String toString()
        {
            return "MostEventsRecord{" +
                    "name='" + name + '\'' +
                    ", athleteId=" + athleteId +
                    ", differentRegionCourseCount=" + differentRegionCourseCount +
                    ", totalRegionRuns=" + totalRegionRuns +
                    ", differentCourseCount=" + differentCourseCount +
                    ", totalRuns=" + totalRuns +
                    ", positionDelta=" + positionDelta +
                    '}';
        }
    }
}
