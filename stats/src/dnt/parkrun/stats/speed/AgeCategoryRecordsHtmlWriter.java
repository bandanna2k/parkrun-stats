package dnt.parkrun.stats.speed;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.htmlwriter.BaseWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

public class AgeCategoryRecordsHtmlWriter extends BaseWriter implements Closeable
{
    private final UrlGenerator urlGenerator;

    public AgeCategoryRecordsHtmlWriter(XMLStreamWriter writer, UrlGenerator urlGenerator) throws XMLStreamException
    {
        super(writer);
        this.urlGenerator = urlGenerator;

//        startElement("h2");
////        writer.writeCharacters(ageGroup.textOnWebpage);
//        startElement("h2");

        startElement("table", "class", "sortable bold1 left3");
        writeHeader(writer);
    }

    private void writeHeader(XMLStreamWriter writer) throws XMLStreamException
    {
        startElement("thead");
        startElement("tr");

        // Age group
        startElement("th");
        writer.writeCharacters("Age Group");
        endElement("th");

        // Date (desktop)
        startElement("th", "class", "dt");
        writer.writeCharacters("Date");
        endElement("th");

        // Name
        startElement("th");
        writer.writeCharacters("Name");
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

    public void write(StatsRecord statsRecord) throws XMLStreamException
    {
        Result result = statsRecord.result();

        writer.writeStartElement("tr");

        // Date
        startElement("td");
        writer.writeCharacters(result.ageCategory.textOnWebpage);
        endElement("td");

        // Date
        startElement("td", "class", "dt");
        startElement("a", "target", statsRecord.course().name + statsRecord.eventNumber(),
                "href", urlGenerator.generateCourseEventUrl(statsRecord.course().name, statsRecord.eventNumber()).toString());
        writer.writeCharacters(DateConverter.formatDateForHtml(result.date));
        endElement("a");
        endElement("td");


        // Name
        startElement("td");
        writer.writeStartElement("a");
        writer.writeAttribute("href", urlGenerator.generateAthleteEventSummaryUrl(result.athlete.athleteId).toString());
        writer.writeAttribute("target", String.valueOf(result.athlete.name));
        writer.writeCharacters(result.athlete.name);
        endElement("a");
        endElement("td");

        // Time
        startElement("td");
        writer.writeCharacters(result.time.toHtmlString());
        endElement("td");

        // Age grade (desktop)
        startElement("td", "class", "dt");
        writer.writeCharacters(result.ageGrade.toHtmlString());
        endElement("td");

        endElement("tr");
    }
}
