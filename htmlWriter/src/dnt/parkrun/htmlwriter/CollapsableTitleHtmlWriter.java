package dnt.parkrun.htmlwriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

public class CollapsableTitleHtmlWriter extends BaseWriter implements Closeable
{
    public CollapsableTitleHtmlWriter(XMLStreamWriter writer, String title) throws XMLStreamException
    {
        super(writer);

        startElement("details");
        startElement("summary");
        writer.writeCharacters(title);
        endElement("summary");
    }

    @Override
    public void close()
    {
        try
        {
            endElement("details");
        }
        catch (XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
    }
}
