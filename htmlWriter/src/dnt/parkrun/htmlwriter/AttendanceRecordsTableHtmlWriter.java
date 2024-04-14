package dnt.parkrun.htmlwriter;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.datastructures.stats.AttendanceRecord;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

public class AttendanceRecordsTableHtmlWriter extends BaseWriter implements Closeable
{
    private final UrlGenerator urlGenerator;

    public AttendanceRecordsTableHtmlWriter(XMLStreamWriter writer, UrlGenerator urlGenerator) throws XMLStreamException
    {
        super(writer);
        this.urlGenerator = urlGenerator;

        startDetails();
        startElement("summary");
        writer.writeCharacters("Attendance Records");
        endElement("summary");

        writer.writeStartElement("table");
        writer.writeAttribute("class", "sortable name-data");

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

        // Up arrows for max attendance
        startElement("th");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Record Event Finishers");
        endElement("th");

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

            endDetails();
        }
        catch (XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void writeAttendanceRecord(AttendanceRecord record) throws XMLStreamException
    {
        writeRecord(record);
    }

    private void writeRecord(AttendanceRecord record) throws XMLStreamException
    {
        writer.writeStartElement("tr");

        // Course name
        startElement("td");
        writer.writeStartElement("a");
        writer.writeAttribute("href", urlGenerator.generateCourseEventSummaryUrl(record.courseName).toString());
        writer.writeAttribute("target", String.valueOf(record.courseName));
        writer.writeCharacters(record.courseLongName);
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
        startElement("a", "target", record.courseName,
                "href", urlGenerator.generateCourseEventUrl(record.courseName, record.recentEventNumber).toString());
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
        startElement("a", "target", record.courseName, "href",
                urlGenerator.generateCourseEventUrl(record.courseName, record.recordEventNumber).toString());
        writer.writeCharacters(DateConverter.formatDateForHtml(record.recordEventDate));
        endElement("a");
        endElement("td");

        // Max delta
        startElement("td");
        if(record.maxAttendanceDelta > 0)
        {
            startElement("font", "color", "green");
            startElement("abbr", "title", "+" + record.maxAttendanceDelta);
            writer.writeCharacters("▲");
            endElement("abbr");
            endElement("font");
        }
        else if(record.maxAttendanceDelta < 0)
        {
            // Probably won't happen, but keeps me honest
            startElement("font", "color", "red");
            startElement("abbr", "title", String.valueOf(record.maxAttendanceDelta));
            writer.writeCharacters("▼");
            endElement("abbr");
            endElement("font");
        }
        endElement("td");

        // Max attendance
        startElement("td");
        writer.writeCharacters(String.valueOf(record.recordEventFinishers));
        endElement("td");

        endElement("tr");
    }
}
