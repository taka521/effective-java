# 項目28 APIの柔軟性向上のために境界ワイルドカードを使用する

パラメータ化された型は不変であるため、`List<Object>` と `List<String>` には何の関係もない。  
しかし、`Object` と `String` には継承関係があるため、`List<String>` が `List<Object>` のサブタイプでないのは直感に反する。

以下は項目26に登場したスタックのAPI。

```java
public class Stack<E> {
    public Stack();
    public void push(E e);
    public E pop();
    public boolean isEmpty();
}
```

ここに一連の要素を受け取り、それらの要素を全てプッシュする `pushAll` メソッドを追加したいと仮定する。

```java
public void pushAll(Iterable<E> src){
    for(E e : src){
        push(e);
    }
}
```

このメソッドはエラーも警告も出ずにコンパイルできて、機能するが十分に満足できるものではない。  

`Stack<Number>` に `Integer` 型の値を `push` したいと仮定した場合、
`Number` は `Integer` のスーパークラスなので正常に動作する。

```java
Stack<Number> stack = new Stack<>(); // Number型のスタック

Integer intVal = Integer.valueOf(0);
stack.push(intVal); // Number は Integer の親なので、pushできる。
```

そのため、次のコードも実行されるように思う。

```java
Stack<Number> stack = new Stack<>();

Iterable<Integer> integers = ....
stack.pushAll(integers);
```

が、パラメータ化された型は不変なので、コンパイルエラーになる。  
※`Iterable<Integer>` は `Iterable<Number>` のサブタイプではないため。

```
No28.java:17: エラー: 不適合な型: Iterable<Integer>をIterable<Number>に変換できません:
        stack.pushAll(integers);
                      ^
```


次に、スタックの要素を全てpopし、与えられたコレクションに追加する `popAll` メソッドを追加してみる。

```java
public void popAll(Collection<E> dst){
    while(!isEmpty()){
        dst.add(pop());
    }
}
```

こちらもコンパイルエラーにはならず、以下のコードは正常に実行できる。

```java
Stack<Number> stack = new Stack<>();
stack.push(1);
stack.push(2);

Object o = stack.pop(); // popした値は Number なので Object として受け取ることができる。
```

しかし次のコードは、`pushAll` と同じようにエラーとなる。  
理由は同じ。  


```java
Stack<Number> stack = new Stack<>();
stack.push(1);
stack.push(2);

List<Object> objects = new ArrayList<>();
stack.popAll(objects);

// No28.java:29: エラー: 不適合な型: List<Object>をCollection<Number>に変換できません:
//        stack.popAll(objects);
//                     ^
```

このように、パラメータ化された型が不変であることによって、柔軟性に欠けたAPIとなってしまう。


## 境界ワイルドカード型

ではどうすればいいかというと、パラメータ化された型に *共変* または *反変* を与えればよい。  
パラメータ化された型に共変、または反変を与えるには **境界ワイルドカード型** と呼ばれる特殊なパラメータ化された型を用いる。  

* 共変：`<? extends E>`
  * `E`、もしくは `E` のサブクラス
* 反変：`<? super E>`
  * `E`、もしくは `E` のスーパークラス

上記の `pushAll` は「`E` の `Iterable`」を引数に受け取っていたが、
実際に受け取りたいのは **「`E` の何らかのサブクラスの `Iterable`」** 。（要は、`E` として振る舞うことができる型が欲しい）  
サブクラスをスーパークラスで受け取りたい場合は共変を与えればよいので、`pushAll` は以下のように変更される。

```java
// 境界ワイルドカードを使用した pushAll 。
public void pushAll(Iterable<? extends E> src) {
    for (E e : src) push(e);
}
```

次に `popAll` は「`E` の `Collection`」を引数に受け取っていたが、
実際に受け取りたいのは **「`E` のスーパークラスの `Collection`」**。
（要は、スタックが保持する型 `E` のスーパークラスをパラメータとする `Collection` に入れたい）  
スーパークラスをサブクラスで受け取りたい場合は、反変を与えればよい。

```java
// 境界ワイルドカードを使用した popAll メソッド
public void popAll(Collection<? super E> dst) {
    while (!isEmpty()) dst.add(pop());
}
```

型パラメータを境界ワイルドカードにしたことで、今までコンパイルエラーとなっていたコードが実行できるようになる。

```java
Stacl<Number> stack = new Stack<>();

// pushAll
// Iterable<Integer> は Iterable<Number> のサブタイプとして振る舞うことができる => 共変
List<Integer> integers = Arrays.asList(1, 2, 3);
stack.pushAll(integers);  

// popAll
// Collection<Object> は Collection<Number> のサブタイプとして振る舞うことができる => 反変
List<Object> objects = new ArrayList<>();
stack.popAll(objects);
```

ちなみに `<? extends E>` のように共変を与える境界ワイルドカードは、**「上限境界ワイルドカード」** と言われる。  
上限（この場合、最低でも `E` 以下の型であること）の型が決まるので、「上限境界ワイルドカード」。  
`<? super E>`は反変、つまりは上限の反対なので **「下限境界ワイルドカード」**。  
下限（この場合、`E` 以上の型であること）の型が決まるので、「下限境界ワイルドカード」。

```
Object
  ^
  |  <? super Number>
  |
Number
  ^
  | <? extends Number>
  |
Integer
```


## Get & Put原則

上限境界ワイルドカード（`<? extends T>`）と下限境界ワイルドカード（`<? super T>`）の
どちらを使用すればよいかの指針が **Get & Put原則**。

別名、**PECS（Producer-extends and Consumer-super）** 。

パラメータ化された型が「プロデューサー（生産者）」であれば上限境界ワイルドカード、
「コンシューマ（消費者）」であれば下限境界ワイルドカードを使用する。

プロデューサーとは、値を生産する引数のこと。  
例えば `pushAll` メソッドは、引数の `src` を使用して `Stack` が保持する `E` 型の値を生成する。  
（`src` から値を取得して、スタックへ格納している）

```java
public void pushAll(Iterable<? extends E> src) {
    for (E e : src) push(e);  // Stack へ値を生成している => プロデューサー
}
```

コンシューマとは、値を消費する引数のこと。  
`popAll` メソッドでは `Stack` が保持する `E` の値を、引数 `dst` で消費している。  
（スタックから値を取得して、`dst` へ格納している）

```java
public void popAll(Collection<? super E> dst) {
    while (!isEmpty()) dst.add(pop());  // Stack から値を消費している => コンシューマ
}
```


## 注意する点

境界ワイルドカードを使用する場合、気をつける点がある。

* 戻り値としてワイルドカードを使用しないこと。
  * クライアント側のコードでワイルドカード型を使用することを強制することになるため。
* 型パラメータ `T` が `<T extends Comparable<T>>` である場合、`<T extends Compareble<? super T>>` と宣言すること。
  * `Comparable<T>` は `T` を消費して比較を行うので、コンシューマ。
  * よって、Get&Put原則に乗っ取ると `Compareble<T>` は `Comparable<? super T>` となる。
  * 例えば、リストの最大値を取得するメソッドは `public <T extends Compareble<? super T>> T max(List<? extends T> list)`
