package effectivejava.chapter05.denature;

import java.util.ArrayList;
import java.util.List;

/**
 * Javaの変性に関するメモ
 */
public class Denature {

    public static void main(String[] args) {

        // 共変
        //   Javaでは、配列は共変性を持つ。

        Object[] objects = new Object[10];
        String[] strings = new String[5];
        objects = strings;


        // 反変
        //   Javaでは反変は許されてない。

        // strings = objects;  // 反変が許可されていれば、この代入が可能。


        // 不変
        //    ジェネリクス型は不変。

        List<Object> objectList = new ArrayList<>();
        List<String> stringList = new ArrayList<>();
        // objectList = stringList;  // コンパイルエラー
        // stringList = objectList;  // コンパイルエラー


        throwArrayStoreException();
    }

    /**
     * 共変性によって、ArrayStoreException をスローさせる。
     */
    private static void throwArrayStoreException() {

        Object[] objects = new String[1];

        // 配列は共変なので、String[] として定義された参照先に
        // Integer 型を格納する処理を書いてもコンパイルエラーにならない。
        objects[0] = Integer.valueOf(0);  // ArrayStoreExceptionがスローされる!!
    }

}
