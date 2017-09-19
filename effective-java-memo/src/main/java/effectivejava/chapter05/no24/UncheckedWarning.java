package effectivejava.chapter05.no24;

import java.util.ArrayList;
import java.util.List;

public class UncheckedWarning {

    public static void main(String[] args) {

        List<String> list = new ArrayList();

//        Warning:(10, 29) java: 無検査変換
//        期待値: java.util.List<java.lang.String>
//        検出値:    java.util.ArrayList
    }

}
