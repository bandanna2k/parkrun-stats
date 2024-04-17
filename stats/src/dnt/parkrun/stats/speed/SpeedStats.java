package dnt.parkrun.stats.speed;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.DateConverter;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.AgeCategory;
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
import java.util.*;

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
        Map<Integer, Map<AgeCategory, AgeCategoryRecord>> courseToAgeGroupToAgeGradeRecord = new HashMap<>();
        Map<AgeCategory, Map<Integer, AgeCategoryRecord>> ageGroupToCourseAgeGradeRecord = new HashMap<>();
        resultDao.tableScanResultAndEventNumber((result, eventNumber) -> {
            {
                Map<AgeCategory, AgeCategoryRecord> ageGroupToAgeGradeRecord = courseToAgeGroupToAgeGradeRecord
                        .computeIfAbsent(result.courseId, courseId -> new HashMap<>());
                AgeCategoryRecord ageCategoryRecord = ageGroupToAgeGradeRecord.computeIfAbsent(result.ageCategory, ageGroup -> new AgeCategoryRecord());
                ageCategoryRecord.maybeAddByTime(new StatsRecord().result(result).eventNumber(eventNumber));
            }
            {
                Map<Integer, AgeCategoryRecord> courseToAgeGroupRecord = ageGroupToCourseAgeGradeRecord
                        .computeIfAbsent(result.ageCategory, ageGroup -> new HashMap<>());
                AgeCategoryRecord ageCategoryRecord = courseToAgeGroupRecord.computeIfAbsent(result.courseId, courseId -> new AgeCategoryRecord());
                ageCategoryRecord.maybeAddByTime(new StatsRecord().result(result).eventNumber(eventNumber));
            }
        });

        try (HtmlWriter writer = HtmlWriter.newInstance(date, "speed_stats", "speed_stats.css"))
        {
            try(CollapsableTitleHtmlWriter collapse1 = new CollapsableTitleHtmlWriter(
                    writer.writer, "Age Category Records "))
            {
                List<Course> sortedCourses = new ArrayList<>();
                courseToAgeGroupToAgeGradeRecord.keySet().forEach(courseId -> {
                    sortedCourses.add(courseRepository.getCourse(courseId));
                });
                sortedCourses.sort(Comparator.comparing(s -> s.name));

                for (Course course : sortedCourses)
                {
                    Map<AgeCategory, AgeCategoryRecord> ageGroupToAgeGroupRecord = courseToAgeGroupToAgeGradeRecord.get(course.courseId);

                    try(CollapsableTitleHtmlWriter collapse2 = new CollapsableTitleHtmlWriter(
                            writer.writer, course.longName, 2, 95.0))
                    {
                        try (AgeCategoryRecordsHtmlWriter ageGroupRecordsWriter = new AgeCategoryRecordsHtmlWriter(writer.writer, urlGenerator))
                        {
                            for (AgeCategory ageCategory : AgeCategory.values())
                            {
                                AgeCategoryRecord ageCategoryRecord = ageGroupToAgeGroupRecord.get(ageCategory);
                                if (ageCategoryRecord == null) continue;

                                writeAgeGroupRecord(ageGroupRecordsWriter, ageCategoryRecord.recordGold, course);
//                                writeAgeGroupRecord(ageGroupRecordsWriter, ageGroupRecord.resultSilver);
//                                writeAgeGroupRecord(ageGroupRecordsWriter, ageGroupRecord.resultBronze);
                            }
                        }
                    }
                }

                for (Map.Entry<Integer, Map<AgeCategory, AgeCategoryRecord>> entry : courseToAgeGroupToAgeGradeRecord.entrySet())
                {
                }
            }
            return writer.getFile();
        }
    }

    private void writeAgeGroupRecord(AgeCategoryRecordsHtmlWriter writer, StatsRecord record, Course course) throws XMLStreamException
    {
        if(record.result().date == null) return; // TODO
        if(record.result().ageCategory == AgeCategory.UNKNOWN) return;

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
