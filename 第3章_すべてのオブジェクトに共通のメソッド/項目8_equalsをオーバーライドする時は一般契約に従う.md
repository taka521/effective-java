Objectクラスは具象クラスであるにもかかわらず、拡張されるために設計されている  
そのfinalでないメソッド(`equals`, `hashCode`, `toString`, `finalize`)は、すべてオーバーライドされるように設計されており、明示的な**一般契約**を持っている。  
一般契約に従うことは、上記のメソッドをオーバーライドするクラスの責任。  
契約に従わなければ、その契約に依存している(`HashMap`や`HashSet`などの)他のクラスが、契約に従っていないクラスと一緒には適切に機能しなくなる。

# 項目8 equalsをオーバーライドする時は一般契約に従う

## equalsをオーバーライドしないのが正しいケース

`equals`をオーバーライドするのは簡単そうだが、間違ってオーバーライドしてしまう方法は多く存在し、結果的に悲惨なものになる。  
問題を回避する最も簡単な方法は、**`equals`をオーバーライドしない**こと。  
その場合、各インスタンスは自分自身とだけ等しくなる。

以下の条件のいずれかに当てはまるのであれば、`equals`はオーバーライドしないが正しい。

* クラスの個々のインスタンスは、本質的に一意である。
 * `Thread`のように、値よりも能動的な実態を表しているクラスが当てはまる。
* 「論理的等価性」検査を、クラスが提供するかどうかに関心がない。
 * 単純に、等価であるかの検査が必要なければオーバーライドしない。
* スーパークラスが既に`equals`をオーバーライドしており、スーパークラスの振る舞いがこのクラス（サブクラス）に対して適切である。
 * 例えば、`Set`の実装は`AbstractSet`から`equals`の実装を継承、`List`は`AbstractList`、`Map`は`AbstractMap`から継承している。
* クラスが`private`、あるいはパッケージプライベートであり、その`equals`メソッドが決して呼び出されないことが確かである。
 * もし呼び出されないことが分かっていても、いつか偶然呼び出されてしまう場合に備えて、`equals`メソッドは以下のようにオーバーライドされるべき。

```java
@Override
public boolean equals(Object o){
    throw new AssertError(); // メソッドは決して呼び出されない
}
```

## どんな時にequalsをオーバーライドするか

端的に言うと、

* そのクラスがオブジェクトの同一性とは異なる**論理等価性**の概念を持っている。
* 論理等価性の振る舞いを実装するために、スーパークラスが`equals`をオーバーライドしていない時。

に、`equals`をオーバーライドする。  
一般的には　**値クラス(ValueObject)**　がそれに該当する。  
値クラスは`Date`や`Integer`などの値を表現している、単なるクラスのこと。

`equals`メソッドを用いる場合、「論理的に同値であるのか」を知るのを期待しており、「同一オブジェクトであるか」であるかを知るのは期待していない。

## equalsをオーバーラーイドする必要の無いケース

個々の値に対して高々1つのオブジェクトしか存在しないことを保証するために、インスタンス制御を行なっているようなクラス。  
`enum`はこのカテゴリに入る。  
このようなクラスは、論理等価性はオブジェクトの同一性と同じであるため、`equals`をオーバーライドする必要はない。

事実、`enum` の `equals`メソッドでやっていることは同一オブジェクトであるかの判定。

```java
@Override
public final boolean equals(Object other) {
    return this == other;
}
```

## equalsをオーバーライドする場合は一般契約を厳守する

`equals`メソッドをオーバーライドする場合には、一般契約を厳守しなければならない。  
以下は、`Object`の仕様からコピーしてきた契約。(Java8)

