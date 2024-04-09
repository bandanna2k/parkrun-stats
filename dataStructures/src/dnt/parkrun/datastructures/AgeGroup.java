package dnt.parkrun.datastructures;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public enum AgeGroup
{
    UNKNOWN(null, 0),

    JM10("JM10", 1),
    JW10("JW10", 2),

    JM11_14("JM11-14", 3),
    JW11_14("JW11-14", 4),

    JM15_17("JM15-17", 5),
    JW15_17("JW15-17", 6),

    SM18_19("SM18-19", 7),
    SW18_19("SW18-19", 8),

    SM20_24("SM20-24", 9),
    SW20_24("SW20-24", 10),

    SM25_29("SM25-29", 11),
    SW25_29("SW25-29", 12),

    SM30_34("SM30-34", 13),
    SW30_34("SW30-34", 14),

    VM35_39("VM35-39", 15),
    VW35_39("VW35-39", 16),

    VM40_44("VM40-44", 17),
    VW40_44("VW40-44", 18),

    VM45_49("VM45-49", 19),
    VW45_49("VW45-49", 20),

    VM50_54("VM50-54", 21),
    VW50_54("VW50-54", 22),

    VM55_59("VM55-59", 23),
    VW55_59("VW55-59", 24),

    VM60_64("VM60-64", 25),
    VW60_64("VW60-64", 26),

    VM65_69("VM65-69", 27),
    VW65_69("VW65-69", 28),

    VM70_74("VM70-74", 29),
    VW70_74("VW70-74", 30),

    VM75_79("VM75-79", 31),
    VW75_79("VW75-79", 32),

    VM80_84("VM80-84", 33),
    VW80_84("VW80-84", 34),

    VM85_89("VM85-89", 35),
    VW85_89("VW85-89", 36),

    VM90_94("VM90-94", 37),
    VW90_94("VW90-94", 38),

    VM95_99("VM95-99", 39),
    VW95_99("VW95-99", 40),

    VM100_PLUS("VM100+", 41),
    VW100_PLUS("VW100+", 42),

    SM_TRIPLE_DASH("SM---", 43),
    SW_TRIPLE_DASH("SW---", 44),

    MWC("MWC", 45),
    WWC("WWC", 46),
    ;


    public final String textOnWebpage;
    public final int dbCode;

    AgeGroup(String textOnWebpage, int dbCode)
    {

        this.textOnWebpage = textOnWebpage;
        this.dbCode = dbCode;
    }

    private static Map<String, AgeGroup> textOnWebpageToAgeGroup = new HashMap<>();
    static
    {
        assert Arrays.stream(values()).map(Enum -> Enum.dbCode).collect(Collectors.toSet()).size() == values().length;
        Arrays.stream(values()).forEach(ageGroup -> textOnWebpageToAgeGroup.put(ageGroup.textOnWebpage, ageGroup));
        assert values().length == textOnWebpageToAgeGroup.size();
    }
    private static Map<Integer, AgeGroup> dbCodeToAgeGroup = new HashMap<>();
    static
    {
        Arrays.stream(values()).forEach(ageGroup -> dbCodeToAgeGroup.put(ageGroup.dbCode, ageGroup));
        assert values().length == dbCodeToAgeGroup.size();
    }

    public static AgeGroup from(String textOnWebpage)
    {
        AgeGroup ageGroup = textOnWebpageToAgeGroup.get(textOnWebpage);
        assert ageGroup != null : "Age group not found: " + textOnWebpage;
        return ageGroup;
    }

    public static AgeGroup from(Integer dbCode)
    {
        if(dbCode == null) return null;
        AgeGroup ageGroup = dbCodeToAgeGroup.get(dbCode);
        assert ageGroup != null : "Age group not found: " + dbCode;
        return ageGroup;
    }
}
