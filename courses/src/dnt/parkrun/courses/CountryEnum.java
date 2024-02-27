package dnt.parkrun.courses;

import java.util.HashMap;
import java.util.Map;

public enum CountryEnum
{
    UNKNOWN(0),

    AUSTRALIA(3),
    AUSTRIA(4),
    CANADA(14),
    DENMARK(23),
    FINLAND(30),
    FRANCE(31),
    GERMANY(32),
    IRELAND(42),
    ITALY(44),
    JAPAN(46),
    MALAYSIA(57),
    NETHERLANDS(64),
    NZ(65),
    NORWAY(67),
    POLAND(74),
    SINGAPORE(82),
    SOUTH_AFRICA(85),
    SWEDEN(88),
    UK(97),
    USA(98);

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

    CountryEnum(int countryCode)
    {
        this.countryCode = countryCode;
    }

    public static CountryEnum valueOf(int countryCode)
    {
        return countryCodeToEnum.get(countryCode);
    }

    public int getCountryCode()
    {
        return countryCode;
    }
}
