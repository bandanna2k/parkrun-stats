package dnt.parkrun.datastructures;

import java.util.HashMap;
import java.util.Map;

public enum CountryEnum
{
    UNKNOWN(0, "??"),

    AUSTRALIA(3, "AU"),
    AUSTRIA(4, "AS"),
    CANADA(14, "CA"),
    DENMARK(23, "DN"),
    FINLAND(30, "FI"),
    FRANCE(31, "FR"),
    GERMANY(32, "DE"),
    IRELAND(42, "EI"),
    ITALY(44, "IT"),
    JAPAN(46, "JP"),
    MALAYSIA(57, "MA"),
    NETHERLANDS(64, "NE"),
    NZ(65, "NZ"),
    NORWAY(67, "NO"),
    POLAND(74, "PO"),
    SINGAPORE(82, "SG"),
    SOUTH_AFRICA(85, "SA"),
    SWEDEN(88, "SW"),
    UK(97, "UK"),
    USA(98, "US");

    private static final Map<Integer, CountryEnum> countryCodeToEnum = new HashMap<>()
    {
        {
            for (CountryEnum value : CountryEnum.values())
            {
                put(value.countryCode, value);
            }
        }
    };

    private final int countryCode;
    private final String countryCodeForDb;

    CountryEnum(int countryCode, String countryCodeForDb)
    {
        this.countryCode = countryCode;
        this.countryCodeForDb = countryCodeForDb;
    }

    public static CountryEnum valueOf(int countryCode)
    {
        return countryCodeToEnum.get(countryCode);
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
