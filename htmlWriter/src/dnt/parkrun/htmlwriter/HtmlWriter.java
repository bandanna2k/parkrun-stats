package dnt.parkrun.htmlwriter;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.datastructures.Country;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class HtmlWriter extends BaseWriter
{
    private final Date date;
    private final File file;
    private final String cssFilename;
    private final Country country;

    public static HtmlWriter newInstance(Date date, Country country, String prefix, String cssFile) throws IOException, XMLStreamException
    {
        File file = new File(prefix + "_" + DateConverter.formatDateForDbTable(date) + ".html");

        XMLStreamWriter writer = XMLOutputFactory
                .newInstance()
                .createXMLStreamWriter(
                        new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
        return new HtmlWriter(writer, date, country, file, cssFile);
    }

    private HtmlWriter(XMLStreamWriter writer, Date date, Country country, File file, String cssFilename) throws XMLStreamException, IOException
    {
        super(writer);
        this.date = date;
        this.file = file;
        this.cssFilename = cssFilename;
        this.country = country;

        startHtml();
    }

    private void startHtml() throws XMLStreamException, IOException
    {
        writer.writeStartDocument();

        startElement("html");

        String title = country.countryName + " parkrun Most Events and Attendance Records for Events on and prior to " + DateConverter.formatDateForHtml(date);
        startElement("title");
        writer.writeCharacters(title);
        endElement("title");

        startElement("head");

        startElement("meta", "charset", "UTF-8");
        endElement("meta");

        startElement("meta", "name", "description", "content", title);
        endElement("meta");

        startElement("meta",
                "name", "keywords",
                "content", "Most Events parkrun Attendance Records pIndex Most Volunteer Volunteering 90% Club");
        endElement("meta");

        endElement("head");

        writer.writeStartElement("style\n");
        try(BufferedReader reader1 = new BufferedReader(new InputStreamReader(
                this.getClass().getResourceAsStream("/css/" + cssFilename))))
        {
            String line1;
            while(null != (line1 = reader1.readLine()))
            {
                writer.writeCharacters(line1 + "\n");
            }
        }
        endElement("style");

        startElement("script", "src", "https://www.kryogenix.org/code/browser/sorttable/sorttable.js");
        endElement("script");

        startElement("body");
    }

    @Override
    public void close() throws IOException
    {
        try
        {
            endElement("body");
            endElement("html");

            writer.writeEndDocument();
            writer.close();
        }
        catch (XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
    }

    public File getFile()
    {
        return file;
    }
}
