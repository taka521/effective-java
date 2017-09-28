package effectivejava.chapter05.no26;

/**
 * 項目26 ジェネリック型を使用する
 */
public class No26 {

    public static void main(String[] args) {

        // 項目6で登場したスタック実装
        Stack stack = new Stack();
        stack.push("A");
        stack.push("B");

        String b = (String) stack.pop(); // キャストが必要
        String a = (String) stack.pop(); // キャストが必要

        // コンパイルは通るが、実行時に ClassCastException が発生する
        stack.push("C");
        Integer c = (Integer) stack.pop(); // キャストに失敗する


        // ジェネリックなスタック実装
        GenericStack<String> genericStack = new GenericStack<>();
        genericStack.push("A");
        genericStack.push("B");

        b = genericStack.pop(); // キャストの必要性はない
        a = genericStack.pop(); // キャストの必要性はない
    }
}
