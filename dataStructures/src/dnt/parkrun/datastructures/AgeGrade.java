package dnt.parkrun.datastructures;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class AgeGrade
{
    public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
    public final boolean assisted;
    public final BigDecimal ageGrade;


    private AgeGrade(boolean assisted, BigDecimal ageGrade)
    {
        this.assisted = assisted;
        this.ageGrade = ageGrade;
    }

    public static AgeGrade newInstanceAssisted()
    {
        return new AgeGrade(true, new BigDecimal(-1));
    }

    public static AgeGrade newInstance(BigDecimal ageGrade)
    {
        return new AgeGrade(false, ageGrade);
    }

    public static AgeGrade newInstance(String ageGrade)
    {
        if("Assisted".equals(ageGrade.trim())) return newInstanceAssisted();
        String substring = ageGrade.substring(0, ageGrade.indexOf('%'));
        return newInstance(new BigDecimal(substring.trim()));
    }

    public static AgeGrade newInstanceNoAgeGrade()
    {
        return AgeGrade.newInstance(BigDecimal.ZERO);
    }

    public static AgeGrade newInstanceFromDb(int ageGradeFromDb)
    {
        if(ageGradeFromDb == 0) return newInstanceNoAgeGrade();
        if(ageGradeFromDb == -100) return newInstanceAssisted();
        BigDecimal ageGrade = new BigDecimal(ageGradeFromDb)
                .setScale(2, RoundingMode.HALF_UP)
                .divide(ONE_HUNDRED, RoundingMode.HALF_UP);
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
        return ageGrade.multiply(ONE_HUNDRED).intValue();
    }
}
