# 項目12 `Comparable` の実装を検討する

## `Compareble`

`Compareble` は `comparaTo` が唯一のメソッドで、順序比較を許容しておりジェネリックである。  
この `Comparable` を実装することで、そのクラスは **自然な順序** をもっていることを表す。

`Compareble` を実装することで、多くの一般的なアルゴリズム、および、
`Compareble` に依存しているコレクション実装と一緒にクラスを利用できる。

Javaのプラットフォームが提供する値クラス（`String` や `Integer`、`Date` など）のほとんどが、`Compareble` を実装している。
アルファベット順、数値順、年代順などの明らかに自然な順序を持つ値クラスを作成するのであれば、`Compareble` の実装を真剣に検討すべき。

```java
public interface Comparable<T> {
    int compareTo(T t);
}
```

## `compareTo` の一般契約

`comparaTo` の一般契約は `equals` の一般契約と似ており、以下のような内容。

1. 自身と指定されたオブジェクトの順序を比較する。
1. 自身が指定されたオブジェクトよりも小さい、等しい、大きいに応じて、負の整数、ゼロ、正の整数を返す。
1. 指定されたオブジェクトが自身と比較できない場合には `ClassCastException` をスローする。

次の記述では、`sgn(expression)` は数学上の **符号** を表しており、符号関数は `expression` の値が、
負、ゼロ、正のどれであるかに応じて、-1、0、1を返すと定義されている。

* 全ての x と y に対して `sgn(x.compareTo(y)) == -sgn(y.compareTo(x))` を保証しなければいけない。
    * これは、`y.compareTo(x)` が例外をスローする場合にのみ、`x.compareTo(y)` は例外をスローしなければいけないことを意味する。
* 関係が推移的であることも保証しなければならない。
    * すなわち、`(x.compareTo(y) > 0 && y.compareTo(z) > 0)` は、`x.compareTo(z) > 0` であることを意味する。
* すべての z に関して、`x.compareTo(z) == 0` が、`sgn(x.compareTo(z)) == sgn(y.compareTo(z))` を意味することを保証しなければならない。
* `(x.compareTo(y) == 0) == (x.equals(y))` は強く推奨されるが、厳密には必須ではない。
    * `Compareble` を実装し、この条件を破っている場合には明確にそのことを述べるべき。
    * 例えば、`「注意：このクラスは、equalsと一致しない自然な順序をもっています」`。


`equals` と異なり、クラスをまたがって `compareTo` を機能させる必要はない。  
比較されるクラスが別のクラスであれば、`ClassCastException` をスローさせるべき。  


## `compareTo` は `equals` と同様の制約に従う

`compareTo` の一般契約は、`equals` の一般契約と類似している。  
つまり、反射性、対称性、推移性に従わなければならないため、`equals` と同様の注意点が適応される。

`equals` と同様、オブジェクト指向の抽象化の恩恵を諦めず、インスタンス化可能なクラスを拡張して、`compareTo` 契約を守ったまま新たな値要素を追加する方法は、無い。が、回避方法は `equals` と同様のものが適用される。


`compareTo` と `equals` による検査は、同じ結果を返すのが良い。  
`compareTo` と `equals` による検査結果が一致している場合、`compareTo` が課せられる順序は **「equalsと一致している」** と言われる。逆は　**「equalsと矛盾している」**。  
`equals` と矛盾している順序を課している `compareTo` は機能するが、
そのクラスを要素をとして保持するコレクションは、適切に機能しないかもしれない。

例えば、`equals` と矛盾している `compareTo` メソッドを持つ `BigDecimal` を考えてみる。

`new BigDecimal("1.0")` と `new BigDecimal("1.00")` を `HashSet` に追加した時、２つの要素が保持される。
これは、`equals` によって比較されると２つのオブジェクトは等しくないと判定されるため。

しかし、`HashSet` ではなく `TreeSet` に変更すると、保持される要素は1つになる。
これは、`compareTo` による比較で、２つのオブジェクトが等価であると判定されるため。


## `compareTo` の実装

`Comparable` はパラメータ化されているので、`compareTo` は静的に型付けされる。
もし引数が null であれば `NullPointerException` をスローすべき。

フィールドが参照型の場合には、`compareTo` を再帰的に呼び出して順序比較を行う。  
基本データ型であれば、`<` や `>` による順序比較を行う。  

また、浮動小数点に関しては `Double.compare` か `Float.compare` を使用すること。  
そうしないと、単なる関係演算子では `compareTo` の一般契約に従わなくなってしまう。

フィールドの比較順としては、最も意味のあるフィールドから開始し、意味が弱くなる順に行う。  
比較した結果、ゼロ以外の値になるのであれば、そこで比較は終了。その結果を返す。

例として、電話番号を表す `PhoneNumber` の `compreTo` を実装する。

```java
    @Override
    public int compareTo(PhoneNumber o) {

        // 市外局番の比較
        if (areaCode < o.areaCode) return -1;
        if (areaCode > o.areaCode) return 1;

        // 市内局番の前半比較
        if (prefix < o.prefix) return -1;
        if (prefix > o.prefix) return 1;

        // 市内局番の後半を比較
        if (lineNumber < o.lineNumber) return -1;
        if (lineNumber > o.lineNumber) return 1;

        return 0; // 全てのフィールドが正しい
    }
```

戻り値として、符号だけが必要なことがわかっているのであれば、以下のように改善することができる。  
※ `compareTo` の戻り値はあくまで、負、ゼロ、正であればいい。


```java
    // 改善後の compareTo
    @Override
    public int compareTo(PhoneNumber o) {

        // 市外局番との比較
        int areaCodeDiff = areaCode - o.areaCode;
        if (areaCodeDiff != 0) return areaCodeDiff;

        // 市内局番の前半との比較
        int prefixDiff = prefix - o.prefix;
        if (prefixDiff != 0) return prefixDiff;

        // 市内局番の後半との比較
        return lineNumber - o.lineNumber;
    }

```

ただし、この方法を採用する場合には、比較した結果の差が `Integer.MAX_VALUE` 以下になることが保証されている前提。