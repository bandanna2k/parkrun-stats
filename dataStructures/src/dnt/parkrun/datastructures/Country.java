package dnt.parkrun.datastructures;

public class Country
{
    public final int countryCode;
    public final String url;

    public Country(int countryCode, String url)
    {
        this.countryCode = countryCode;
        this.url = url;
    }

    @Override
    public String toString()
    {
        return "Country{" +
                "countryCode=" + countryCode +
                ", url='" + url + '\'' +
                '}';
    }
}
