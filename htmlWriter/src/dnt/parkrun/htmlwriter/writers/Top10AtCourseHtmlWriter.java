package dnt.parkrun.htmlwriter.writers;

import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.htmlwriter.BaseWriter;
import dnt.parkrun.htmlwriter.StatsRecord;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

public class Top10AtCourseHtmlWriter extends BaseWriter implements Closeable
{
    private final UrlGenerator urlGenerator;
    private final String runOrVolunteer;

    public Top10AtCourseHtmlWriter(XMLStreamWriter writer,
                                   UrlGenerator urlGenerator,
                                   String title,
                                   String runOrVolunteer) throws XMLStreamException
    {
        super(writer);
        this.urlGenerator = urlGenerator;
        this.runOrVolunteer = runOrVolunteer;
        startSubDetails();
        startElement("summary", "style", "font-size:16px;");
        writer.writeCharacters(title);
        endElement("summary");

        startElement("table", "class", "sortable name-data");
        writeHeader(writer);
    }

    private void writeHeader(XMLStreamWriter writer) throws XMLStreamException
    {
        startElement("thead");
        startElement("tr");

        // Name
        startElement("th");
        writer.writeCharacters("Name");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Course " + runOrVolunteer + " Count");
        endElement("th");

        startElement("th");
        writer.writeCharacters("% of Total Events");
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

            endSubDetails();
        }
        catch (XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void writeRecord(StatsRecord record) throws XMLStreamException
    {
       writer.writeStartElement("tr");

        // Name
        startElement("td");
        writer.writeStartElement("a");
        writer.writeAttribute("href", urlGenerator.generateAthleteEventSummaryUrl(record.athlete().athleteId).toString());
        writer.writeAttribute("target", String.valueOf(record.athlete().name));
        writer.writeCharacters(record.athlete().name);
        endElement("a");
        endElement("td");

        // Count
        startElement("td");
        writer.writeCharacters(String.valueOf(record.count()));
        endElement("td");

        // Percentage
        startElement("td");
        writer.writeCharacters(String.format("%.1f", record.percentage()));
        endElement("td");

        endElement("tr");
    }
}
