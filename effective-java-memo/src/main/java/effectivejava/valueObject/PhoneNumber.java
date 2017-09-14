package effectivejava.valueObject;

/**
 * 電話番号を表す値クラス
 */
public class PhoneNumber implements Comparable<PhoneNumber> {

    /** 市外局番 */
    private final short areaCode;

    /** 市内局番の前半 */
    private final short prefix;

    /** 市内局番の後半 */
    private final short lineNumber;

    public PhoneNumber(short areaCode, short prefix, short lineNumber) {
        this.areaCode = areaCode;
        this.prefix = prefix;
        this.lineNumber = lineNumber;
    }

    public short getAreaCode() {
        return areaCode;
    }

    public short getPrefix() {
        return prefix;
    }

    public short getLineNumber() {
        return lineNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhoneNumber)) return false;

        PhoneNumber that = (PhoneNumber) o;

        if (areaCode != that.areaCode) return false;
        if (prefix != that.prefix) return false;
        return lineNumber == that.lineNumber;
    }

    @Override
    public int hashCode() {
        int result = (int) areaCode;
        result = 31 * result + (int) prefix;
        result = 31 * result + (int) lineNumber;
        return result;
    }

    //    改善前の compareTo
    //    @Override
    //    public int compareTo(PhoneNumber o) {
    //
    //        // 市外局番の比較
    //        if (areaCode < o.areaCode) return -1;
    //        if (areaCode > o.areaCode) return 1;
    //
    //        // 市内局番の前半比較
    //        if (prefix < o.prefix) return -1;
    //        if (prefix > o.prefix) return 1;
    //
    //        // 市内局番の後半を比較
    //        if (lineNumber < o.lineNumber) return -1;
    //        if (lineNumber > o.lineNumber) return 1;
    //
    //        return 0; // 全てのフィールドが正しい
    //    }

    // 改善後の compareTo
    @Override
    public int compareTo(PhoneNumber o) {

        // 市外局番との比較
        int areaCodeDiff = areaCode - o.areaCode;
        if (areaCodeDiff != 0) return areaCodeDiff;

        // 市内局番の前半との比較
        int prefixDiff = prefix - o.prefix;
        if (prefixDiff != 0) return prefixDiff;

        // 市内局番の後半との比較
        return lineNumber - o.lineNumber;
    }
}
