package dnt.parkrun.htmlwriter;

import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.datastructures.stats.AttendanceRecord;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

public class AttendanceRecordsTableHtmlWriter extends BaseWriter implements Closeable
{
    private static final AttendanceRecord HEADER = new AttendanceRecord("Course",
            null,
            "Last Event Date", "Last Event Finishers",
            "Record Event Date", "Record Event Finishers");


    public AttendanceRecordsTableHtmlWriter(XMLStreamWriter writer) throws XMLStreamException
    {
        super(writer);

        startElement("details");
        startElement("summary");
        writer.writeCharacters("Attendance Records (New Zealand)");
        endElement("summary");

        writer.writeStartElement("table");
        writer.writeAttribute("class", "sortable");

        startElement("thead");
        writeRecord(true, HEADER);
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
        writeRecord(false, record);
    }

    private void writeRecord(boolean isHeader, AttendanceRecord record) throws XMLStreamException
    {
        String tdType = isHeader ? "th" : "td";

        writer.writeStartElement("tr");

        // Course name
        startElement(tdType);
        if (isHeader)
        {
            writer.writeCharacters(record.courseLongName);
        }
        else
        {
            writer.writeStartElement("a");
            writer.writeAttribute("href", UrlGenerator.generateCourseEventSummaryUrl("parkrun.co.nz", record.courseName).toString());
            writer.writeAttribute("target", String.valueOf(record.courseName));
            writer.writeCharacters(record.courseLongName);
            endElement("a");
        }
        endElement(tdType);

        // Recent date
        startElement(tdType);
        writer.writeCharacters(record.recentDate);
        endElement(tdType);

        // Recent delta
        startElement(tdType);
        if(record.recentAttendanceDelta > 0)
        {
            startElement("font", "color", "green");
            writer.writeCharacters("▲");
            endElement("font");
        }
        else if(record.recentAttendanceDelta < 0)
        {
            startElement("font", "color", "red");
            writer.writeCharacters("▼");
            endElement("font");
        }
        endElement(tdType);

        // Recent attendance
        startElement(tdType);
        writer.writeCharacters(record.recentAttendance);
        endElement(tdType);

        // Date
        startElement(tdType);
        writer.writeCharacters(record.maxDate);
        endElement(tdType);

        // Max attendance
        startElement(tdType);
        writer.writeCharacters(record.maxAttendance);
        endElement(tdType);

        endElement("tr");
    }
}
