package dnt.parkrun.htmlwriter;

import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.datastructures.Athlete;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;
import java.util.Comparator;

public class Top10InRegionHtmlWriter extends BaseWriter implements Closeable
{
    private final UrlGenerator urlGenerator;
    private final String isRun;
    private boolean showPercentageColumn = false;

    public Top10InRegionHtmlWriter(XMLStreamWriter writer,
                                   UrlGenerator urlGenerator,
                                   String courseLongName,
                                   String isRun) throws XMLStreamException
    {
        this(writer, urlGenerator, courseLongName, isRun, false);
    }
    public Top10InRegionHtmlWriter(XMLStreamWriter writer,
                                   UrlGenerator urlGenerator,
                                   String courseLongName,
                                   String isRun,
                                   boolean showPercentageColumn) throws XMLStreamException
    {
        super(writer);
        this.urlGenerator = urlGenerator;
        this.isRun = isRun;
        this.showPercentageColumn = showPercentageColumn;
        startSubDetails();
        startElement("summary", "style", "font-size:16px;");
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

        // %
        if(showPercentageColumn)
        {
            startElement("td", "class", "dt");
            writer.writeCharacters(String.format("%.1f", record.percentage));
            endElement("td");
        }
        endElement("tr");
    }

    public static class Record
    {
        public static Comparator<Record> COMPARATOR = (r1, r2) ->
        {
            if (r1.percentage > r2.percentage) return -1;
            if (r1.percentage < r2.percentage) return 1;
            if (r1.runCount > r2.runCount) return -1;
            if (r1.runCount < r2.runCount) return 1;
            if (r1.athlete.athleteId > r2.athlete.athleteId) return 1;
            if (r1.athlete.athleteId < r2.athlete.athleteId) return -1;
            return 0;
        };

        public final Athlete athlete;
        public final String courseLongName;
        public final int runCount;
        public final double percentage;

        public Record(Athlete athlete, String courseLongName, int runCount, double percentage)
        {
            this.athlete = athlete;
            this.courseLongName = courseLongName;
            this.runCount = runCount;
            this.percentage = percentage;
        }

        @Override
        public String toString()
        {
            return "Record{" +
                    "athlete=" + athlete +
                    ", courseLongName='" + courseLongName + '\'' +
                    ", runCount=" + runCount +
                    ", percentage=" + percentage +
                    '}';
        }
    }
}
