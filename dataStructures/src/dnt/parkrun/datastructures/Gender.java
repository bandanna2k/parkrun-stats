package dnt.parkrun.datastructures;

public enum Gender
{
    Male("M"),
    Female("F");

    public final String dbCode;

    Gender(String dbCode)
    {
        this.dbCode = dbCode;
    }

    public static Gender from(String input)
    {
        if(input == null) return null;

        String input2 = input.replace("&nbsp;", "");
        return input2.trim().isEmpty() ? null : Gender.valueOf(input2);
    }
}
