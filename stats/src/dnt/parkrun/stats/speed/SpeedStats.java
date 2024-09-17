package dnt.parkrun.stats.speed;

import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.Database;
import dnt.parkrun.database.LiveDatabase;
import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.AgeCategory;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.htmlwriter.HtmlWriter;
import dnt.parkrun.htmlwriter.StatsRecord;
import dnt.parkrun.htmlwriter.writers.CollapsableTitleHtmlWriter;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;

import static dnt.parkrun.common.FindAndReplace.findAndReplace;
import static dnt.parkrun.common.FindAndReplace.getTextFromFile;
import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;
import static dnt.parkrun.stats.speed.AgeCategoryRecordsHtmlWriter.Type.AGE_CATEGORY_BY_TIME;
import static dnt.parkrun.stats.speed.AgeCategoryRecordsHtmlWriter.Type.AGE_GRADE;

public class SpeedStats
{
    private static final Country COUNTRY = NZ;

    private final CourseRepository courseRepository;

    /*
            02/03/2024
     */
    public static void main(String... args) throws IOException, XMLStreamException
    {
//        Country country = Country.valueOf(args[0]);
        Database database = new LiveDatabase(NZ, getDataSourceUrl(), "stats", "4b0e7ff1");
        SpeedStats stats = SpeedStats.newInstance(database);

        Map<Integer, Map<AgeCategory, AgeCategoryRecord>> courseToAgeGroupToAgeGradeRecord = new HashMap<>();
        Map<Integer, Map<AgeCategory, AgeCategoryRecord>> courseToAgeGroupToTimeRecord = new HashMap<>();
        stats.collectCourseToAgeGroupStats(courseToAgeGroupToAgeGradeRecord, courseToAgeGroupToTimeRecord);

        {
            File file = stats.generateFastTimeStats(courseToAgeGroupToAgeGradeRecord, courseToAgeGroupToTimeRecord);
            File modified = new File(file.getAbsoluteFile().getParent() + "/modified_" + file.getName());

            findAndReplace(file, modified, fileReplacements());

            new ProcessBuilder("xdg-open", modified.getAbsolutePath()).start();
        }
    }

    public static Object[][] fileReplacements()
    {
        return new Object[][]{
            {"Cornwall parkrun", (Supplier<String>) () -> "Cornwall Park parkrun"},
            {"{{css}}", (Supplier<String>) () ->
                    "<style>" +
                            getTextFromFile(SpeedStats.class.getResourceAsStream("/css/speed_stats.css")) +
                            "</style>"
            },
            {"{{meta}}", (Supplier<String>) () ->
                    getTextFromFile(SpeedStats.class.getResourceAsStream("/meta_speed_stats.xml"))
            },
        };
    }

    private Date mostRecentDate = new Date(Long.MIN_VALUE);
    private final ResultDao resultDao;

    private final UrlGenerator urlGenerator = new UrlGenerator(COUNTRY.baseUrl);

    private SpeedStats(Database database)
    {
        this.resultDao = new ResultDao(database);
        this.courseRepository = new CourseRepository();
        new CourseDao(database, courseRepository);
    }

    public static SpeedStats newInstance(Database database)
    {
        return new SpeedStats(database);
    }

    public void collectCourseToAgeGroupStats(
            Map<Integer, Map<AgeCategory, AgeCategoryRecord>> courseToAgeGroupToAgeGradeRecord,
            Map<Integer, Map<AgeCategory, AgeCategoryRecord>> courseToAgeGroupToTimeRecord)
    {
        resultDao.tableScan((result) -> {
            {
                Map<AgeCategory, AgeCategoryRecord> ageGroupToAgeGradeRecord = courseToAgeGroupToAgeGradeRecord
                        .computeIfAbsent(result.courseId, courseId -> new HashMap<>());
                AgeCategoryRecord ageCategoryRecord = ageGroupToAgeGradeRecord.computeIfAbsent(result.ageCategory, ageGroup -> new AgeCategoryRecord());
                ageCategoryRecord.maybeAddByAgeGrade(new StatsRecord().result(result).eventNumber(result.eventNumber));
            }
            {
                Map<AgeCategory, AgeCategoryRecord> ageGroupToTimeRecord = courseToAgeGroupToTimeRecord
                        .computeIfAbsent(result.courseId, courseId -> new HashMap<>());
                AgeCategoryRecord ageCategoryRecord = ageGroupToTimeRecord.computeIfAbsent(result.ageCategory, ageGroup -> new AgeCategoryRecord());
                ageCategoryRecord.maybeAddByTime(new StatsRecord().result(result).eventNumber(result.eventNumber));
            }
            if(result.date.getTime() > mostRecentDate.getTime())
                mostRecentDate = new Date(result.date.getTime());
        });
        System.out.println(mostRecentDate);
    }

