package dnt.parkrun.datastructures;

public class Country
{
    public final CountryEnum countryEnum;
    public final String url;

    public Country(CountryEnum countryEnum, String url)
    {
        this.countryEnum = countryEnum;
        this.url = url;
    }

    @Override
    public String toString()
    {
        return "Country{" +
                "countryCode=" + countryEnum +
                ", url='" + url + '\'' +
                '}';
    }

    public Object getCountryCodeForDb()
    {
        return countryEnum.getCountryCodeForDb();
    }

    public int getCountryCode()
    {
        return countryEnum.getCountryCode();
    }
}