> * 反射性(reflexive): null以外の参照値`x`について、`x.equals(x)`はtrueを返します。
> * 対称性(symmetric): null以外の参照値`x`および`y`について、`y.equals(x)`がtrueを返す場合に限り、`x.equals(y)`はtrueを返します。
> * 推移性(transitive): null以外の参照値`x`、`y`、および`z`について、`x.equals(y)`がtrueを返し、`y.equals(z)`がtrueを返す場合、`x.equals(z)`はtrueを返します。
> * 一貫性(consistent): null以外の参照値`x`および`y`について、`x.equals(y)`の複数の呼出しは、このオブジェクトに対する`equals`による比較で使われた情報が変更されていなければ、一貫してtrueを返すか、一貫してfalseを返します。
> * null以外の参照値xについて、`x.equals(null)`はfalseを返します。

これらは無視せず厳守しなければ、プログラムが不規則に振舞ったりクラッシュしたりする可能性もある。  
また、コレクションライブラリはこの一般契約に依存した実装になっている。

## 反射性(reflexive)

単に、オブジェクトが自身と等しくなければならない、と言っているだけ。  
この要求を破った場合、コレクションメソッドの`contains`が、追加されたばかりのインスタンスをコレクションが保持していないと答える。

```java
// 反射性を破ったオブジェクト
ValueObject value = new ValueObject();

List<ValueObject> values = List.of(value);
boolean result = values.contailns(value); // falseを返す。
```

## 対称性(symmetric)

この要求は、いかなる2つのオブジェクトでも、それが等しいかどうかについて合意しなければならない、というもの。

例えば以下のクラスは、大文字小文字を区別しない文字列のクラスです。

```java
public class MyString {
    private final String s;
    
    public MyString(String s){
        if(s == null) throw new NullPointerException();
        this.s = s;
    }
    
    @Override
    public boolean equals(Object o){
        if(o instanceOf MyString) return s.equalsIgnoreCase(((MyString)o).s);
        
        // 一方向の相互作用!
        if(o instanceOf String)   return s.equalsIgnoreCase((String)o);
    }
}
```

このクラスの`equals`メソッドは、通常の文字列とも上手く機能しようとしています。  
ではここで、実際に比較を行ってみます。

```java
MyString myString = new MyString("Apple");
String s = "apple";

myString.equals(s); // true
```

期待通りに `true` が返ります。  
しかし、逆の場合だと...

```java
s.equals(myString); // false
```

`false` が返ります。  
これは、`MyString` の `equals` は通常の文字列については知っていても、`String` の `equals` は大文字小文字を区別しない `MyString` について知らないことが原因。  
これは明らかに対称性を破っています。

この`MyString`をコレクションに入れたと仮定します。

```java
List<MyString> list = new ArrayList<>();
list.add(myString);
```

この次に `list.contains(s)` を呼び出した時、返る値はなんでしょうか。  
答えは**わからない**です。  
現在の実装では`false`が返るようになっているものの、あくまで実装の結果であり、他の実装だと`true`を返す可能性もあるし、例外がスローされる可能性もある。  

**`equals`契約を破ってしまうと、他のオブジェクトがその契約を破っているオブジェクトに直面した時に、どのように振る舞うか全く分からない**。

この問題を取り除くためには、`String`と上手く機能しようとする考えを取り除くだけ。

```java
@Override
public boolean equals(Object o){
    return (o instanceOf MyString) && ((MyString)o).s.equalsIgnoreCase(s);
}
```

## 推移性(transitivity)

**1つ目のオブジェクトが2つ目のオブジェクトと等しく、且つ、2つ目のオブジェクトと3つ目のオブジェクトが等しい時、
1つ目のオブジェクトと3つ目のオブジェクトは等しくなければならない**、というもの


インスタンス可能なクラスに新たな値要素を付加するために、サブクラスを作成するケースを考えてみる。  
まずは親となるインスタンス可能なクラス。

```java
// 位置を表すクラス
public class Point {
    private final int x;
    private final int y;
    
    public Point(int x, int y){
        this.x = x;
        this.y = y;
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceOf Point)) return false
        
        Point p = (Point)o;
        return p.x == x && p.y == y;
    }
}
```

