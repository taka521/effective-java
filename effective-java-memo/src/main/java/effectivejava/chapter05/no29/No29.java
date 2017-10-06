package effectivejava.chapter05.no29;

import effectivejava.chapter05.no29.column.Age;
import effectivejava.chapter05.no29.column.FirstName;
import effectivejava.chapter05.no29.column.LastName;

import java.util.Date;

/**
 * 項目29 型安全な異種コンテナーを検討する
 */
public class No29 {

    public static void main(String[] args) {

        Class integerClass = Integer.class;     // クラスから、Classインスタンスを取得 => クラスリテラル

        String hello = "Hello";
        Class stringClass = hello.getClass();  // 変数からClassインスタンスを取得

        Class<Float> floatClass = Float.class;       // ちなみに、Classクラスはジェネリックな型。
        // Class<Object> objectClass = Double.class; // ジェネリックなので、不変。

        Date date = new Date();
        Class<? extends Date> dateClass = date.getClass(); // getClass() は、Class<? extends T> を返す。
        // 上記の場合、変数 date に格納されている型は「Dateのサブタイプ」ということしか分からない。
        // Class<?> clazz = date.getClass(); でもOK.

        Class<String[]> stringArrayClazz = String[].class; // 配列もいける


        // 異種コンテナに値を設定
        Favorites favorites = new Favorites();
        favorites.putFavorite(String.class, "Java");
        favorites.putFavorite(Integer.class, 0x5fafc);
        favorites.putFavorite(Class.class, Favorites.class);

        // 異種コンテナから値を取得 => Class<T>をキーに、Tを取得しているため型安全
        String favoriteString = favorites.getFavorite(String.class);
        Integer favoriteInteger = favorites.getFavorite(Integer.class);
        Class<?> favoriteClass = favorites.getFavorite(Class.class);

        System.out.printf("%s %x %s%n", favoriteString, favoriteInteger, favoriteClass.getName());
        // 結果 => Java 5fafc effectivejava.chapter05.no29.Favorites

        record();
    }

    private static void record() {

        // Effective Javaの異種コンテナの例が微妙なので、DBのレコードを異種コンテナとした。

        Record record = new Record();
        record.put(FirstName.class, FirstName.of("太郎"));
        record.put(LastName.class, LastName.of("田中"));
        record.put(Age.class, Age.of(20));

        FirstName firstName = record.get(FirstName.class);
        LastName lastName = record.get(LastName.class);
        Age age = record.get(Age.class);

        System.out.printf("%s %s %d", firstName.getValue(), lastName.getValue(), age.getValue());
    }

}
