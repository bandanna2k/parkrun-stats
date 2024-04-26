package dnt.parkrun.friends;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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

        assert valid();
    }

    private boolean valid()
    {
        AtomicBoolean result = new AtomicBoolean(true);
        forEach((t, ts) -> {
            ts.forEach(t1 -> {
                if(t == t1) result.set(false);
            });
        });
        return result.get();
    }

    private void loop() {
        for (int i = 0; i < list1.size() - 1; i++) {
            List<T> rowList = new ArrayList<>();

            for (int j = list2.size() - 1; j > 0; j--)
            {
                rowList.add(list2.get(j));
            }

            list2.remove(0);
            result.add(rowList);
        }
    }

    public void consumeFirst(BiConsumer<T, List<T>> consumer)
    {
        final T item = list1.get(0);
        final List<T> items = result.get(0);

        consumer.accept(item, items);
    }

    public void forEach(BiConsumer<T, List<T>> consumer)
    {
        for (int i = 0; i < result.size(); i++)
        {
            final T item = list1.get(i);
            final List<T> items = result.get(i);

            consumer.accept(item, items);
        }
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
