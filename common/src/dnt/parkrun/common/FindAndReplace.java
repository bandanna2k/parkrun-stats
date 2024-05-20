package dnt.parkrun.common;

import java.io.*;
import java.util.function.Supplier;

public abstract class FindAndReplace
{
    public static void findAndReplace(File input, File output, Object[][] replacements) throws IOException
    {
        try (FileInputStream fis = new FileInputStream(input);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader reader = new BufferedReader(isr))
        {
            try (FileOutputStream fos = new FileOutputStream(output);
                 OutputStreamWriter osw = new OutputStreamWriter(fos);
                 BufferedWriter writer = new BufferedWriter(osw))
            {
                String line;
                while (null != (line = reader.readLine()))
                {
                    String lineModified = line;
                    for (Object[] replacement : replacements)
                    {
                        final String criteria = (String) replacement[0];
                        if(line.contains(criteria))
                        {
                            lineModified = lineModified.replace(criteria, ((Supplier<String>)replacement[1]).get());
                        }
                    }
                    writer.write(lineModified + "\n");
                }
            }
        }
    }

    public static String getTextFromFile(InputStream inputStream)
    {
        StringBuilder sb = new StringBuilder();
        try(BufferedReader reader1 = new BufferedReader(new InputStreamReader(inputStream)))
        {
            String line1;
            while(null != (line1 = reader1.readLine()))
            {
                sb.append(line1);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
}
