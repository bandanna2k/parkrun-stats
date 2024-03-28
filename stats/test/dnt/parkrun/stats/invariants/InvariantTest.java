package dnt.parkrun.stats.invariants;

import com.mysql.jdbc.Driver;
import dnt.parkrun.athletecoursesummary.Parser;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.database.AthleteDao;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.CourseRepository;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class InvariantTest
{
    private DataSource dataSource;
    private NamedParameterJdbcTemplate jdbc;

    @Before
    public void setUp() throws Exception
    {
        dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");
        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    @Test
    public void eventsNotInTheDatabase()
    {

    }

    @Test
    public void courseEventSummaryWithoutVolunteers()
    {
        String sql = "select distinct ces.course_id, ces.date, ev.athlete_id \n" +
                "from course_event_summary ces\n" +
                "left join event_volunteer ev using (course_id)\n" +
                "where ev.athlete_id is null;\n";
        List<Object[]> query = jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            return new Object[]{
                    rs.getInt("course_id"),
                    rs.getDate("date"),
                    rs.getInt("athlete_id")
            };
        });
        Assertions.assertThat(query.size()).isEqualTo(0);
    }

    @Test
    public void courseEventSummaryFinishersShouldMatchResultCount()
    {
        String sql = "select ces.course_id, ces.date, ces.finishers, count(r.athlete_id) as result_count\n" +
                "from course_event_summary ces\n" +
                "left join result r on \n" +
                "    ces.course_id = r.course_id and\n" +
                "    ces.date = r.date\n" +
                "group by ces.course_id, ces.date, ces.finishers\n" +
                "having \n" +
                "    ces.finishers <> result_count\n" +
                "limit 10;\n";
        List<Object[]> query = jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            return new Object[]{
                    rs.getInt("course_id"),
                    rs.getInt("date"),
                    rs.getString("result_count")
            };
        });
        Assertions.assertThat(query.size()).isEqualTo(0);
    }

    @Test
    public void allVolunteersShouldHaveAnAthlete() throws SQLException
    {
        String sql = "select distinct athlete_id, course_id, name \n" +
                "from event_volunteer\n" +
                "left join athlete using (athlete_id)\n" +
                "where name is null;\n";
        List<Object[]> volunteerWithNoAthleteRecord = jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
                new Object[]{
                        rs.getInt("athlete_id"),
                        rs.getInt("course_id"),
                        rs.getString("name")
                });
        //addMissingVolunteer(volunteerWithNoAthleteRecord);
        Assertions.assertThat(volunteerWithNoAthleteRecord.size()).isEqualTo(0);
    }

    private void addMissingVolunteer(List<Object[]> volunteerWithNoAthleteRecord) throws SQLException
    {
        AthleteDao athleteDao = new AthleteDao(dataSource);
        volunteerWithNoAthleteRecord.stream().map(object ->
        {
            int nonameAthleteId = (int) object[0];
            Parser parser = new Parser.Builder()
                    .url(UrlGenerator.generateAthleteEventSummaryUrl(Country.NZ.baseUrl, nonameAthleteId))
                    .build(new CourseRepository());
            try
            {
                parser.parse();
            }
            catch(Exception ex)
            {
                System.out.println("ERROR " + ex.getMessage());
            }
            Athlete athlete = parser.getAthlete();
            athleteDao.insert(athlete);
            return String.format("insert into athlete (athlete_id, name) values (%d, '');", nonameAthleteId);
        }).collect(Collectors.toList());
    }
}
