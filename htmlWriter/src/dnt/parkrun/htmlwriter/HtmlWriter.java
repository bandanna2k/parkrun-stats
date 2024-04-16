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
    private final Date date;
    private final File file;

    public static HtmlWriter newInstance(Date date, String prefix) throws IOException, XMLStreamException
    {
        File file = new File(prefix + "_" + DateConverter.formatDateForDbTable(date) + ".html");

        XMLStreamWriter writer = XMLOutputFactory
                .newInstance()
                .createXMLStreamWriter(
                        new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
        return new HtmlWriter(writer, date, file);
    }

    private HtmlWriter(XMLStreamWriter writer, Date date, File file) throws XMLStreamException, IOException
    {
        super(writer);
        this.date = date;
        this.file = file;

        startHtml();
    }

    private void startHtml() throws XMLStreamException, IOException
    {
        writer.writeStartDocument();

        startElement("html");

        startElement("title");
        writer.writeCharacters("New Zealand parkrun Statistics for Events on " + DateConverter.formatDateForHtml(date));
        endElement("title");

        writer.writeStartElement("style\n");
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
