package dnt.parkrun.datastructures;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum Country
{
    UNKNOWN("Unknown", 0, "??", null),

    AUSTRALIA("Australia", 3, "AU", "parkrun.com.au"),
    AUSTRIA("Austria", 4, "AS", ""),
    CANADA("Canada", 14, "CA", "parkrun.ca"),
    DENMARK("Denmark", 23, "DN", ""),
    FINLAND("Finland", 30, "FI", ""),
    FRANCE("France", 31, "FR", "parkrun.fr"),
    GERMANY("Germany", 32, "DE", ""),
    ICELAND("Iceland", -40, "IS", "parkrun.is"),
    IRELAND("Ireland", 42, "EI", ""),
    ITALY("Italy", 44, "IT", ""),
    JAPAN("Japan", 46, "JP", ""),
    MALAYSIA("Malaysia", 57, "MA", "parkrun.my"),
    NETHERLANDS("Netherlands", 64, "NE", ""),
    NZ("New Zealand", 65, "NZ", "parkrun.co.nz"),
    NORWAY("Norway", 67, "NO", ""),
    POLAND("Poland", 74, "PO", ""),
    RUSSIA("Russia", -80, "RU", "parkrun.ru"),
    SINGAPORE("Singapore", 82, "SG", "parkrun.sg"),
    SOUTH_AFRICA("South Africa", 85, "SA", "parkrun.co.za"),
    SWEDEN("Sweden", 88, "SW", "parkrun.se"),
    UK("United Kingdom", 97, "UK", "parkrun.org.uk"),
    USA("United States", 98, "US", "parkrun.us");

    private static final Map<Integer, Country> countryCodeToCountry = new HashMap<>()
    {
        {
            for (Country value : Country.values())
            {
                put(value.countryCode, value);
            }
        }
    };

    public final int countryCode;
    public final String countryCodeForDb;
    public final String baseUrl;
    public final String countryName;

    Country(String countryName, int countryCode, String countryCodeForDb, String baseUrl)
    {
        this.countryCode = countryCode;
        this.countryCodeForDb = countryCodeForDb;
        this.baseUrl = baseUrl;
        this.countryName = countryName;
    }

    public static Country valueOf(int countryCode)
    {
        return countryCodeToCountry.get(countryCode);
    }

    public static Country findFromUrl(String url)
    {
        List<Country> list = Arrays.stream(values()).filter(c -> c.baseUrl != null).collect(Collectors.toList());
        for(Country country : list)
        {
            if(url.contains(country.baseUrl)) return country;
        }
        return UNKNOWN;
    }

    public int getCountryCode()
    {
        return countryCode;
    }

    public String getCountryDbCode()
    {
        return countryCodeForDb;
    }
}
