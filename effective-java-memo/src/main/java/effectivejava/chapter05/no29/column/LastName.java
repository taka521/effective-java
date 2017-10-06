package effectivejava.chapter05.no29.column;

/**
 * 姓クラス
 */
public class LastName extends Column<String> {

    private LastName(final String value) {
        super(value);
    }

    public static LastName of(final String value) {
        return new LastName(value);
    }
}
