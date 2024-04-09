package dnt.parkrun.htmlwriter;

import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.datastructures.Athlete;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

public class Top10InRegionHtmlWriter extends BaseWriter implements Closeable
{
    private final UrlGenerator urlGenerator;

    public Top10InRegionHtmlWriter(XMLStreamWriter writer, UrlGenerator urlGenerator, String courseLongName) throws XMLStreamException
    {
        super(writer);
        this.urlGenerator = urlGenerator;
        startSubDetails();
        startElement("summary", "style", "font-size:20px");
        writer.writeCharacters(courseLongName);
        endElement("summary");

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
        writer.writeCharacters("Course Run Count");
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

    public void writeRecord(Record record) throws XMLStreamException
    {
       writer.writeStartElement("tr");

        // Name
        startElement("td");
        writer.writeStartElement("a");
        writer.writeAttribute("href", urlGenerator.generateAthleteEventSummaryUrl(record.athlete.athleteId).toString());
        writer.writeAttribute("target", String.valueOf(record.athlete.name));
        writer.writeCharacters(record.athlete.name);
        endElement("a");
        endElement("td");

        // Course
        startElement("td");
        writer.writeCharacters(record.courseLongName);
        endElement("td");

        // Count
        startElement("td");
        writer.writeCharacters(String.valueOf(record.runCount));
        endElement("td");

        endElement("tr");
    }

    public static class Record
    {
        public final Athlete athlete;
        public final String courseLongName;
        public final int runCount;

        public Record(Athlete athlete, String courseLongName, int runCount)
        {
            this.athlete = athlete;
            this.courseLongName = courseLongName;
            this.runCount = runCount;
        }

        @Override
        public String toString()
        {
            return "Record{" +
                    "athlete=" + athlete +
                    ", courseLongName='" + courseLongName + '\'' +
                    ", runCount=" + runCount +
                    '}';
        }
    }
}
