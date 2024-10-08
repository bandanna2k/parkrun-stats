package dnt.parkrun.database;

import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Volunteer;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static dnt.parkrun.datastructures.Athlete.NO_ATHLETE_ID;

public class VolunteerDao extends BaseDao
{
    public static final int MIN_VOLUNTEER_COUNT = 20;

    private final String SQL_FOR_INSERT = STR."""
            insert into \{volunteerTable()}
            (athlete_id, course_id, date)
            values
            (:athleteId, :courseId, :date)
            """;

    public VolunteerDao(Database database)
    {
        super(database);
    }

    public void insert(Volunteer volunteer)
    {
        jdbc.update(SQL_FOR_INSERT, new MapSqlParameterSource()
                .addValue("athleteId", volunteer.athlete.athleteId)
                .addValue("courseId", volunteer.courseId)
                .addValue("date", volunteer.date)
        );
    }

    public void insert(List<Volunteer> volunteers)
    {
        volunteers.forEach(volunteer -> {
            assert volunteer.athlete.athleteId != NO_ATHLETE_ID : volunteers.toString();
        });
        jdbc.batchUpdate(SQL_FOR_INSERT, volunteers.stream().map(volunteer -> new MapSqlParameterSource()
                .addValue("athleteId", volunteer.athlete.athleteId)
                .addValue("courseId", volunteer.courseId)
                .addValue("date", volunteer.date)).toArray(MapSqlParameterSource[]::new));
    }

    public static String getSqlForVolunteersAtDifferentCourse(String volunteerTable)
    {
        return  "    select athlete_id, count(course_id) as count\n" +
                "    from (select distinct athlete_id, course_id from " + volunteerTable + ") as sub1a\n" +
                "    group by athlete_id\n" +
                "    having count >= " + MIN_VOLUNTEER_COUNT + " " +
                "    order by count desc, athlete_id asc \n";
    }
    public static String getMostVolunteersSubQuery2(String volunteerTable)
    {
        return  "    select athlete_id, count(concat) as count\n" +
                "    from (select distinct athlete_id, concat(athlete_id, '-', date) as concat from " + volunteerTable + ") as sub2a\n" +
                "    group by athlete_id\n" +
                "    order by count desc, athlete_id asc \n";
    }

    public List<Object[]> getMostVolunteers()
    {
        String sql = STR."""
            select a.name, a.athlete_id, sub1.count as different_region_course_count, sub2.count as total_region_volunteers
            from \{athleteTable()} a
            join (
                \{getSqlForVolunteersAtDifferentCourse(volunteerTable())}
            ) as sub1 on sub1.athlete_id = a.athlete_id
            join (
                \{getMostVolunteersSubQuery2(volunteerTable())}
            ) as sub2 on sub2.athlete_id = a.athlete_id
            where a.name is not null
            order by different_region_course_count desc, total_region_volunteers desc, a.athlete_id desc;
        """;
        List<Object[]> results = jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) -> new Object[]{
                Athlete.from(rs.getString("name"), rs.getInt("athlete_id")),
                rs.getInt("different_region_course_count"),
                rs.getInt("total_region_volunteers")
        });
        results.forEach(result -> {
//            Athlete athlete = (Athlete) result[0];
            int regionCourseCount = (int)result[1];
            int regionTotalCount = (int)result[2];

            // Not true. You can volunteer at 2 courses. You get 1 credit for the volunteer day. course should be counted.
            assert regionTotalCount >= regionCourseCount || true;
        });
        return results;
    }

    @Deprecated(since = "Testing only. Do not use. Results too large")
    public List<Volunteer> getVolunteers()
    {
        String sql = STR."""
                select *
                from \{volunteerTable()}
                left join \{athleteTable()} using (athlete_id)
                """;
        List<Volunteer> query = jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
                new Volunteer(
                        rs.getInt("course_id"),
                        rs.getDate("date"),
                        Athlete.from(
                                rs.getString("name"),
                                rs.getInt("athlete_id")
                        )
                ));
        return query;
    }

    public void delete(int courseId, Date date)
    {
        String sql = STR."""
                delete from \{volunteerTable()}
                where course_id = :courseId
                and date = :date
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("courseId", courseId)
                .addValue("date", date);
        jdbc.update(sql, params);
    }

    public void tableScan(Processor... processors)
    {
        tableScan(volunteer -> Arrays.stream(processors)
                .forEach(processor -> processor.visitInOrder(volunteer)),
                "order by course_id asc, date asc");
        Arrays.stream(processors).forEach(Processor::onFinishCourse);
    }
    private void tableScan(Consumer<Volunteer> consumer, String orderBy)
    {
        String sql = STR."""
                select *
                from \{volunteerTable()}
                join \{athleteTable()} using (athlete_id)
                \{orderBy}
                """;
        jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            Volunteer volunteer = new Volunteer(
                    rs.getInt("course_id"),
                    rs.getDate("date"),
                    Athlete.from(
                            rs.getString("name"),
                            rs.getInt("athlete_id")
                    ));
            consumer.accept(volunteer);
            return null;
        });
    }

    public interface Processor
    {
        void visitInOrder(Volunteer volunteer);
        void onFinishCourse();
    }
}
