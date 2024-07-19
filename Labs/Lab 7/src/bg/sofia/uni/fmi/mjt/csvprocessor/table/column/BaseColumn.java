package bg.sofia.uni.fmi.mjt.csvprocessor.table.column;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Set;
import java.util.TreeSet;

public class BaseColumn implements Column {

    private final Set<String> values;

    public BaseColumn() {
        this(new LinkedHashSet<>());
    }

    public BaseColumn(Set<String> values) {
        this.values = values;
    }

    @Override
    public void addData(String data) {
        if (data == null || data.isBlank()) {
            throw new IllegalArgumentException("Data cannot be null or blank");
        }
        values.add(data);
    }

    @Override
    public Collection<String> getData() {
        return Collections.unmodifiableSequencedSet(new LinkedHashSet<>(values));
    }
}
