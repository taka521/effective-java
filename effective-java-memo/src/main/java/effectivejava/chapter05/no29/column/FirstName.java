package effectivejava.chapter05.no29.column;

/**
 * 名クラス
 */
public class FirstName extends Column<String> {

    private FirstName(final String value) {
        super(value);
    }

    public static FirstName of(final String value) {
        return new FirstName(value);
    }
}
