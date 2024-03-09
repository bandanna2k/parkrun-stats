package dnt.parkrun.htmlwriter;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.datastructures.stats.AttendanceRecord;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

public class AttendanceRecordsTableHtmlWriter extends BaseWriter implements Closeable
{
    public AttendanceRecordsTableHtmlWriter(XMLStreamWriter writer) throws XMLStreamException
    {
        super(writer);

        startElement("details");
        startElement("summary");
        writer.writeCharacters("Attendance Records (New Zealand)");
        endElement("summary");

        writer.writeStartElement("table");
        writer.writeAttribute("class", "sortable attendance");

        writeHeader(writer);
    }

    private void writeHeader(XMLStreamWriter writer) throws XMLStreamException
    {
        startElement("thead");
        startElement("tr");

        startElement("th");
        writer.writeCharacters("Course");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Last Event Date");
        endElement("th");

        startElement("th");
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
            endElement("details");
            endElement("table");
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
        writer.writeAttribute("href", UrlGenerator.generateCourseEventSummaryUrl("parkrun.co.nz", record.courseName).toString());
        writer.writeAttribute("target", String.valueOf(record.courseName));
        writer.writeCharacters(record.courseLongName);
        endElement("a");
        endElement("td");

        // Recent date
        startElement("td");
        writer.writeCharacters(DateConverter.formatDateForHtml(record.recentDate));
        endElement("td");

        // Recent attendance
        startElement("td");
        if(record.recentAttendanceDelta >= 0)
        {
            startElement("abbr", "title", "+" + record.recentAttendanceDelta);
            writer.writeCharacters(String.valueOf(record.recentAttendance));
            endElement("abbr");
            endElement("td");
        }
        else
        {
            startElement("abbr", "title", String.valueOf(record.recentAttendanceDelta));
            writer.writeCharacters(String.valueOf(record.recentAttendance));
            endElement("abbr");
            endElement("td");
        }

        // Date
        startElement("td");
        writer.writeCharacters(DateConverter.formatDateForHtml(record.maxDate));
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
        writer.writeCharacters(String.valueOf(record.maxAttendance));
        endElement("td");

        endElement("tr");
    }
}
