package effectivejava.chapter05.no25;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 項目25 配列よりリストを選ぶ
 */
public class No25 {

    public static void main(String[] args) {

        List<String> list = new ArrayList<>();
        list.add("Hello World");
        list.get(0);

        // 非境界値ワイルドカード型の配列は生成が許されている
        List<?>[] lists = new ArrayList<?>[1];
        Map<?, ?>[] maps = new HashMap<?, ?>[1];
        ParameterArray<?>[] parameterArrays = new ParameterArray<?>[1];
    }

    private class ParameterArray<T> {

        // パラメータ化された配列の変数の宣言は可能。
        private T[] array;

        // 戻り値として、型パラメータの配列を定義することも可能。
        T[] getArray() {
            return this.array;
        }

        // 引数として、型パラメータの配列を定義することも可能。
        void setArray(T...array) {
            this.array = array;
        }

    }

}
