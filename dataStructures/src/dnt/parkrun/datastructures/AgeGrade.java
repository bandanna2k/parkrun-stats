package dnt.parkrun.datastructures;

public class AgeGrade
{
    public final boolean assisted;
    public final double ageGrade;


    private AgeGrade(boolean assisted, double ageGrade)
    {
        this.assisted = assisted;
        this.ageGrade = ageGrade;
    }

    public static AgeGrade newInstanceAssisted()
    {
        return new AgeGrade(true, -1);
    }

    public static AgeGrade newInstance(double ageGrade)
    {
        return new AgeGrade(false, ageGrade);
    }

    public static AgeGrade newInstance(String ageGrade)
    {
        if("Assisted".equals(ageGrade.trim())) return newInstanceAssisted();
        return newInstance(Double.parseDouble(ageGrade.substring(0, ageGrade.indexOf('%'))));
    }

    public static AgeGrade newInstanceNoAgeGrade()
    {
        return AgeGrade.newInstance(0);
    }

    public static AgeGrade newInstanceFromDb(Integer ageGradeFromDb)
    {
        if(ageGradeFromDb == null) return null;
        if(ageGradeFromDb == 0) return newInstanceNoAgeGrade();
        if(ageGradeFromDb == -100) return newInstanceAssisted();
        double ageGrade = ((double)ageGradeFromDb) / 100.0;
        return newInstance(ageGrade);
    }

    @Override
    public String toString()
    {
        return "AgeGrade{" +
                "assisted=" + assisted +
                ", ageGrade=" + ageGrade +
                '}';
    }

    public int getAgeGradeForDb()
    {
        return (int)(ageGrade * 100.0d);
    }
}
