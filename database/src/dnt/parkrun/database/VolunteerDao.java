package dnt.parkrun.database;

import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Volunteer;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

public class VolunteerDao
{
    private final NamedParameterJdbcOperations jdbc;

    public VolunteerDao(DataSource dataSource)
    {
        jdbc = new NamedParameterJdbcTemplate(dataSource);
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

    @Deprecated
    public // Testing only. Do not use. Results too large
    List<Volunteer> getVolunteers()
    {
        String sql = "select * from event_volunteer " +
                "right join athlete using (athlete_id)";
        List<Volunteer> query = jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            return new Volunteer(
                    rs.getInt("course_id"),
                    rs.getDate("date"),
                    Athlete.from(
                            rs.getString("name"),
                            rs.getInt("athlete_id")
                    )
            );
        });
        return query;
    }
}
