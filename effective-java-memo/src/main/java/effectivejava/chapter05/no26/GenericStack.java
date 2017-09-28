package effectivejava.chapter05.no26;

import java.util.Arrays;
import java.util.EmptyStackException;

/**
 * Genericなスタック実装
 *
 * @param <E> スタックが保持する、要素の型
 */
public class GenericStack<E> {

    private E[] elements;
    private int size;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    // elements は push(E e) メソッドからのみ E型 の要素が追加されるため問題ない。
    @SuppressWarnings("unchecked")
    public GenericStack() {
        this.elements = (E[]) new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(E e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public E pop() {
        if (this.size == 0) throw new EmptyStackException();

        E result = (E) elements[--size];
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
