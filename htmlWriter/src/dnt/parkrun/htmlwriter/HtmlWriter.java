package dnt.parkrun.htmlwriter;

import dnt.parkrun.common.DateConverter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class HtmlWriter extends BaseWriter
{
    private final FileOutputStream fos;
    private final Date date;

    public static HtmlWriter newInstance(Date date) throws IOException, XMLStreamException
    {
        File file = new File("stats_" + DateConverter.formatDateForDbTable(date) + ".html");
        FileOutputStream fos = new FileOutputStream(file);

        XMLStreamWriter writer = XMLOutputFactory
                .newInstance()
                .createXMLStreamWriter(
                        new OutputStreamWriter(fos, StandardCharsets.UTF_8));
        return new HtmlWriter(fos, writer, date);
    }

    private HtmlWriter(FileOutputStream fos, XMLStreamWriter writer, Date date) throws XMLStreamException, IOException
    {
        super(writer);
        this.fos = fos;
        this.date = date;

        startHtml();
    }

    private void startHtml() throws XMLStreamException, IOException
    {
        writer.writeStartDocument();

        startElement("html");

        startElement("title");
        writer.writeCharacters("New Zealand parkrun Statistics for Events on " + DateConverter.formatDateForHtml(date));
        endElement("title");

        writer.writeStartElement("style");
        try(BufferedReader reader1 = new BufferedReader(new InputStreamReader(
                this.getClass().getResourceAsStream("/css/most_events.css"))))
        {
            String line1;
            while(null != (line1 = reader1.readLine()))
            {
                writer.writeCharacters(line1 + "\n");
            }
        }
        endElement("style");

        writer.writeStartElement("script");
        writer.writeAttribute("src", "https://www.kryogenix.org/code/browser/sorttable/sorttable.js");
        endElement("script");

        startElement("body");
    }

    public void writeRawString(String input) throws IOException
    {
        fos.flush();
        fos.write(input.getBytes(StandardCharsets.UTF_8));
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
}
