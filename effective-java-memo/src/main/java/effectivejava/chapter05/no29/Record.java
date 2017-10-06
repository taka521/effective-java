package effectivejava.chapter05.no29;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class Record {

    private Map<Class<?>, Object> record = new HashMap<>();

    <T> void put(final Class<T> type, final T value) {
        Objects.requireNonNull(type, "Type is null.");
        this.record.put(type, type.cast(value));
    }

    <T> T get(final Class<T> type) {
        return type.cast(record.get(type));
    }

}
