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
            "Max Attendance", "This Weeks Attendance" ,
            "Date");


    public AttendanceRecordsTableHtmlWriter(XMLStreamWriter writer) throws XMLStreamException
    {
        super(writer);

        startElement("details");
        startElement("summary");
        writer.writeCharacters("Attendance Records (New Zealand)");
        endElement("summary");

        startElement("table");

        writeRecord(true, HEADER);
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
        String trType = isHeader ? "thead" : "tr";
        String tdType = isHeader ? "th" : "td";

        writer.writeStartElement(trType);

        // Name
        startElement(tdType);
        if (isHeader)
        {
            writer.writeCharacters(record.courseLongName);
        }
        else
        {
            writer.writeStartElement("a");
            writer.writeAttribute("href", UrlGenerator.generateCourseUrl("parkrun.co.nz", record.courseName).toString());
            writer.writeAttribute("target", String.valueOf(record.courseName));
            writer.writeCharacters(record.courseLongName);
            endElement("a");
        }
        endElement(tdType);

        // This weeks attendance
        startElement(tdType);
        writer.writeCharacters(record.recentAttendance);
        endElement(tdType);

        // Max attendance
        startElement(tdType);
        writer.writeCharacters(record.maxAttendance);
        endElement(tdType);

        // Date
        startElement(tdType);
        writer.writeCharacters(record.date);
        endElement(tdType);

        endElement(trType);
    }
}
