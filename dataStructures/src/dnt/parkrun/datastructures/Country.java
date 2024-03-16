package dnt.parkrun.datastructures;

import java.util.HashMap;
import java.util.Map;

public enum Country
{
    UNKNOWN(0, "??", null),

    AUSTRALIA(3, "AU", "parkrun.com.au"),
    AUSTRIA(4, "AS", ""),
    CANADA(14, "CA", ""),
    DENMARK(23, "DN", ""),
    FINLAND(30, "FI", ""),
    FRANCE(31, "FR", "parkrun.fr"),
    GERMANY(32, "DE", ""),
    ICELAND(-40, "IS", "parkrun.is"),
    IRELAND(42, "EI", ""),
    ITALY(44, "IT", ""),
    JAPAN(46, "JP", ""),
    MALAYSIA(57, "MA", ""),
    NETHERLANDS(64, "NE", ""),
    NZ(65, "NZ", "parkrun.co.nz"),
    NORWAY(67, "NO", ""),
    POLAND(74, "PO", ""),
    RUSSIA(-80, "RU", "parkrun.ru"),
    SINGAPORE(82, "SG", ""),
    SOUTH_AFRICA(85, "SA", ""),
    SWEDEN(88, "SW", ""),
    UK(97, "UK", "parkrun.org.uk"),
    USA(98, "US", "parkrun.us");

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

    Country(int countryCode, String countryCodeForDb, String baseUrl)
    {
        this.countryCode = countryCode;
        this.countryCodeForDb = countryCodeForDb;
        this.baseUrl = baseUrl;
    }

    public static Country valueOf(int countryCode)
    {
        return countryCodeToCountry.get(countryCode);
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