この`Point`クラスに、**色**の概念を付加したサブクラスを作成すると仮定する。

```java
// 位置に加え、色の情報を保持するサブクラス
public class ColorPoint extends Point {
    private Color color;
    
    public ColorPoint(int x, int y, Color color){
        super(x, y);
        this.color = color;
    }
}
```

この時、`equals`メソッドはどのように修正されるべきか。  
特に何もしなければ、実装は`Point`から継承されて、`equals`での比較は色の情報は無視される。  
`equals`契約は破っていないものの、明らかに受け入れられない。

なので、引数が同じ位置と色を持つ場合だけtrueを返すように、次のような`equals`メソッドをオーバライドするとする。

```java
@Override
public boolean equals(Object o){

    if(!(o instanceOf ColorPoint)){
        return false;
    }
    
    return super.equals(o) && ((ColorPoint)o).color == color;
}
```

このメソッドの問題点は２つ。

* `Point` から `ColorPoint` を比較する場合、色の情報が無視されてしまう。
* `ColorPoint` から `Point` を比較する場合、引数の型が正しくない(`ColorPoint`ではない）ため常にfalseになる。

```java
Point point = new Pont(1, 2);
ColorPoint colorPoint = new ColorPoint(1, 2, Color.RED);

point.equals(colorPoint); // Point から ColorPoint を比較する場合：true
colorPoint.equals(point); // ColorPoint から Point を比較する場合：false
```

これは**対称性**を破っていることになる。（ `x.equals(y)` が true を返す場合、`y.equals(x)` も true を返さなければいけない）  
混在比較が行われた場合に、`ColorPoint#equals` が色を無視するようにすることで、問題を解決しようとするかもしれない。

```java
@Override
public boolean equals(Object o){
    if(!(o instanceOf Point)) return false;
    
    // oがPointなら、色を無視した比較を行う。
    if(!(o instanceOf ColorPoint)) return o.equals(this);
    
    // oがColorPointなら、完全な比較を行う。
    return super.equals(o) && (ColorPoint)o).color == this.color;
}
```

この方法だと、対称性は提供しているものの、**推移性を犠牲にしている。**  
推移性とは **1つ目のオブジェクトが2つ目のオブジェクトと等しく、且つ、2つ目のオブジェクトと3つ目のオブジェクトが等しい時、
1つ目のオブジェクトと3つ目のオブジェクトは等しくなければならない** であった。  
要は `x.equals(y)` が true で `y.equals(z)` も true の場合、`y.equals(z)` も true でなければならない。

```java
ColorPoint p1 = new ColorPoint(1, 2, Color.RED);   // 1つ目
Point p2 = new Point(1, 2);                        // 2つ目
ColorPoint p3 = new ColorPoint(1, 2, Color.BLUE);  // 3つ目

p1.equals(p2); // 1つ目と2つ目は座標のみの比較を行い、true
p2.equals(p3); // 2つ目と3つ目は座標のみの比較を行い、true
p1.equals(p3); // 1つ目と3つ目は座標と色の比較を行い、false
```

`Color` と `ColorPoint` の関係性は、明らかに推移性を犠牲にしていると言える。  
この解決方法は...**ない**。  
ここでいう「ない」とは、**インスタンス化可能クラスを拡張し、`equals`契約を守ったまま値要素を追加する方法**が「ない」ということ。

`instanceOf` の代わりに `getClass` での検査を使用することで、equals契約を守ったまま、インスタンス可能なクラスを拡張して要素を追加できる、みたいな話がある。

```java
@Override
public boolean equals(Object o){
    if(o == null || o.getClass() != this.getClass()) return false;
    
    Point p = (Point)o;
    return p.x == this.x && p.y == this.y;
}
```

これは、*オブジェクトが同じクラスの場合だけ等しくなる*という効果があり、推移性を満たすことができている。

```java
ColorPoint cp1 = new ColorPoint(0, 1, Color.RED);
Point p = new Point(0, 1);
ColorPoint cp2 = new ColorPoint(0, 1, Color.GREEN);

cp1.equals(p);   // false
p.equals(cp2);   // false
cp1.equals(cp2); // false
```

しかし、`HashSet` などのコレクションに値を格納した場合は期待通りの動作をしない。

```java
Set<Point> points = new HashSet<>();
points.add(new Point(0, 1));
points.add(new Point(1, 0));

Point p = new ColorPoint(0, 1, Color.RED);
points.contains(p); // false
```

`points.contains(p);` に期待する結果は true であるが、`getClass` を使用した `equals` を実装していると false が返る。  
これは `HashSet` などのコレクションが、要素の比較に `equals` を使用していることに起因する。  

**リスコフの置換原則** というものがあり、*ある型が保有する重要な情報はサブタイプでも維持されるべき* というもの。  
したがって、ある型に定義されたメソッドは、そのサブタイプでも同様に機能すべきということ。  
しかし、上記のように親となるクラスが `getClass` を用いて `equals` を実装してしまうと、リスコフの置換原則を破ってしまう。


このようにインスタンス化可能なクラスを拡張して値要素を追加する満足の出来る方法は**ない**が、、**回避方法**はある。  
`ColorPoint` に `Point` を拡張させる代わりに、`ColorPoint` にprivateの `Point` フィールドを持たせて、
そのカラーポイントと同じ位置のポイントを返すpublicの **ビュー(View)** メソッドを持たせること。

```java
public class ColorPoint {
    private Point point;
    private Color color;
    
    public ColorPoint(int x, int y, Color color){
        if(color == null) thorw new NullPointerException();
        
        this.point = new Point(x, y);
        this.color = color;
    }
    
    /** このカラーポイントのポイントとして、ビューを返す。 */
    public Point asPoint(){
        return this.point;
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceOf ColorPoint)) return false;
        
        ColorPoint cp = (ColorPoint)o;
        return cp.point.equals(this.point) && cp.color.equals(this.color);
    }
}
```

```java
Point p = new Point(0, 1);
ColorPoint cp = new ColorPoint(0, 1);

p.equals(cp.asPoint()); // true
```

こうすることで、`ColorPoint` は `Point` のサブタイプではないので **リスコフの置換原則** に従う必要はない。  
もし `ColorPoint` と `Point` の同値性を比較したいのであれば、ビューとの比較を行えばいい。  
また、対称性と推移性も保証できている。

## 整合性（consitency）

**２つのオブジェクトが等しければ、どちらか片方あるいは両方が変更されない限り、
いつまでも常に等しくあり続けなければならない** のが整合性。

クラスを作成する場合、そのクラスが不変であるべきかどうかを検討する必要がある。  
不変であれば、そのオブジェクトと等しい場合には常に等しく、等しくなければ常に等しくない `equals` を実装すること。

また、不変であるか否かに関係なく、**信頼できない資源に依存する`equals`を実装してはいけない**。  

`java.net.URL` の `equals` が悪い例。  
というのも、`URL#equals` は比較対象の `URL` に関連付けられたホストのIPアドレスに依存している。  
ホスト名をIPアドレスへ変換するには、ネットワークへの接続が必要になることがあり、常に同じ結果になることは保証されていない。   
`URL#equals` は `equals` 契約を破っており、実際に問題も出ているらしい。  

`equals`メソッドは、メモリの中のオブジェクトに対して決定できる計算を行うべき。


## 非null性（non-nullity）

単純に、**全てのオブジェクトはnullと等しく合ってはならない**というのが非null性。  
※実際に、非null性という制約名はないのでEffective Java内での名称

`equals`で、`NullPointerException`をスローされることは一般契約で認められていない。  
基本的に多くのクラスでは、`equals` の中でnullチェックを行っている。

```java
@Override
public boolean equals(Object o){
    if(o == null) return false;
    ...
}
```

また、`equals`では引数を適切な方にキャストする必要があり、`instanceOf`演算子による検査を行わなければならない。

```java
@Override
public boolean equals(Object o){
    if(!(o instanceOf MyObject)) return false;

    MyObject myObject = (MyObject)o;
    ...
}
```

`instanceOf` による検査を怠ると `ClassCastException` がスローされるため、`equals`契約に違反する。  
しかし、`instanceOf`演算子の仕様上、第一オペランドがnullであれば必ず `false` を返すようになっている。  
したがって、`instanceOf` で型検査を行うと、nullチェックも同時に行っていることになるため、明示的なnullチェックは不要になる。


## まとめ

`equals` は以下の順番で実装してけば良い。

1. 引数が自分自身のオブジェクトへの参照であるかを検査する場合には、`==`演算子を使用する。
    * そうであれば `true` を返す。
    * 単なるパフォーマンスの最適化だが、比較するコストが高い場合には有効。
2. 引数が正しい型であるか検査するのに、`instanceOf`演算子を使用する。
    * 正しい型でなければ、`false` を返す。
3. 引数を正しい方にキャストする。
    * `instanceOf`による型検査の後に行われるので、成功することが保証されている。
4. クラスの「意味のある」フィールドに対して、引数のオブジェクトのフィールドが、このオブジェクトの対応するフィールドと一致するか検査する。
    * もし、これらの検査が全て成功したならば `true` を返す。
    * そうでなければ、`false` を返す。
    * floatでもdoubleでもない基本型のフィールドに対しては、`==`による比較。
    * 参照型に対しては、再帰的に `equals` を呼び出す。
    * floatに対しては、`Float.compare` を呼び出す。
    * doubleに対しては、`Double.compare` を呼び出す。
    * 配列に対しては、`Arrays.equals` を使用する。
    * 参照型のフィールドによっては正当な値として null が設定されている可能性がある。
    * その場合には、`(field == null ? o.field == null : field.equals(o))` を行う。
    * または、`(field == o.field || (field != null && field.equals(o)))` の方が速い可能性もある。
    * `equals` メソッドのパフォーマンスは、フィールドが比較される順番に影響される。
    * そのため、比較する順番としては *最も違っているだろう、あるいは比較するのにコストが低い、あるいはその両方であるフィールドを最初に比較すべき*。
    * その際、オブジェクトの論理的状態の一部ではないフィールドを比較してはいけない。
5. `equals` メソッドを書き終えた時に、`equals`メソッドが「対照的であるか」「推移的であるか」「整合的」かという3つの質問をする。
    * 単に質問するだけではダメ。
    * これらの性質を保持していることを保証するために、テストを書くこと。
    * 性質が保持されていない場合には、原因を特定して修正すること。
    * 反射性と非null性も保証していなければならないが、上記の３つを満たしていれば大抵保持している。


```java
@Override
public boolean equals(Object o){
    if( o == this ) return true;
    if( !(o instanceOf MyObject) ) return false;

    MyObject mo = (MyObject)o;
    return this.intField == mo.intField
            && Float.compare(this.floatField, mo.floatField)
            && Double.compare(this.doubleField, mo.doubleField)
            && Arrays.equals(this.arrayField, mo.arrayField);
}
```

## 補足

* `equals`をオーバーライドする場合には、`hashCode` をオーバライドする。
* あまりにも賢くなろうとしないこと。
    * 同値性を調べるのに過度に積極的だと、トラブルに陥りやすい。
* `equals`宣言中のObjectを他の型で置き換えてはいけない。
    * 以下のようなコードを書いてはいけない。
    * `public boolean equals(MyObject o){`
    * `equals`をオーバライドしているのではなく、オーバーロードしているだけ。
    * `equals`を実装する場合は、`@Overrdie`を注釈しましょう。（オーバラードでなければコンパイルエラーになるため）

