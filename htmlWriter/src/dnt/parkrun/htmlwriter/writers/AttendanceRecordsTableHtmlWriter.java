package dnt.parkrun.htmlwriter.writers;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.stats.AttendanceRecord;
import dnt.parkrun.htmlwriter.BaseWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

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
        writer.writeAttribute("class", "sortable bold1 left1 define2 define3 define6");

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
            startElement("th");
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

    public void writeAttendanceRecord(AttendanceRecord record, Course course) throws XMLStreamException
    {
        writeRecord(record, course);
    }

    private void writeRecord(AttendanceRecord record, Course course) throws XMLStreamException
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
        endElement("td");

        // Recent attendance (desktop only)
        startElement("td", "class", "dt");
        if(record.recentAttendanceDelta >= 0)
        {
            startElement("abbr", "title", "+" + record.recentAttendanceDelta);
            writer.writeCharacters(String.valueOf(record.recentEventFinishers));
            endElement("abbr");
            endElement("td");
        }
        else
        {
            startElement("abbr", "title", String.valueOf(record.recentAttendanceDelta));
            writer.writeCharacters(String.valueOf(record.recentEventFinishers));
            endElement("abbr");
            endElement("td");
        }

        // Record Date
        startElement("td");
        startElement("a", "target", course.name, "href",
                urlGenerator.generateCourseEventUrl(course.name, record.recordEventNumber).toString());
        writer.writeCharacters(DateConverter.formatDateForHtml(record.recordEventDate));
        endElement("a");
        endElement("td");

        // Max attendance
        startElement("td");
        writeDelta(record.maxAttendanceDelta, false);
        writer.writeCharacters(String.valueOf(record.recordEventFinishers));
        endElement("td");

        // Average
        if(extended)
        {
            startElement("td");
            writer.writeCharacters(record.average.toHtmlString());
            endElement("td");
        }
        endElement("tr");
    }
}
