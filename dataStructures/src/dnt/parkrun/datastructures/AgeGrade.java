package dnt.parkrun.datastructures;

import java.util.Objects;

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
        return new AgeGrade(true, -1.0);
    }

    public static AgeGrade newInstance(double ageGrade)
    {
        return new AgeGrade(false, ageGrade);
    }

    public static AgeGrade newInstance(String ageGrade)
    {
        if("Assisted".equals(ageGrade.trim())) return newInstanceAssisted();
        String substring = ageGrade.substring(0, ageGrade.indexOf('%'));
        return newInstance(Double.parseDouble(substring.trim()));
    }

    public static AgeGrade newInstanceNoAgeGrade()
    {
        return AgeGrade.newInstance(0.0);
    }

    public static AgeGrade newInstanceFromDb(int ageGradeFromDb)
    {
        if(ageGradeFromDb == 0) return newInstanceNoAgeGrade();
        if(ageGradeFromDb == -100) return newInstanceAssisted();

        double ageGrade = ageGradeFromDb / 100.0;
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        AgeGrade ageGrade1 = (AgeGrade) o;
        return assisted == ageGrade1.assisted && Objects.equals(ageGrade, ageGrade1.ageGrade);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(assisted, ageGrade);
    }

    public int getAgeGradeForDb()
    {
        return (int)(Math.round(ageGrade * 100.0));
    }
}
