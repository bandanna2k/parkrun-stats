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
                "countryEnum=" + countryEnum +
                ", url='" + url + '\'' +
                '}';
    }

    public Object getCountryDbCode()
    {
        return countryEnum.getCountryDbCode();
    }

    public int getCountryCode()
    {
        return countryEnum.getCountryCode();
    }
}
