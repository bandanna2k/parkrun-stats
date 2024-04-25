package dnt.parkrun.friends;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class PairsTable<T>
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
        for (int i = 0; i < list1.size() - 1; i++)
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

    public void forEach(BiConsumer<T, T> consumer)
    {
        records.forEach(record ->
        {
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
