package dnt.parkrun.stats.speed;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.AgeCategory;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.htmlwriter.HtmlWriter;
import dnt.parkrun.htmlwriter.StatsRecord;
import dnt.parkrun.htmlwriter.writers.CollapsableTitleHtmlWriter;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;
import static dnt.parkrun.stats.speed.AgeCategoryRecordsHtmlWriter.Type.AGE_CATEGORY_BY_TIME;
import static dnt.parkrun.stats.speed.AgeCategoryRecordsHtmlWriter.Type.AGE_GRADE;

public class SpeedStats
{
    private final CourseRepository courseRepository;

    /*
            02/03/2024
     */
    public static void main() throws SQLException, IOException, XMLStreamException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                getDataSourceUrl("parkrun_stats"), "stats", "statsfractalstats");

        SpeedStats stats = SpeedStats.newInstance(dataSource);
        Map<Integer, Map<AgeCategory, AgeCategoryRecord>> courseToAgeGroupToAgeGradeRecord =
                stats.collectCourseToAgeGroupToAgeGradeRecord();
        {
            File file = stats.generateFastTimeStats(courseToAgeGroupToAgeGradeRecord);
            new ProcessBuilder("xdg-open", file.getAbsolutePath()).start();
        }
    }

    private Date mostRecentDate = new Date(Long.MIN_VALUE);
    private final ResultDao resultDao;

    private final UrlGenerator urlGenerator = new UrlGenerator(NZ.baseUrl);

    private SpeedStats(DataSource dataSource)
    {
        this.resultDao = new ResultDao(dataSource);
        this.courseRepository = new CourseRepository();
        new CourseDao(dataSource, courseRepository);
    }

    public static SpeedStats newInstance(DataSource dataSource)
    {
        return new SpeedStats(dataSource);
    }

    public Map<Integer, Map<AgeCategory, AgeCategoryRecord>> collectCourseToAgeGroupToAgeGradeRecord()
    {
        Map<Integer, Map<AgeCategory, AgeCategoryRecord>> courseToAgeGroupToAgeGradeRecord = new HashMap<>();
        resultDao.tableScanResultAndEventNumber((result, eventNumber) -> {
            //if(result.courseId == 40 && result.ageCategory == AgeCategory.SW30_34)
            //if(result.courseId == 2)
            {
                Map<AgeCategory, AgeCategoryRecord> ageGroupToAgeGradeRecord = courseToAgeGroupToAgeGradeRecord
                        .computeIfAbsent(result.courseId, courseId -> new HashMap<>());
                AgeCategoryRecord ageCategoryRecord = ageGroupToAgeGradeRecord.computeIfAbsent(result.ageCategory, ageGroup -> new AgeCategoryRecord());
                ageCategoryRecord.maybeAddByAgeGrade(new StatsRecord().result(result).eventNumber(eventNumber));
            }
            if(result.date.getTime() > mostRecentDate.getTime())
                mostRecentDate = new Date(result.date.getTime());
        });
        System.out.println(mostRecentDate);
        return courseToAgeGroupToAgeGradeRecord;
    }

    public File generateFastTimeStats(Map<Integer, Map<AgeCategory, AgeCategoryRecord>> courseToAgeGroupToAgeGradeRecord) throws IOException, XMLStreamException
    {
        try (HtmlWriter writer = HtmlWriter.newInstance(mostRecentDate, "stats_for_speed", "speed_stats.css"))
        {
            writer.writer.writeStartElement("p");
            writer.writer.writeAttribute("align", "right");
            writer.writer.writeCharacters(new SimpleDateFormat("yyyy MMM dd HH:mm").format(new Date()));
            writer.writer.writeEndElement();

            writeAgeCategoryRecords(writer, courseToAgeGroupToAgeGradeRecord);

            writer.writer.writeStartElement("hr");
            writer.writer.writeEndElement();

            writeAgeGradeRecords(writer, courseToAgeGroupToAgeGradeRecord);
            return writer.getFile();
        }
    }

    private void writeAgeCategoryRecords(HtmlWriter writer,
                                         Map<Integer, Map<AgeCategory, AgeCategoryRecord>> courseToAgeGroupToAgeGradeRecord) throws XMLStreamException
    {
        try(CollapsableTitleHtmlWriter collapse1 = new CollapsableTitleHtmlWriter.Builder(
                writer.writer, "Age Category Records (Time)").build())
        {
            List<Course> sortedCourses = new ArrayList<>();
            courseToAgeGroupToAgeGradeRecord.keySet().forEach(courseId -> {
                sortedCourses.add(courseRepository.getCourse(courseId));
            });
            sortedCourses.sort(Comparator.comparing(s -> s.name));

            for (Course course : sortedCourses)
            {
                Map<AgeCategory, AgeCategoryRecord> ageGroupToAgeGroupRecord = courseToAgeGroupToAgeGradeRecord.get(course.courseId);

                try(CollapsableTitleHtmlWriter collapse2 = new CollapsableTitleHtmlWriter.Builder(
                        writer.writer, course.longName).level(2).fontSizePercent(95.0).build())
                {
                    try (AgeCategoryRecordsHtmlWriter ageGroupRecordsWriter = new AgeCategoryRecordsHtmlWriter(
                            writer.writer, urlGenerator, AGE_CATEGORY_BY_TIME))
                    {
                        for (AgeCategory ageCategory : AgeCategory.values())
                        {
                            AgeCategoryRecord ageCategoryRecord = ageGroupToAgeGroupRecord.get(ageCategory);
                            if (ageCategoryRecord == null) continue;

                            writeAgeGroupRecord(ageGroupRecordsWriter, ageCategoryRecord.records[0], course);
//                                writeAgeGroupRecord(ageGroupRecordsWriter, ageGroupRecord.resultSilver);
//                                writeAgeGroupRecord(ageGroupRecordsWriter, ageGroupRecord.resultBronze);
                        }
                    }
                }
            }
        }
    }

    private void writeAgeGradeRecords(HtmlWriter writer,
                                      Map<Integer, Map<AgeCategory, AgeCategoryRecord>> courseToAgeGroupToAgeGradeRecord) throws XMLStreamException
    {
        try(CollapsableTitleHtmlWriter collapse1 = new CollapsableTitleHtmlWriter.Builder(
                writer.writer, "Age Grade Records").open().build())
        {
            List<Course> sortedCourses = new ArrayList<>();
            courseToAgeGroupToAgeGradeRecord.keySet().forEach(courseId -> {
                sortedCourses.add(courseRepository.getCourse(courseId));
            });
            sortedCourses.sort(Comparator.comparing(s -> s.name));

            for (Course course : sortedCourses)
            {
                Map<AgeCategory, AgeCategoryRecord> ageGroupToAgeGroupRecord = courseToAgeGroupToAgeGradeRecord.get(course.courseId);

                try(CollapsableTitleHtmlWriter collapse2 = new CollapsableTitleHtmlWriter.Builder(
                        writer.writer, course.longName).level(2).fontSizePercent(95.0).build())
                {
                    for (AgeCategory ageCategory : AgeCategory.values())
                    {
                        AgeCategoryRecord ageCategoryRecord = ageGroupToAgeGroupRecord.get(ageCategory);
                        if (ageCategoryRecord == null) continue;

                        List<StatsRecord> records = new ArrayList<>();
                        for (StatsRecord record : ageCategoryRecord.records)
                        {
                            if(record.result().date == null) continue; // TODO: Need a better way of telling that a result is null.
                            if(record.result().ageCategory == AgeCategory.UNKNOWN) continue;

                            records.add(record);
                        }
                        if(records.isEmpty()) continue;

                        try (CollapsableTitleHtmlWriter collapse3 = new CollapsableTitleHtmlWriter.Builder(
                                writer.writer, ageCategory.textOnWebpage).level(3).fontSizePercent(95.0).build())
                        {
                            writer.writer.writeStartElement("h3");
                            writer.writer.writeCharacters(course.longName);
                            writer.writer.writeEndElement();

                            try (AgeCategoryRecordsHtmlWriter ageGroupRecordsWriter = new AgeCategoryRecordsHtmlWriter(writer.writer, urlGenerator, AGE_GRADE))
                            {
                                for (StatsRecord record : ageCategoryRecord.records)
                                {
                                    writeAgeGroupRecord(ageGroupRecordsWriter, record, course);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void writeAgeGroupRecord(AgeCategoryRecordsHtmlWriter writer, StatsRecord record, Course course) throws XMLStreamException
    {
        if(record.result().date == null) return; // TODO: Need a better way of telling that a result is null.
        if(record.result().ageCategory == AgeCategory.UNKNOWN) return;

        record.course(course);
        record.isRecent(isRecent(record.result().date));
        record.isNew(isNew(record.result().date));

        writer.write(record);
    }

    private boolean isNew(Date date)
    {
        Instant isNew = mostRecentDate.toInstant().minus(6, ChronoUnit.DAYS);
        return date.after(Date.from(isNew));
    }

    private boolean isRecent(Date date)
    {
        Instant isRecent = mostRecentDate.toInstant().minus(35, ChronoUnit.DAYS);
        return date.after(Date.from(isRecent));
    }
}
