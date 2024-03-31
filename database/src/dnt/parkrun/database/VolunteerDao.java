package dnt.parkrun.database;

import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Volunteer;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;
import java.util.List;

public class VolunteerDao extends BaseDao
{
    public static final int MIN_VOLUNTEER_COUNT = 20;

    public VolunteerDao(DataSource dataSource)
    {
        super(dataSource);
    }

    public void insert(Volunteer volunteer)
    {
        String sql = "insert into event_volunteer (" +
                "athlete_id, course_id, date" +
                ") values ( " +
                ":athleteId, :courseId, :date" +
                ")";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("athleteId", volunteer.athlete.athleteId)
                .addValue("courseId", volunteer.courseId)
                .addValue("date", volunteer.date)
        );
    }

    public static String getMostVolunteersSubQuery1(String volunteerTable)
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
        String sql = "select a.name, a.athlete_id, sub1.count as different_region_course_count, sub2.count as total_region_volunteers\n" +
                "from " + athleteTable() + " a\n" +
                "join  \n" +
                "(\n" +
                getMostVolunteersSubQuery1(volunteerTable()) +
                ") as sub1 on sub1.athlete_id = a.athlete_id\n" +
                "join\n" +
                "(\n" +
                getMostVolunteersSubQuery2(volunteerTable()) +
                ") as sub2 on sub2.athlete_id = a.athlete_id\n" +
                "where a.name is not null\n" +
                "order by different_region_course_count desc, total_region_volunteers desc, a.athlete_id desc;\n";
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) -> new Object[] {
                Athlete.from(rs.getString("name"), rs.getInt("athlete_id")),
                rs.getInt("different_region_course_count"),
                rs.getInt("total_region_volunteers")
        });
    }

    @Deprecated
    public // Testing only. Do not use. Results too large
    List<Volunteer> getVolunteers()
    {
        String sql = "select * from event_volunteer " +
                "right join athlete using (athlete_id)";
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
}
