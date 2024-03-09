package dnt.parkrun.htmlwriter;

import dnt.parkrun.datastructures.stats.MostEventsRecord;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

import static dnt.parkrun.common.UrlGenerator.generateAthleteEventSummaryUrl;
import static dnt.parkrun.datastructures.Athlete.NO_ATHLETE_ID;

public class MostEventsTableHtmlWriter extends BaseWriter implements Closeable
{
    private static final MostEventsRecord HEADER = new MostEventsRecord("Name",
            NO_ATHLETE_ID,
            "Region Events",
            "Total Region Runs",
            "Worldwide Events",
            "Total Runs",
            0);


    public MostEventsTableHtmlWriter(XMLStreamWriter writer) throws XMLStreamException
    {
        super(writer);

        startElement("details");
        startElement("summary");
        writer.writeCharacters("Most Events (New Zealand)");
        endElement("summary");

        startElement("table", "class", "sortable most-events");

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

    public void writeMostEventRecord(MostEventsRecord record) throws XMLStreamException
    {
        writeRecord(false, record);
    }

    private void writeRecord(boolean isHeader, MostEventsRecord record) throws XMLStreamException
    {
        String trType = isHeader ? "thead" : "tr";
        String tdType = isHeader ? "th" : "td";

        writer.writeStartElement(trType);

        // Up/Down
        startElement(tdType);
        if(record.positionDelta > 0)
        {
            writer.writeStartElement("font");
            writer.writeAttribute("color", "green");
            writer.writeCharacters("▲");
            writer.writeEndElement();
        }
        else if(record.positionDelta < 0)
        {
            writer.writeStartElement("font");
            writer.writeAttribute("color", "red");
            writer.writeCharacters("▼");
            writer.writeEndElement();
        }
        endElement(tdType);

        // Name
        startElement(tdType);
        if (isHeader)
        {
            writer.writeCharacters(record.name);
        }
        else
        {
//            writer.writeCharacters(record.positionDelta);
//            writer.writeCharacters(" ");
            writer.writeStartElement("a");
            writer.writeAttribute("href", generateAthleteEventSummaryUrl("parkrun.co.nz", record.athleteId).toString());
            writer.writeAttribute("target", String.valueOf(record.athleteId));
            writer.writeCharacters(record.name);
            endElement("a");
        }
        endElement(tdType);

        // Different region courses
        startElement(tdType);
        writer.writeCharacters(record.differentRegionCourseCount);
        endElement(tdType);

        // Total region runs
        startElement(tdType);
        writer.writeCharacters(record.totalRegionRuns);
        endElement(tdType);

        // Different courses
        startElement(tdType);
        writer.writeCharacters(record.differentCourseCount);
        endElement(tdType);

        // Total region runs
        startElement(tdType);
        writer.writeCharacters(record.totalRuns);
        endElement(tdType);

        endElement(trType);
    }
}
