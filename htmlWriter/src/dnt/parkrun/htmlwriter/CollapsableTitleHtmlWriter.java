package dnt.parkrun.htmlwriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

public class CollapsableTitleHtmlWriter extends BaseWriter implements Closeable
{
    public CollapsableTitleHtmlWriter(XMLStreamWriter writer, String title) throws XMLStreamException
    {
        this(writer, title, 1);
    }
    public CollapsableTitleHtmlWriter(XMLStreamWriter writer, String title, int level) throws XMLStreamException
    {
        super(writer);

        startDetails(level);
        startElement("summary");
        writer.writeCharacters(title);
        endElement("summary");
    }
    public CollapsableTitleHtmlWriter(XMLStreamWriter writer, String title, int level, double fontSizePercent) throws XMLStreamException
    {
        super(writer);

        startDetails(level);
        startElement("summary", "style", String.format("font-size:%.1f%%;", fontSizePercent));
        writer.writeCharacters(title);
        endElement("summary");
    }

    @Override
    public void close()
    {
        try
        {
            endDetails();
        }
        catch (XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
    }
}
