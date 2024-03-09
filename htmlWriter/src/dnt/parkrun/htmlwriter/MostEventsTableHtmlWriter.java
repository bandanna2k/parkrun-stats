package dnt.parkrun.htmlwriter;

import dnt.parkrun.datastructures.stats.MostEventsRecord;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

import static dnt.parkrun.common.UrlGenerator.generateAthleteEventSummaryUrl;

public class MostEventsTableHtmlWriter extends BaseWriter implements Closeable
{
    public MostEventsTableHtmlWriter(XMLStreamWriter writer) throws XMLStreamException
    {
        super(writer);

        startElement("details");
        startElement("summary");
        writer.writeCharacters("Most Events (New Zealand)");
        endElement("summary");

        startElement("table", "class", "sortable most-events");
        writeHeader(writer);
    }

    private void writeHeader(XMLStreamWriter writer) throws XMLStreamException
    {
        startElement("thead");
        startElement("tr");

        // Up arrows for max attendance
        startElement("th");
        endElement("th");

        // Name
        startElement("th");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Region Events");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Total Region Runs");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Worldwide Events");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Total Runs");
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

    public void writeMostEventRecord(MostEventsRecord record) throws XMLStreamException
    {
       writer.writeStartElement("tr");

        // Up/Down
        startElement("td");
        if(record.positionDelta > 0)
        {
            startElement("font", "color", "green");
            startElement("abbr", "title", "+" + record.positionDelta);
            writer.writeCharacters("▲");
            endElement("abbr");
            endElement("font");
        }
        else if(record.positionDelta < 0)
        {
            startElement("font", "color", "red");
            startElement("abbr", "title", String.valueOf(record.positionDelta));
            writer.writeCharacters("▼");
            endElement("abbr");
            endElement("font");
        }
        endElement("td");

        // Name
        startElement("td");
        writer.writeStartElement("a");
        writer.writeAttribute("href", generateAthleteEventSummaryUrl("parkrun.co.nz", record.athleteId).toString());
        writer.writeAttribute("target", String.valueOf(record.athleteId));
        writer.writeCharacters(record.name);
        endElement("a");
        endElement("td");

        // Different region courses
        startElement("td");
        writer.writeCharacters(String.valueOf(record.differentRegionCourseCount));
        endElement("td");

        // Total region runs
        startElement("td");
        writer.writeCharacters(String.valueOf(record.totalRegionRuns));
        endElement("td");

        // Different courses
        startElement("td");
        writer.writeCharacters(String.valueOf(record.differentCourseCount));
        endElement("td");

        // Total region runs
        startElement("td");
        writer.writeCharacters(String.valueOf(record.totalRuns));
        endElement("td");

        endElement("tr");
    }
}
