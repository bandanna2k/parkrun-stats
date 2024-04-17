package dnt.parkrun.stats.speed;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.DateConverter;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.AgeGroup;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseRepository;
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

    public static SpeedStats newInstance(DataSource dataSource, Date date)
    {
        return new SpeedStats(dataSource, date);
    }

    public File generateStats() throws IOException, XMLStreamException
    {
        Map<Integer, Map<AgeGroup, AgeGroupRecord>> courseToAgeGroupToAgeGradeRecord = new HashMap<>();
        Map<AgeGroup, Map<Integer, AgeGroupRecord>> ageGroupToCourseAgeGradeRecord = new HashMap<>();
        resultDao.tableScanResultAndEventNumber((result, eventNumber) -> {
            {
                Map<AgeGroup, AgeGroupRecord> ageGroupToAgeGradeRecord = courseToAgeGroupToAgeGradeRecord
                        .computeIfAbsent(result.courseId, courseId -> new HashMap<>());
                AgeGroupRecord ageGroupRecord = ageGroupToAgeGradeRecord.computeIfAbsent(result.ageGroup, ageGroup -> new AgeGroupRecord());
                ageGroupRecord.maybeAddByTime(new StatsRecord().result(result).eventNumber(eventNumber));
            }
            {
                Map<Integer, AgeGroupRecord> courseToAgeGroupRecord = ageGroupToCourseAgeGradeRecord
                        .computeIfAbsent(result.ageGroup, ageGroup -> new HashMap<>());
                AgeGroupRecord ageGroupRecord = courseToAgeGroupRecord.computeIfAbsent(result.courseId, courseId -> new AgeGroupRecord());
                ageGroupRecord.maybeAddByTime(new StatsRecord().result(result).eventNumber(eventNumber));
            }
        });

        try (HtmlWriter writer = HtmlWriter.newInstance(date, "speed_stats", "speed_stats.css"))
        {
            try(CollapsableTitleHtmlWriter collapse1 = new CollapsableTitleHtmlWriter(
                    writer.writer, "Age Category Records "))
            {
                for (Map.Entry<Integer, Map<AgeGroup, AgeGroupRecord>> entry : courseToAgeGroupToAgeGradeRecord.entrySet())
                {
                    int courseId = entry.getKey();
                    Map<AgeGroup, AgeGroupRecord> ageGroupToAgeGroupRecord = entry.getValue();

                    Course course = courseRepository.getCourse(courseId);
                    if (course != null)
                    {
                        try(CollapsableTitleHtmlWriter collapse2 = new CollapsableTitleHtmlWriter(
                                writer.writer, course.longName, 2, 95.0))
                        {
                            try (AgeGroupRecordsHtmlWriter ageGroupRecordsWriter = new AgeGroupRecordsHtmlWriter(writer.writer, urlGenerator))
                            {
                                for (AgeGroup ageGroup : AgeGroup.values())
                                {
                                    AgeGroupRecord ageGroupRecord = ageGroupToAgeGroupRecord.get(ageGroup);
                                    if (ageGroupRecord == null) continue;

                                    writeAgeGroupRecord(ageGroupRecordsWriter, ageGroupRecord.recordGold, course);
//                                writeAgeGroupRecord(ageGroupRecordsWriter, ageGroupRecord.resultSilver);
//                                writeAgeGroupRecord(ageGroupRecordsWriter, ageGroupRecord.resultBronze);
                                }
                            }
                        }
                    }
                }
            }
            return writer.getFile();
        }
    }

    private void writeAgeGroupRecord(AgeGroupRecordsHtmlWriter writer, StatsRecord record, Course course) throws XMLStreamException
    {
        if(record.result().date == null) return; // TODO
        if(record.result().ageGroup == AgeGroup.UNKNOWN) return;

        record.course(course);

        writer.write(record);
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
