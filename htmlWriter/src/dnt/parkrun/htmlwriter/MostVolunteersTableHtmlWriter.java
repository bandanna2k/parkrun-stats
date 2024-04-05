package dnt.parkrun.htmlwriter;

import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.datastructures.Athlete;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

public class MostVolunteersTableHtmlWriter extends BaseWriter implements Closeable
{
    private final UrlGenerator urlGenerator;

    public MostVolunteersTableHtmlWriter(HtmlWriter writer, UrlGenerator urlGenerator) throws XMLStreamException
    {
        super(writer.writer);
        this.urlGenerator = urlGenerator;

        startElement("table", "class", "sortable name-name-data");
        writeHeader(writer.writer);
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
        writer.writeCharacters("Name");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Region Events");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Total Region Volunteers");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Total Global Volunteers");
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
//            writer.writeCharacters("* Hover over position arrows to see weekly movement.");
//            endElement("p");
//            endElement("center");

        }
        catch (XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void writeRecord(Record record) throws XMLStreamException
    {
        writer.writeStartElement("tr");

        writeTableDataWithDelta(record.positionDelta, record.isNewEntry);

        // Name
        startElement("td");
        writer.writeStartElement("a");
        writer.writeAttribute("href", urlGenerator.generateAthleteEventSummaryUrl(record.athlete.athleteId).toString());
        writer.writeAttribute("target", String.valueOf(record.athlete.name));
        writer.writeCharacters(record.athlete.name);
        endElement("a");
        endElement("td");

        // Different region courses
        startElement("td");
        writer.writeCharacters(String.valueOf(record.differentRegionCourseCount));
        endElement("td");

        // Total region volunteers
        startElement("td");
        writer.writeCharacters(String.valueOf(record.totalRegionVolunteers));
        endElement("td");

        // Total global volunteers
        startElement("td");
        writer.writeCharacters(String.valueOf(record.totalGlobalVolunteers));
        endElement("td");

        endElement("tr");
    }

    public static class Record
    {
        public final Athlete athlete;
        public final int differentRegionCourseCount;
        public final int totalRegionVolunteers;
        public final int totalGlobalVolunteers;

        public int positionDelta;
        public boolean isNewEntry;

        public Record(Athlete athlete,
                      int differentRegionCourseCount,
                      int totalRegionVolunteers,
                      int totalGlobalVolunteers)
        {
            this.athlete = athlete;
            this.differentRegionCourseCount = differentRegionCourseCount;
            this.totalRegionVolunteers = totalRegionVolunteers;
            this.totalGlobalVolunteers = totalGlobalVolunteers;
        }

        @Override
        public String toString()
        {
            return "Record{" +
                    "athlete=" + athlete +
                    ", differentRegionCourseCount=" + differentRegionCourseCount +
                    ", totalRegionVolunteers=" + totalRegionVolunteers +
                    ", totalGlobalVolunteers=" + totalGlobalVolunteers +
                    ", positionDelta=" + positionDelta +
                    ", isNewEntry=" + isNewEntry +
                    '}';
        }
    }
}
