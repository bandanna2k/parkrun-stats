package dnt.parkrun.database.individualqueries;

import dnt.parkrun.datastructures.Athlete;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class TableWritingTest
{
    private Random random = new Random(1);

    private List<Athlete> athletes1;
    private List<Athlete> athletes2;

    @Before
    public void setUp() throws Exception
    {
        List<Athlete> athletes = new ArrayList<>();
        athletes.add(Athlete.from("Dave", 1));
        athletes.add(Athlete.from("Zoe", 2));
        athletes.add(Athlete.from("Tasch", 3));
        athletes.add(Athlete.from("Mel", 4));
        athletes.add(Athlete.from("Joe", 5));
        athletes.add(Athlete.from("Divine", 6));
        athletes.add(Athlete.from("Cole", 7));
        athletes.add(Athlete.from("Patience", 8));
        athletes.add(Athlete.from("Daniel", 9));
        athletes.add(Athlete.from("Craig", 10));
        athletes.add(Athlete.from("Ed", 11));
        athletes.add(Athlete.from("Francios", 12));
        athletes.add(Athlete.from("Greg", 13));
        athletes.add(Athlete.from("Hope", 14));
        athletes.add(Athlete.from("Ingrid-Marilyn", 15));
        athletes1 = new ArrayList<>(athletes);
        athletes2 = new ArrayList<>(athletes);
    }

    @Test
    public void test3()
    {
        System.out.printf("%1$10s\t", "");
        for (int j = athletes2.size() - 1; j > 0; j--)
        {
            Athlete athlete = athletes2.get(j);
            System.out.printf("%s\t", athlete.name.substring(0, 1));
        }
        System.out.println();

        for(int i = 0; i < athletes1.size() - 1; i++)
        {
            Athlete athlete1 = athletes1.get(i);

            String name10Characters = String.format("%1$10s", athlete1.name).substring(0, 10); // Row names
            System.out.printf(name10Characters + "\t", athlete1.name);

            for (int j = athletes2.size() - 1; j > 0; j--)
            {
                Athlete athlete2 = athletes2.getFirst();

                System.out.printf("%d\t", random.nextInt(10));
            }
            System.out.println();

            athletes2.removeLast();
        }
    }

    @Test
    public void test4()
    {
        PairsTable<Athlete> pairsTable = new PairsTable<>(athletes1);

        System.out.printf("%1$10s\t", "");
        for (int j = athletes2.size() - 1; j > 0; j--)
        {
            Athlete athlete = athletes2.get(j);
            System.out.printf("%s\t", athlete.name.substring(0, 1));
        }

        AtomicInteger lastAthleteId = new AtomicInteger(-1);
        pairsTable.forEach((athlete1, athlete2) -> {
            if(athlete1.athleteId != lastAthleteId.get())
            {
                lastAthleteId.set(athlete1.athleteId);
                System.out.println();
                String name10Characters = String.format("%1$10s", athlete1.name).substring(0, 10); // Row names
                System.out.printf(name10Characters + "\t", athlete1.name);
            }

            System.out.printf("%d\t", random.nextInt(10));
        });
    }

    public static class PairsTable<T>
    {
        private final List<T> list1;
        private final List<T> list2;

        private final List<List<T>> result = new ArrayList<>();
        private final List<Record> records = new ArrayList<>();

        public PairsTable(List<T> list)
        {
            this.list1 = new ArrayList<>(list);
            this.list2 = new ArrayList<>(list);
            loop();
        }

        private void loop()
        {
//            this.result = new ArrayList<>();
            for(int i = 0; i < list1.size() - 1; i++)
            {
                List<T> rowList = new ArrayList<>();

                T object1 = list1.get(i);

                for (int j = list2.size() - 1; j > 0; j--)
                {
                    T object2 = list2.getFirst();

                    records.add(new Record(object1, object2));

                    rowList.add(object2);
                }

                list2.removeLast();
                result.add(rowList);
            }
        }

        private void forEach(BiConsumer<T, T> consumer)
        {
            records.forEach(record -> {
                consumer.accept(record.row, record.column);
            });
        }

        private class Record
        {
            final T row;
            final T column;

            private Record(T row, T column)
            {
                this.row = row;
                this.column = column;
            }
        }
    }
}
