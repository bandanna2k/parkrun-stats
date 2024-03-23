package dnt.parkrun.htmlwriter;

import dnt.parkrun.datastructures.Athlete;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

import static dnt.parkrun.common.UrlGenerator.generateAthleteEventSummaryUrl;

public class Top10AtCourseHtmlWriter extends BaseWriter implements Closeable
{
    private final String runOrVolunteer;

    public Top10AtCourseHtmlWriter(XMLStreamWriter writer, String title, String runOrVolunteer) throws XMLStreamException
    {
        super(writer);
        this.runOrVolunteer = runOrVolunteer;
        startElement("details", "style", "margin-left:10em");
        startElement("summary", "style", "font-size:24px");
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

            endElement("details");
        }
        catch (XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void writeRecord(Record record) throws XMLStreamException
    {
       writer.writeStartElement("tr");

        // Name
        startElement("td");
        writer.writeStartElement("a");
        writer.writeAttribute("href", generateAthleteEventSummaryUrl("parkrun.co.nz", record.athlete.athleteId).toString());
        writer.writeAttribute("target", String.valueOf(record.athlete.name));
        writer.writeCharacters(record.athlete.name);
        endElement("a");
        endElement("td");

        // Count
        startElement("td");
        writer.writeCharacters(String.valueOf(record.runCount));
        endElement("td");

        // Percentage
        startElement("td");
        writer.writeCharacters(record.percentage);
        endElement("td");

        endElement("tr");
    }

    public static class Record
    {
        public final Athlete athlete;
        public final int runCount;
        public final String percentage;

        public Record(Athlete athlete, int runCount, String percentage)
        {
            this.athlete = athlete;
            this.runCount = runCount;
            this.percentage = percentage;
        }

        @Override
        public String toString()
        {
            return "Record{" +
                    "athlete=" + athlete +
                    ", runCount=" + runCount +
                    ", percentage='" + percentage + '\'' +
                    '}';
        }
    }
}
