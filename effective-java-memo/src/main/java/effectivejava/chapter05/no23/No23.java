package effectivejava.chapter05.no23;

import java.util.ArrayList;
import java.util.List;

public class No23 {

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        unsafeAdd(list, Integer.valueOf(1));

        // safeAdd(list, Integer.valueOf(1));
        // コンパイルエラー  →  Error:(12, 17) java: 不適合な型: java.util.List<java.lang.String>をjava.util.List<java.lang.Object>に変換できません:

        String s = list.get(0);
    }

    //原型を使用した、安全でないメソッド
    private static void unsafeAdd(List list, Object o) {
        list.add(o);
    }

    // ジェネリックスを利用した安全なメソッド
    private static void safeAdd(List<Object> list, Object o) {
        list.add(o);
    }

//    private void wildcard() {
//        List<?> list = new ArrayList<>();
//        list.add("one");
//
//        Error:(30, 13) java: addに適切なメソッドが見つかりません(java.lang.String)
//        メソッド java.util.Collection.add(?のキャプチャ#1)は使用できません
//                (引数の不一致: java.lang.Stringを?のキャプチャ#1に変換できません:)
//        メソッド java.util.List.add(?のキャプチャ#1)は使用できません
//                (引数の不一致: java.lang.Stringを?のキャプチャ#1に変換できません:)
//    }
}
