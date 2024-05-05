package dnt.parkrun.region;

import dnt.parkrun.datastructures.Country;

public class RegionCheckerFactory
{
    public RegionChecker getRegionChecker(Country country)
    {
        return switch (country)
        {
            case NZ -> new NewZealandRegionChecker();
            default -> throw new IllegalArgumentException("No region exists for " + country);
        };
    }
}
