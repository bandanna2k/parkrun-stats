package dnt.parkrun.htmlwriter.writers;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.Time;
import dnt.parkrun.datastructures.stats.AttendanceRecord;
import dnt.parkrun.datastructures.stats.EventDateCount;
import dnt.parkrun.htmlwriter.BaseWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;
import java.util.List;

public class AttendanceRecordsTableHtmlWriter extends BaseWriter implements Closeable
{
    private final UrlGenerator urlGenerator;
    private final boolean extended;

    public AttendanceRecordsTableHtmlWriter(XMLStreamWriter writer, UrlGenerator urlGenerator, boolean extended)
            throws XMLStreamException
    {
        super(writer);
        this.urlGenerator = urlGenerator;
        this.extended = extended;

        writer.writeStartElement("table");
        writer.writeAttribute("class", "sortable bold1 left1 hl2 hl3 hl6 hl7 right6 left7");

        writeHeader(writer);
    }

    private void writeHeader(XMLStreamWriter writer) throws XMLStreamException
    {
        startElement("thead");
        startElement("tr");

        startElement("th");
        writer.writeCharacters("Course");
        endElement("th");

        startElement("th", "class", "dt");
        writer.writeCharacters("Last Event Date");
        endElement("th");

        startElement("th", "class", "dt");
        writer.writeCharacters("Last Event Finishers");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Record Event Date");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Record Event Finishers");
        endElement("th");

        if(extended)
        {
            startElement("th", "colSpan", "2");
            writer.writeCharacters("Avg. Attendance / Last 10 Avg.");
            endElement("th");

            startElement("th", "class", "dt");
            writer.writeCharacters("Average Time");
            endElement("th");
        }

        endElement("tr");
        endElement("thead");
    }

    @Override
    public void close()
    {
        try
        {
            endElement("table");

            startElement("center");
            startElement("p");
            writer.writeCharacters("* Hover over last event finisher to see weekly movement.");
            endElement("p");
            startElement("p");
            writer.writeCharacters("* Hover over record attendance arrows to see difference since last record.");
            endElement("p");
            endElement("center");
        }
        catch (XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void writeAttendanceRecord(AttendanceRecord record,
                                      Course course,
                                      double averageAttendance,
                                      double recentAverageAttendance,
                                      Time averageTime,
                                      List<EventDateCount> maxAttendancesX,
                                      EventDateCount lastAttendanceX) throws XMLStreamException
    {
        writer.writeStartElement("tr");

        // Course name
        startElement("td");
        writer.writeStartElement("a");
        writer.writeAttribute("href", urlGenerator.generateCourseEventSummaryUrl(course.name).toString());
        writer.writeAttribute("target", course.name);
        writer.writeCharacters(course.longName);
        if(record.courseSmallTest != null)
        {
            startElement("div", "class", "small-text");
            writer.writeCharacters(record.courseSmallTest);
            endElement("div");
        }
        endElement("a");
        endElement("td");

        // Recent date (desktop only)
        startElement("td", "class", "dt");
        startElement("a", "target", course.name,
                "href", urlGenerator.generateCourseEventUrl(course.name, record.recentEventNumber).toString());
        writer.writeCharacters(DateConverter.formatDateForHtml(record.recentEventDate));
        endElement("a");
//        writer.writeCharacters(DateConverter.formatDateForHtml(lastAttendance.date));
        endElement("td");

        // Recent attendance (desktop only)
        startElement("td", "class", "dt");
        if(record.recentAttendanceDelta >= 0)
        {
            startElement("abbr", "title", "+" + record.recentAttendanceDelta);
            writer.writeCharacters(String.valueOf(record.recentEventFinishers));
            endElement("abbr");
        }
        else
        {
            startElement("abbr", "title", String.valueOf(record.recentAttendanceDelta));
            writer.writeCharacters(String.valueOf(record.recentEventFinishers));
            endElement("abbr");
        }
        endElement("td");
//        startElement("td", "class", "dt");
//        writer.writeCharacters(String.valueOf(lastAttendance.count));
//        endElement("td");


        // Max Date
        startElement("td");
        startElement("a", "target", course.name, "href",
                urlGenerator.generateCourseEventUrl(course.name, record.recordEventNumber).toString());
        writer.writeCharacters(DateConverter.formatDateForHtml(record.recordEventDate));
        endElement("a");
        endElement("td");

        // Max attendance count
        startElement("td");
        startElement("p");
        writeDelta(record.maxAttendanceDelta, false);
        writer.writeCharacters(String.valueOf(record.recordEventFinishers));
        endElement("p");
        endElement("td");

        // Max attendance date
//        startElement("td");
//        for (EventDateCount maxAttendance : maxAttendances)
//        {
//            startElement("p");
////            startElement("a", "target", course.name, "href",
////                    urlGenerator.generateCourseEventUrl(course.name, maxAttendance.eventNumber).toString());
//            writer.writeCharacters(DateConverter.formatDateForHtml(maxAttendance.date));
////            endElement("a");
//            endElement("p");
//        }
//        endElement("td");

        // Max attendance count
//        startElement("td");
//        startElement("p");
//        writeDelta(record.maxAttendanceDelta, false);
//        writer.writeCharacters(String.valueOf(maxAttendances.getFirst().count));
//        endElement("p");
//        endElement("td");

        // Avg attendance
        if(extended)
        {
            startElement("td");
            writer.writeCharacters(format0dp(averageAttendance));
            endElement("td");
            startElement("td");
            writer.writeCharacters("(" + format0dp(recentAverageAttendance) + ")");
            endElement("td");

            // Avg time (desktop only)
            startElement("td", "class", "dt");
            writer.writeCharacters(averageTime.toHtmlString());
            endElement("td");
        }
        endElement("tr");
    }

    private static String format1dp(double value)
    {
        return String.format("%.1f", value);
    }

    private static String format0dp(double value)
    {
        return String.format("%.0f", value);
    }
}
