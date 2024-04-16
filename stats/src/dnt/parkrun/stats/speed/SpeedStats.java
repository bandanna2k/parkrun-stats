package dnt.parkrun.stats.speed;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.DateConverter;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.AgeGroup;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.htmlwriter.CollapsableTitleHtmlWriter;
import dnt.parkrun.htmlwriter.HtmlWriter;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static dnt.parkrun.common.DateConverter.SEVEN_DAYS_IN_MILLIS;
import static dnt.parkrun.datastructures.Country.NZ;

public class SpeedStats
{
    private final CourseRepository courseRepository;

    /*
            02/03/2024
     */
    public static void main(String[] args) throws SQLException, IOException, XMLStreamException
    {
        Date date = args.length == 0 ? getParkrunDay(new Date()) : DateConverter.parseWebsiteDate(args[0]);

        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "stats", "statsfractalstats");

        SpeedStats stats = SpeedStats.newInstance(dataSource, date);
        File file = stats.generateStats();

        new ProcessBuilder("xdg-open", file.getAbsolutePath()).start();
    }

    private final Date date;
    private final Date lastWeek;
    private final ResultDao resultDao;

    private final UrlGenerator urlGenerator = new UrlGenerator(NZ.baseUrl);

    private SpeedStats(DataSource dataSource,
                       Date date)
    {
        this.date = date;
        lastWeek = new Date();
        lastWeek.setTime(date.getTime() - SEVEN_DAYS_IN_MILLIS);

        this.resultDao = new ResultDao(dataSource);
        this.courseRepository = new CourseRepository();
        new CourseDao(dataSource, courseRepository);
    }

    public static SpeedStats newInstance(DataSource dataSource, Date date) throws SQLException
    {
        return new SpeedStats(dataSource, date);
    }

    public File generateStats() throws IOException, XMLStreamException
    {
        Map<Integer, Map<AgeGroup, AgeGroupRecord>> courseToAgeGroupToAgeGradeRecord = new HashMap<>();
        resultDao.tableScan(result -> {
            Map<AgeGroup, AgeGroupRecord> courseToAgeGradeRecord = courseToAgeGroupToAgeGradeRecord
                    .computeIfAbsent(result.courseId, courseId -> new HashMap<>());
            AgeGroupRecord ageGroupRecord = courseToAgeGradeRecord.computeIfAbsent(result.ageGroup, ageGroup -> new AgeGroupRecord());

            ageGroupRecord.maybeAdd(result);
        });

        try (HtmlWriter writer = HtmlWriter.newInstance(date, "speed_stats"))
        {
            try(CollapsableTitleHtmlWriter collapse1 = new CollapsableTitleHtmlWriter(writer.writer, "Age Group Records", 1))
            {
                for (Map.Entry<Integer, Map<AgeGroup, AgeGroupRecord>> entry : courseToAgeGroupToAgeGradeRecord.entrySet())
                {
                    int courseId = entry.getKey();
                    Map<AgeGroup, AgeGroupRecord> ageGroupToAgeGroupRecord = entry.getValue();
                    Course course = courseRepository.getCourse(courseId);
                    if (course != null)
                    {
                        try (CollapsableTitleHtmlWriter collapse2 = new CollapsableTitleHtmlWriter(writer.writer, course.longName, 2, 90.0))
                        {
                            for (Map.Entry<AgeGroup, AgeGroupRecord> e : ageGroupToAgeGroupRecord.entrySet())
                            {
                                AgeGroup ageGroup = e.getKey();
                                AgeGroupRecord ageGroupRecord = e.getValue();

                                try (AgeGroupHtmlWriter ageGroupWriter = new AgeGroupHtmlWriter(writer.writer, urlGenerator, ageGroup))
                                {
                                    writeAgeGroupRecord(ageGroupWriter, ageGroupRecord.resultGold);
                                    writeAgeGroupRecord(ageGroupWriter, ageGroupRecord.resultSilver);
                                    writeAgeGroupRecord(ageGroupWriter, ageGroupRecord.resultBronze);
                                }
                            }
                        }
                    }
                }
            }
            return writer.getFile();
        }
    }

    private void writeAgeGroupRecord(AgeGroupHtmlWriter writer, Result result) throws XMLStreamException
    {
        if(result.time == null) return; // TODO

        StatsRecord statsRecord = new StatsRecord()
                .athlete(result.athlete)
                .date(result.date)
                .time(result.time)
                .ageGrade(result.ageGrade);
        writer.write(statsRecord);
    }

    public static Date getParkrunDay(Date result)
    {
        Calendar calResult = Calendar.getInstance();
        calResult.setTime(result);

        for (int i = 0; i < 7; i++)
        {
            if (calResult.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
            {
                calResult.set(Calendar.HOUR_OF_DAY, 0);
                calResult.set(Calendar.MINUTE, 0);
                calResult.set(Calendar.SECOND, 0);
                calResult.set(Calendar.MILLISECOND, 0);
                return calResult.getTime();
            }
            calResult.add(Calendar.DAY_OF_MONTH, -1);
        }
        throw new UnsupportedOperationException();
    }
}
