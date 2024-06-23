package dnt.parkrun.stats.invariants.postdownload;

import dnt.parkrun.athletecoursesummary.Parser;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.database.AthleteDao;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.stats.invariants.AbstractDatabaseInvariantTest;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static dnt.parkrun.database.BaseDaoTest.TEST_DATABASE;
import static dnt.parkrun.datastructures.Country.NZ;

public class DatabaseInvariantTest extends AbstractDatabaseInvariantTest
{
    private final UrlGenerator urlGenerator = new UrlGenerator(NZ.baseUrl);

    @Test
    public void allEventVolunteersExist()
    {
        assertNoRowsMatch("""
                select athlete_id, course_id, date 
                from parkrun_stats_NZ.event_volunteer
                where athlete_id <= 0
                limit 10
                """,
                "athlete_id", "course_id", "date");
    }

    @Test
    public void allCourseEventSummariesShouldHaveOnly1DatePerEventNumber()
    {
        assertNoRowsMatch("""
            select course_id, event_number, count(date) as count
            from parkrun_stats_NZ.course_event_summary
            group by course_id, event_number
            having count > 1
            limit 10
            """,
            "course_id", "event_number", "count");
    }

    @Test
    public void allCourseEventSummariesShouldHaveOnly1EventNumberPerDate()
    {
        assertNoRowsMatch("""
                select course_id, date, count(event_number) as count
                from parkrun_stats_NZ.course_event_summary
                group by course_id, date
                having count > 1
                limit 10
                """,
                "course_id", "date", "count");
    }
    @Test
    public void courseEventSummaryWithoutVolunteers()
    {
        assertNoRowsMatch("""
                select distinct ces.course_id, ces.date, ev.athlete_id
                from parkrun_stats_NZ.course_event_summary ces
                left join parkrun_stats_NZ.event_volunteer ev using (course_id)
                where ev.athlete_id is null
                limit 10
                """,
                "course_id", "date", "athlete_id");
    }

    /*
    On failure, run 'RewriteEvent'
     */
    @Test
    public void courseEventSummaryFinishersShouldMatchResultCount()
    {
        System.setProperty("TEST", "false");

        CourseRepository courseRepository = new CourseRepository();
        new CourseDao(TEST_DATABASE, courseRepository);

        String sql = """
                select ces.course_id, ces.date, ces.finishers, count(r.athlete_id) as result_count
                from parkrun_stats_NZ.course_event_summary ces
                left join parkrun_stats_NZ.result r on
                    ces.course_id = r.course_id and
                    ces.date = r.date
                group by ces.course_id, ces.date, ces.finishers
                having
                    ces.finishers <> result_count
                limit 10
                """;
        List<Object[]> query = jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            return new Object[]{
                    rs.getInt("course_id"),
                    rs.getDate("date"),
                    rs.getInt("result_count")
            };
        });

        SoftAssertions softly = new SoftAssertions();
        if(!query.isEmpty())
        {
            query.forEach(fields -> {
                int courseId = (int) fields[0];

                Date date = (Date) fields[1];
                int resultCount = (int) fields[2];

                Integer eventNumber = jdbc.queryForObject(
                        "select event_number from parkrun_stats_NZ.course_event_summary where course_id = :courseId and date = :date",
                        new MapSqlParameterSource()
                                .addValue("courseId", courseId)
                                .addValue("date", date),
                        Integer.class);

                softly.fail(String.format("Course: %s, Date %s, Event number %s, Result Count: %s", courseId, date, eventNumber, resultCount));
            });
            softly.assertAll();
        }
    }

    @Test
    public void allVolunteersShouldHaveAnAthlete()
    {
        assertNoRowsMatch("""
                select distinct athlete_id, course_id, name
                from parkrun_stats_NZ.event_volunteer
                left join parkrun_stats.athlete using (athlete_id)
                where name is null
                limit 10
                """,
                "athlete_id", "course_id", "name");
    }

    private void addMissingVolunteer(List<Object[]> volunteerWithNoAthleteRecord)
    {
        AthleteDao athleteDao = new AthleteDao(TEST_DATABASE);
        volunteerWithNoAthleteRecord.stream().map(object ->
        {
            int nonameAthleteId = (int) object[0];
            Parser parser = new Parser.Builder()
                    .webpageProvider(new WebpageProviderImpl(urlGenerator.generateAthleteEventSummaryUrl(nonameAthleteId)))
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
