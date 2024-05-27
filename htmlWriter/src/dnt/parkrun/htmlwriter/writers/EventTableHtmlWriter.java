package dnt.parkrun.htmlwriter.writers;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.htmlwriter.BaseWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;
import java.util.Date;

public class EventTableHtmlWriter extends BaseWriter implements Closeable
{
    private final UrlGenerator urlGenerator;

    public EventTableHtmlWriter(XMLStreamWriter writer, UrlGenerator urlGenerator) throws XMLStreamException
    {
        super(writer);
        this.urlGenerator = urlGenerator;

        writer.writeStartElement("table");
//        writeHeader();
    }

    @Override
    public void close()
    {
        try
        {
            endElement("table");
        }
        catch (XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void writeRecord(Course course, Date date, int eventNumberX) throws XMLStreamException
    {
        writer.writeStartElement("tr");

        // Course
        startElement("td");
        writer.writeStartElement("a");
        writer.writeAttribute("href", urlGenerator.generateCourseUrl(course.name).toString());
        writer.writeAttribute("target", course.name);
        writer.writeCharacters(course.longName);
        endElement("a");
        endElement("td");

        // Date
        startElement("td");
//        writer.writeStartElement("a");
//        writer.writeAttribute("href", urlGenerator.generateCourseEventUrl(course.name, eventNumber).toString());
//        writer.writeAttribute("target", course.name + eventNumber);
        writer.writeCharacters(DateConverter.formatDateForHtml(date));
//        endElement("a");
        endElement("td");

        // Event number
//        startElement("td");
//        writer.writeStartElement("a");
//        writer.writeAttribute("href", urlGenerator.generateCourseEventUrl(course.name, eventNumber).toString());
//        writer.writeAttribute("target", course.name + eventNumber);
//        writer.writeCharacters(String.valueOf(eventNumber));
//        endElement("a");
//        endElement("td");

        endElement("tr");
    }

    public void writeHeader() throws XMLStreamException
    {
        writer.writeStartElement("thead");

        // Course
        startElement("th");
        writer.writeCharacters("Course");
        endElement("th");

        // Date
        startElement("th");
        writer.writeCharacters("Date");
        endElement("th");

        // Event number
//        startElement("th");
//        writer.writeCharacters("Event Number");
//        endElement("th");

        endElement("thead");
    }
}
