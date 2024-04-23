package dnt.parkrun.htmlwriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;
import java.util.Optional;

public class CollapsableTitleHtmlWriter extends BaseWriter implements Closeable
{
    private CollapsableTitleHtmlWriter(XMLStreamWriter writer,
                                       String title,
                                       int level,
                                       Optional<Double> fontSizePercent,
                                       boolean open) throws XMLStreamException
    {
        super(writer);

        if(open)
            startDetailsOpen(level);
        else
            startDetails(level);

        if(fontSizePercent.isPresent())
            startElement("summary", "style", String.format("font-size:%.1f%%;", fontSizePercent.get()));
        else
            startElement("summary");

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

    public static class Builder
    {
        private final String title;
        private final XMLStreamWriter writer;
        private int level = 1;
        private Optional<Double> fontSizePercent = Optional.empty();
        private boolean open = false;

        public Builder(XMLStreamWriter writer, String title)
        {
            this.writer = writer;
            this.title = title;
        }

        public CollapsableTitleHtmlWriter build() throws XMLStreamException
        {
            return new CollapsableTitleHtmlWriter(writer, title, level, fontSizePercent, open);
        }

        public Builder open()
        {
            this.open = true;
            return this;
        }

        public Builder level(int level)
        {
            this.level = level;
            return this;
        }

        public Builder fontSizePercent(double fontSizePercent)
        {
            this.fontSizePercent = Optional.of(fontSizePercent);
            return this;
        }
    }
}
