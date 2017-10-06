package effectivejava.chapter05.no29;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 型安全な異種コンテナー
 */
public class Favorites {

    private Map<Class<?>, Object> favorites = new HashMap<>();

    public <T> void putFavorite(Class<T> type, T instance) {
        Objects.requireNonNull(type, "Type is null.");
        this.favorites.put(type, instance);
    }

    public <T> T getFavorite(Class<T> type) {
        return type.cast(favorites.get(type));
    }

}
