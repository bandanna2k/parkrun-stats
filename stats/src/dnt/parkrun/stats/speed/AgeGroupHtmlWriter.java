package dnt.parkrun.stats.speed;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.datastructures.AgeGroup;
import dnt.parkrun.htmlwriter.BaseWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

public class AgeGroupHtmlWriter extends BaseWriter implements Closeable
{
    private final UrlGenerator urlGenerator;
    private final AgeGroup ageGroup;

    public AgeGroupHtmlWriter(XMLStreamWriter writer, UrlGenerator urlGenerator, AgeGroup ageGroup) throws XMLStreamException
    {
        super(writer);
        this.urlGenerator = urlGenerator;
        this.ageGroup = ageGroup;

        startElement("h2");
        writer.writeCharacters(ageGroup.textOnWebpage);
        startElement("h2");

        startElement("table", "class", "sortable most-events");
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

        // Date
        startElement("th");
        writer.writeCharacters("Date");
        endElement("th");

        // Time
        startElement("th");
        writer.writeCharacters("Time");
        endElement("th");

        // Age grade (desktop)
        startElement("th", "class", "dt");
        writer.writeCharacters("Age Grade");
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

//            startElement("center");
//            startElement("p");
//            writer.writeCharacters("* For runners that have run at 20 or more locations.");
//            endElement("p");
//            startElement("p");
//            writer.writeCharacters("* Hover over position arrows to see weekly movement.");
//            endElement("p");
//            endElement("center");
        }
        catch (XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void write(StatsRecord record) throws XMLStreamException
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

        // Date
        startElement("td");
        writer.writeCharacters(DateConverter.formatDateForHtml(record.date()));
        endElement("td");

        // Time
        startElement("td");
        writer.writeCharacters(record.time().toHtmlString());
        endElement("td");

        // Age grade
        startElement("td");
        writer.writeCharacters(record.ageGrade().toHtmlString());
        endElement("td");

        endElement("tr");
    }
}
