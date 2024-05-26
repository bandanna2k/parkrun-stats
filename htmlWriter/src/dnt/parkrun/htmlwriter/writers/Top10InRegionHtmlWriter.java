package dnt.parkrun.htmlwriter.writers;

import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.htmlwriter.BaseWriter;
import dnt.parkrun.htmlwriter.StatsRecord;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

public class Top10InRegionHtmlWriter extends BaseWriter implements Closeable
{
    private final UrlGenerator urlGenerator;
    private final String isRun;
    private boolean showPercentageColumn = false;

    public Top10InRegionHtmlWriter(XMLStreamWriter writer,
                                   UrlGenerator urlGenerator,
                                   String isRun) throws XMLStreamException
    {
        this(writer, urlGenerator, isRun, false);
    }
    public Top10InRegionHtmlWriter(XMLStreamWriter writer,
                                   UrlGenerator urlGenerator,
                                   String isRun,
                                   boolean showPercentageColumn) throws XMLStreamException
    {
        super(writer);
        this.urlGenerator = urlGenerator;
        this.isRun = isRun;
        this.showPercentageColumn = showPercentageColumn;

        startElement("table", "class", "sortable name-name-data");
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
        writer.writeCharacters("Course");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Course " + isRun + " Count");
        endElement("th");

        if(showPercentageColumn)
        {
            startElement("th", "class", "dt");
            writer.writeCharacters("% of Events");
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

        // Course
        startElement("td");
        writer.writeCharacters(record.course().longName);
        endElement("td");

        // Count
        startElement("td");
        writer.writeCharacters(String.valueOf(record.count()));
        endElement("td");

        // %
        if(showPercentageColumn)
        {
            startElement("td", "class", "dt");
            writer.writeCharacters(String.format("%.1f", record.percentage()));
            endElement("td");
        }
        endElement("tr");
    }
}
