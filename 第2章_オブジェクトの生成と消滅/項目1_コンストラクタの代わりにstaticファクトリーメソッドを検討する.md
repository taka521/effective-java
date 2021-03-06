# 第2章 オブジェクトの生成と消滅

## 項目1 コンストラクタの代わりにstaticファクトリーメソッドを検討する

* staticファクトリーメソッドは、デザインパターンのファクトリメソッドパターン**ではない**

### staticファクトリメソッドの長所

#### 1. コンストラクタとは異なり、名前を持つこと

コンストラクタに対するパラメータ自身が、返されるオブジェクトを表現していない場合には、適切な名前のstaticファクトリメソッドは使いやすい。  
また、その結果としてコードの可読性が向上する。


#### 2. メソッドが呼び出される度に、インスタンスを生成する必要がない

不変クラスなどであらかじめ生成しておいたインスタンスを使いまわしたり、  
キャッシュされていなければ生成後にキャッシュを行うことで、重複したインスタンスのが不必要に生成されるのを回避することが可能。

同値オブジェクトが頻繁に要求されたり、インスタンスの生成コストが高い場合に効果的。  
これらは、**インスタンス制御されている**と言える。

インスタンス制御されることで、

* シングルトン、もしくはインスタンス化不能であることを保証することができる。
* 不変なクラスに、２つの同じインスタンスが存在しないことを保証することができる。  
`a==b`が成り立つ場合だけ`a.equals(b)`が成り立つことを保証することで、`equals`ではなく`==`を使用することができ、パフォーマンスの向上につながる。  
enumはこの保証を提供している。


#### 3. メソッドの戻り値型のサブタイプを返すことができる

これにより、どのクラスのインスタンスを返すかという選択の柔軟性を大きく広げる。  
この応用として、返却するインスタンスのクラスをpublicにすることなく、API経由でインスタンスを返すことができるようになること。

例えば、コレクションフレームワーク。  
コレクション(`List`, `Map`, `Set`など）のインターフェースの便利な実装が多数存在しており、それらは`java.util.Collections`のstaticファクトリメソッドを通して外部に提供されている。  
staticファクトリメソッドから返却される各実装クラスは**全てpublic**ではない。

これらの実装クラスをpublicにして別々に提供した場合と、1つのクラスからstaticファクトリメソッドを通して提供した場合とでは後者の方が**概念的な重み**が軽い。  
使用する側としては、返却されるインスタンスがインターフェースで表されたAPIを保持していることを知っているので、余分なドキュメントを読む必要がなくなる。(インターフェースを知っていればよい）  
そのため、基本的には実装クラスのインスタンスを返却するのではなく、インターフェース型として返却することが望ましい。

また、staticファクトリメソッドに渡されたパラメータによって、返却するインスタンスを変えることも可能。  
宣言された戻り値型のサブタイプのクラスであれば、どのクラスでも返却可能であるため、ソフトウェアの保守性やパフォーマンス向上のためにリリースごとに変更可能。

例えば、配列を `List` 型に変換するstaticファクトリメソッドがあるとします。

```java
public static <T> List<T> toList(T [] array){
    if( array.length <= 100 ){
        return new SmallList<T>(array);
    }else{
        return new LargeList<T>(array);
    } 
}
```

配列の要素数によって、`SmallList`もしくは`LargeList`でインスタンス化して返却していますが、  
パフォーマンスの面から`LargeList`は不要と判断された場合

```java
public <T> static List<T> toList(T [] array){
    return new SmallList<T>(array);
}
```

という修正を、後のリリースで行うだけでよくなります。  
ユーザ側は、`List`インターフェースが実装されたインスタンスを返してもらえることだけに関心があるため、内部実装の変更は正直どうでもいいわけです。


#### 4. パラメータ化された型のインスタンス生成の面倒さを低減する

パラメータ化されたクラスのコンストラクタは、インスタンス時に型パラメータを指定する必要があり、面倒。

```java
// Java1.6以前は、２回指定しなければいけなかった。
Map<String, List<String>> map = new HashMap<String, List<String>>();

// Java1.7以降では、型パラメータの指定は１回でいい。
Map<String, List<String>> map = new HashMap<>();
```

これが更に複雑化してくると、インスタンスの生成が苦痛になる。  
そこで、型推定を利用したstaticファクトリメソッドを作成すると楽になる。

```java
public static <K, V> Map<K, V> getInstance() {
    return new HashMap<K, V>();
}
```

型推定を利用することで、コンパイラが型パラメータを見つけ出して解決してくれる。

```java
// Javaのバージョンに関係なく、型パラメータの指定は１回でいい。
Map<String, List<String>> map = HashMap.getInstance();
```

### staticファクトリメソッドの短所

#### 1. publicあるいはprotectedのコンストラクタを持たないクラスのサブクラスを作成できない

publicのstaticファクトリメソッドから返却される、publicではないクラスに対しても同様。  
例えば、コレクションフレームの実装クラスのどれかのサブクラスを作るのは不可能。  
（それはそれでいい。なぜなら、継承ではなくコンポジション（委譲）を利用することを促すから）

#### 2. 他のstaticファクトリメソッドと区別がつかないこと

基本的にクラスのインスタンス化はコンストラクタで行うことが一般的。  
特に初めて使用するAPIなどは、そのクラスがstaticファクトリメソッドを提供していることに気づきにくい。

そのため、ある程度は基準となっている命名規約などに従うことで、上記のデメリットを軽減できる。

| メソッド名      | 意味 |
|---------------|------|
| `valueOf`     | パラメータと同じ値を持つインスタンスを返す。<br/>実質、型変換メソッド。|
| `of`          | 簡潔版`valueOf`。 |
| `getInstance` | パラメータで指定されたインスタンスを返す。(同じ値を持つとは限らない）<br/>シングルトンの場合には、パラメータを受け取らず、唯一のインスタンスを返す。|
| `newInstance` | `getInstance`と類似しているが、`newInstance`はパラメータの値が同じでも異なるインスタンス（参照）を返す。|
| `getType`     | ファクトリメソッドを提供しているクラスと異なるクラスを返却する場合に使用。 |
| `newType`     | ファクトリメソッドを提供しているクラスと異なるクラスを返却する場合に使用。 |


### まとめ

staticファクトリメソッドとpublicコンストラクタは、各使用方法があり、それらの関連する利点を理解すれば得るものがある。  
大抵はstaticファクトリメソッドが好ましい場合が多いので、staticファクトリメソッドを検討することなく、コンストラクタの提供を無意識に行うのは避けるべき。

