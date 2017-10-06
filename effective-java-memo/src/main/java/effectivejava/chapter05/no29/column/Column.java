package effectivejava.chapter05.no29.column;

/**
 * カラムクラス
 *
 * @param <T> カラムのデータ型
 */
public abstract class Column<T> {

    private final T value;

    Column(final T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

}
