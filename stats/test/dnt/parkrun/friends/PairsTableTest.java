package dnt.parkrun.friends;

import dnt.parkrun.datastructures.Athlete;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PairsTableTest
{
    private final Random random = new Random();

    private List<Athlete> athletes;

    @Before
    public void setUp() throws Exception
    {
        athletes = new ArrayList<>();
        athletes.add(Athlete.from("Abi", 1));
        athletes.add(Athlete.from("Barry", 2));
        athletes.add(Athlete.from("Ceri", 3));
        athletes.add(Athlete.from("Darwin", 4));
    }

    @Test
    public void test1()
    {
        PairsTable<Athlete> pairsTable = new PairsTable<>(athletes);

        System.out.printf("%1$10s\t", "");
        pairsTable.consumeFirst((rowAthlete, athletes) -> {
            athletes.forEach(colAthlete -> {
                System.out.printf("%s\t", colAthlete.name.charAt(0));
            });
        });
        System.out.println();

        pairsTable.forEach((rowAthlete, athletes) -> {
            String name10Characters = String.format("%1$10s", rowAthlete.name).substring(0, 10); // Row names
            System.out.printf(name10Characters + "\t");

            athletes.forEach(colAthlete -> {
                //System.out.printf("%d\t", random.nextInt(10));
                System.out.printf("%s\t", colAthlete.name.charAt(0));
            });
            System.out.println();
        });
    }
}