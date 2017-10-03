package effectivejava.chapter05.no28;

import effectivejava.chapter05.no26.GenericStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 項目28 APIの柔軟性向上のために境界ワイルドカードを使用する
 */
public class No28 {

    public static void main(String[] args) {

        GenericStack<Number> stack = new GenericStack<>();

        stack.push(Integer.valueOf(0)); // Integer をpushすることはできる。

        Iterable<Integer> integers = Arrays.asList(0, 1, 2);
        stack.pushAll(integers);
        // が、パラメータ化された型は不変なので、
        // Iterable<Number> に Iterable<Integer> を入れることはできない。 => 直感に反する!

        // No28.java:17: エラー: 不適合な型: Iterable<Integer>をIterable<Number>に変換できません:
        //          stack.pushAll(integers);
        //                       ^

        // ========================================================================

        stack.push(1);
        stack.push(2);

        Object o = stack.pop();  // popした値を Object へ格納することは可能

        List<Object> objects = new ArrayList<>();
        stack.popAll(objects);  // ただ、Collection<Object>  は Collection<Number>　のサブタイプではない。

    }


}
