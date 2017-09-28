package effectivejava.chapter05.no26;

import java.util.Arrays;
import java.util.EmptyStackException;

/**
 * 項目6のスタック実装
 */
public class Stack {

    private Object[] elements;
    private int size;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        this.elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public Object pop() {
        if (this.size == 0) throw new EmptyStackException();

        Object result = elements[--size];
        elements[size] = null;  // 廃れた参照を取り除く
        return result;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    private void ensureCapacity() {
        if (elements.length == size) {
            elements = Arrays.copyOf(elements, 2 * size + 1);
        }
    }
}
