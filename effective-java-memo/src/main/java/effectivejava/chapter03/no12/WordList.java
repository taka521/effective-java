package effectivejava.chapter03.no12;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class WordList {

    public static void main(String[] args) {
        Set<String> s = new TreeSet<>();
        Collections.addAll(s, "c3jeql85o9wz0p".split(""));
        System.out.println(s); // 結果　=> [0, 3, 5, 8, 9, c, e, j, l, o, p, q, w, z]
    }
}
