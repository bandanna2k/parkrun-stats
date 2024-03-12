package dnt.parkrun.stats;

import dnt.parkrun.datastructures.stats.RunsAtEvent;
import dnt.parkrun.htmlwriter.BaseWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

import static dnt.parkrun.common.UrlGenerator.generateAthleteEventSummaryUrl;
import static dnt.parkrun.common.UrlGenerator.generateCourseUrl;
import static dnt.parkrun.stats.Stats.PARKRUN_CO_NZ;

public class MostRunsAtEventTableWriter extends BaseWriter implements Closeable
{
    public MostRunsAtEventTableWriter(XMLStreamWriter writer) throws XMLStreamException
    {
        super(writer);

        startElement("details");
        startElement("summary");
        writer.writeCharacters("Most Runs at Course (New Zealand)");
        endElement("summary");

        startElement("table", "class", "sortable name-name-data");
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
        writer.writeCharacters("Name");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Run Count");
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
            endElement("details");

        }
        catch (XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void writeRecord(RunsAtEvent record) throws XMLStreamException
    {
        writer.writeStartElement("tr");

        // Course
        startElement("td");
        writer.writeStartElement("a");
        writer.writeAttribute("href", generateCourseUrl(PARKRUN_CO_NZ, record.courseName).toString());
        writer.writeAttribute("target", record.courseName);
        writer.writeCharacters(record.courseLongName);
        endElement("a");
        endElement("td");

        // Name
        startElement("td");
        writer.writeStartElement("a");
        writer.writeAttribute("href", generateAthleteEventSummaryUrl(PARKRUN_CO_NZ, record.athleteId).toString());
        writer.writeAttribute("target", record.name);
        writer.writeCharacters(record.name);
        endElement("a");
        endElement("td");

        // P-Index
        startElement("td");
        writer.writeCharacters(String.valueOf(record.maxRunCount));
        endElement("td");

        endElement("tr");
    }
}