    public File generateFastTimeStats(
            Map<Integer, Map<AgeCategory, AgeCategoryRecord>> courseToAgeGroupToAgeGradeRecord,
            Map<Integer, Map<AgeCategory, AgeCategoryRecord>> courseToAgeGroupToTimeRecord) throws IOException, XMLStreamException
    {
        try (HtmlWriter writer = HtmlWriter.newInstance(mostRecentDate, COUNTRY, "stats_for_speed"))
        {
            writer.writer.writeCharacters("{{css}}");

            writer.writer.writeStartElement("p");
            writer.writer.writeAttribute("align", "right");
            writer.writer.writeCharacters(new SimpleDateFormat("yyyy MMM dd HH:mm").format(new Date()));
            writer.writer.writeEndElement();

            writeAgeCategoryTimeRecords(writer, courseToAgeGroupToTimeRecord);

            writer.writer.writeStartElement("hr");
            writer.writer.writeEndElement();

            writeAgeGradeRecords(writer, courseToAgeGroupToAgeGradeRecord);
            return writer.getFile();
        }
    }

    private void writeAgeCategoryTimeRecords(HtmlWriter writer,
                                             Map<Integer, Map<AgeCategory, AgeCategoryRecord>> courseToAgeGroupToTimeRecord) throws XMLStreamException
    {
        try(CollapsableTitleHtmlWriter collapse1 = new CollapsableTitleHtmlWriter.Builder(
                writer.writer, "Age Category Records (Time)").build())
        {
            List<Course> sortedCourses = new ArrayList<>();
            courseToAgeGroupToTimeRecord.keySet().forEach(courseId -> {
                sortedCourses.add(courseRepository.getCourse(courseId));
            });
            sortedCourses.sort(Comparator.comparing(s -> s.name));

            for (Course course : sortedCourses)
            {
                Map<AgeCategory, AgeCategoryRecord> ageGroupToAgeGroupRecord = courseToAgeGroupToTimeRecord.get(course.courseId);

                try(CollapsableTitleHtmlWriter collapse2 = new CollapsableTitleHtmlWriter.Builder(
//                        writer.writer, YEAR + " - " + course.longName).level(2).fontSizePercent(95.0).build())
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
                                      Map<Integer, Map<AgeCategory, AgeCategoryRecord>> courseToAgeGroupToAgeGradeRecord)
            throws XMLStreamException, IOException
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
//                try(BaseWriter subHtml = writer.createSubPageWriter(course.name + ".html",
//                        "parkrun_stats", course.country.name(), "speed_stats", "age_grade_records"))
//                {
//                    subHtml.writer.writeStartElement("html");
//                    subHtml.writer.writeStartElement("body");
//                    subHtml.writer.writeAttribute("style", "margin:0;padding:0");
//
//                    subHtml.writer.writeStartElement("font");
//                    subHtml.writer.writeAttribute("color", "red");
//                    subHtml.writer.writeCharacters(course.longName);
//                    subHtml.writer.writeEndElement();
//
//                    subHtml.writer.writeEndElement();
//                    subHtml.writer.writeEndElement();
//                }
//
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
                            if (record.result().date == null)
                                continue; // TODO: Need a better way of telling that a result is null.
                            if (record.result().ageCategory == AgeCategory.UNKNOWN) continue;

                            records.add(record);
                        }
                        if (records.isEmpty()) continue;

                        try(HtmlWriter htmlWriter = HtmlWriter.newInstance(mostRecentDate, course.country, ageCategory.name(), course.name + ".html");
                            AgeCategoryRecordsHtmlWriter ageGroupRecordsWriter2 =
                                    new AgeCategoryRecordsHtmlWriter(htmlWriter.writer, urlGenerator, AGE_GRADE))
                        {
                            try (CollapsableTitleHtmlWriter collapse3 = new CollapsableTitleHtmlWriter.Builder(
                                    writer.writer, ageCategory.textOnWebpage).level(3).fontSizePercent(95.0).build())
                            {
                                writer.writer.writeStartElement("h3");
                                //                            writer.writer.writeCharacters(YEAR + " - " + course.longName);
                                writer.writer.writeCharacters(course.longName);
                                writer.writer.writeEndElement();

                                try (AgeCategoryRecordsHtmlWriter ageGroupRecordsWriter = new AgeCategoryRecordsHtmlWriter(writer.writer, urlGenerator, AGE_GRADE))
                                {
                                    for (StatsRecord record : ageCategoryRecord.records)
                                    {
                                        writeAgeGroupRecord(ageGroupRecordsWriter, record, course);
                                        writeAgeGroupRecord(ageGroupRecordsWriter2, record, course);
                                    }
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
