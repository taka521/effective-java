package effectivejava.chapter05.no29.column;

/**
 * 年齢クラス
 */
public class Age extends Column<Integer> {

    private Age(final Integer value) {
        super(value);
    }

    public static Age of(final Integer value) {
        return new Age(value);
    }
}
