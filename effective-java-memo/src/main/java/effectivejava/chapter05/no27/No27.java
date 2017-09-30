package effectivejava.chapter05.no27;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 項目27 ジェネリックメソッドを使用する
 */
public class No27 {

    public static void main(String[] args) {
        Set<String> s1 = new HashSet<>(Arrays.asList("A", "B", "C"));
        Set<String> s2 = new HashSet<>(Arrays.asList("1", "2", "3"));
        Set<String> unionSet = Union.union(s1, s2);
        System.out.println(unionSet); // 実行結果 => [A, 1, B, 2, C, 3]

        // ==============================================================

        UnaryFunction<String> unaryFunction = identityFunction();
        String[] strings = {"A", "B", "C"};
        for (String s : strings) {
            System.out.println(unaryFunction.apply(s));
        }

        UnaryFunction<Number> numberUnaryFunction = identityFunction();
        Number[] numbers = {1, 2, 3};
        for (Number n : numbers) {
            System.out.println(numberUnaryFunction.apply(n));
        }

        List<Integer> integers = Arrays.asList(1, 5, 3, 8);
        System.out.println(max(integers));
    }

    // ジェネリックシングルトンファクトリー・パターン（Java8以降は、lambdaで書ける）
    private static UnaryFunction<Object> IDENTITY_FUNCTION = arg -> arg;

    // IDENTITY_FUNCTION は状態を持っておらず、その型パラメータは非境界値なので、
    // 全ての型に対して単一インスタンを共有するのは安全
    @SuppressWarnings("unchecked")
    public static <T> UnaryFunction<T> identityFunction() {
        return (UnaryFunction<T>) IDENTITY_FUNCTION;
    }

    public static <T extends Comparable<T>> T max(List<T> list) {
        Iterator<T> iterator = list.iterator();
        T max = iterator.next();

        T work;
        while (iterator.hasNext()) {
            work = iterator.next();
            if (work.compareTo(max) < 0) {
                max = work;
            }
        }

        return max;
    }

}
