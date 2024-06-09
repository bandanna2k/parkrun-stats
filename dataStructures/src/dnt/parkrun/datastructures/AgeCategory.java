package dnt.parkrun.datastructures;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public enum AgeCategory
{
    UNKNOWN(null, 0, false, false),

    JM10("JM10", 1, true, false),
    JW10("JW10", 2, false, true),

    JM11_14("JM11-14", 3, true, false),
    JW11_14("JW11-14", 4, false, true),

    JM15_17("JM15-17", 5, true, false),
    JW15_17("JW15-17", 6, false, true),

    SM18_19("SM18-19", 7, true, false),
    SW18_19("SW18-19", 8, false, true),

    SM20_24("SM20-24", 9, true, false),
    SW20_24("SW20-24", 10, false, true),

    SM25_29("SM25-29", 11, true, false),
    SW25_29("SW25-29", 12, false, true),

    SM30_34("SM30-34", 13, true, false),
    SW30_34("SW30-34", 14, false, true),

    VM35_39("VM35-39", 15, true, false),
    VW35_39("VW35-39", 16, false, true),

    VM40_44("VM40-44", 17, true, false),
    VW40_44("VW40-44", 18, false, true),

    VM45_49("VM45-49", 19, true, false),
    VW45_49("VW45-49", 20, false, true),

    VM50_54("VM50-54", 21, true, false),
    VW50_54("VW50-54", 22, false, true),

    VM55_59("VM55-59", 23, true, false),
    VW55_59("VW55-59", 24, false, true),

    VM60_64("VM60-64", 25, true, false),
    VW60_64("VW60-64", 26, false, true),

    VM65_69("VM65-69", 27, true, false),
    VW65_69("VW65-69", 28, false, true),

    VM70_74("VM70-74", 29, true, false),
    VW70_74("VW70-74", 30, false, true),

    VM75_79("VM75-79", 31, true, false),
    VW75_79("VW75-79", 32, false, true),

    VM80_84("VM80-84", 33, true, false),
    VW80_84("VW80-84", 34, false, true),

    VM85_89("VM85-89", 35, true, false),
    VW85_89("VW85-89", 36, false, true),

    VM90_94("VM90-94", 37, true, false),
    VW90_94("VW90-94", 38, false, true),

    VM95_99("VM95-99", 39, true, false),
    VW95_99("VW95-99", 40, false, true),

    VM100_PLUS("VM100+", 41, true, false),
    VW100_PLUS("VW100+", 42, false, true),

    SM_TRIPLE_DASH("SM---", 43, true, false),
    SW_TRIPLE_DASH("SW---", 44, false, true),

    MWC("MWC", 45, true, false),
    WWC("WWC", 46, false, true),
    ;


    public final String textOnWebpage;
    public final int dbCode;
    private final boolean male;
    private final boolean female;

    AgeCategory(String textOnWebpage, int dbCode, boolean male, boolean female)
    {
        this.textOnWebpage = textOnWebpage;
        this.dbCode = dbCode;
        this.male = male;
        this.female = female;
    }

    private static Map<String, AgeCategory> textOnWebpageToAgeCategory = new HashMap<>();
    static
    {
        assert Arrays.stream(values()).map(Enum -> Enum.dbCode).collect(Collectors.toSet()).size() == values().length;
        Arrays.stream(values()).forEach(ageCategory -> textOnWebpageToAgeCategory.put(ageCategory.textOnWebpage, ageCategory));
        assert values().length == textOnWebpageToAgeCategory.size();
    }
    private static Map<Integer, AgeCategory> dbCodeToAgeCategory = new HashMap<>();
    static
    {
        Arrays.stream(values()).forEach(ageCategory -> dbCodeToAgeCategory.put(ageCategory.dbCode, ageCategory));
        assert values().length == dbCodeToAgeCategory.size();
    }

    public static AgeCategory from(String textOnWebpage)
    {
        AgeCategory ageCategory = textOnWebpageToAgeCategory.get(textOnWebpage);
        assert ageCategory != null : "Age group not found: " + textOnWebpage;
        return ageCategory;
    }

    public static AgeCategory from(int dbCode)
    {
        AgeCategory ageCategory = dbCodeToAgeCategory.get(dbCode);
        assert ageCategory != null : "Age group not found: " + dbCode;
        return ageCategory;
    }

    public boolean isMale()
    {
        return false;
    }
}
