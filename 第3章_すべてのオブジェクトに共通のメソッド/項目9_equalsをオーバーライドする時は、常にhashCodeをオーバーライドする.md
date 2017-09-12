# 項目9 equalsをオーバーライドする時は、常にhashCodeをオーバーライドする。

## hashCodeの一般契約

`hashCode` をオーバーライドし忘れによるバグは多い。  
**`equals` をオーバーライドした場合、`hashCode` もオーバーライドしなければならない。**

`equals` にも一般契約があったように、`hashCode` にも一般契約がある。  
これを守らない場合、`HashMap` や `HashSet` 、`Hashtable` などのハッシュ値に基づくコレクションは適切に機能しない。

以下は、`java.lang.Object` のjavadocに記載されている `hashCode` の契約（JavaSE8）

> オブジェクトのハッシュ・コード値を返します。
> このメソッドは、HashMapによって提供されるハッシュ表などの、ハッシュ表の利点のためにサポートされています。  
> hashCodeの一般的な規則は次のとおりです。
> 
> * Javaアプリケーションの実行中に同じオブジェクトに対して複数回呼び出された場合は常に、このオブジェクトに対する `equals` の比較で使用される情報が変更されていなければ、`hashCode` メソッドは常に同じ整数を返す必要があります。
> ただし、この整数は同じアプリケーションの実行ごとに同じである必要はありません。  
> * `equals(Object)` メソッドに従って2つのオブジェクトが等しい場合は、2つの各オブジェクトに対する `hashCode` メソッドの呼出しによって同じ整数の結果が生成される必要があります。  
> * `equals(java.lang.Object)` メソッドに従って2つのオブジェクトが等しくない場合は、2つの各オブジェクトに対する`hashCode` メソッドの呼出しによって異なる整数の結果が生成される必要はありません。
> ただし、プログラマは、等しくないオブジェクトに対して異なる整数の結果を生成すると、ハッシュ表のパフォーマンスが向上する場合があることに気付くはずです。
> 
> クラスObjectによって定義された `hashCode` メソッドは、可能なかぎり、異なるオブジェクトに対して異なる整数を返します。
> (これは通常、オブジェクトの内部アドレスを整数に変換することによって実装されますが、この実装テクニックはJava™プログラミング言語では必要ありません。)

何言ってるかわかんないので要約。

* １つのアプリケーションにおいて、`equals` で使用するプロパティが変更されない場合、`hashCode` が返す値は常に同じであること。
    * ただし、別のアプリケーションでも同じ値である必要はない。
* ２つのオブジェクトが等価である（`equals`による比較がtrue）の場合、２つのオブジェクトが返す `hashCode` の値は同じであること。
* ２つのオブジェクトが等価でない（`equals` による比較がfalse）の場合、2つのオブジェクトが返す `hashCode` の値が異なる必要はない。
    * ただし、等しくないオブジェクトであれば異なる値を返すほうがパフォーマンスが向上する。


`hashCode` をオーバーライドするのを忘れた場合に破られる重要な条項は、2つ目の **「２つのオブジェクトが等価である（`equals`による比較がtrue）の場合、２つのオブジェクトが返す `hashCode` の値は同じであること。」**。


## hashCodeを実装し忘れた場合

以下は、単純な構造をもった `PhoneNumber` クラス。  
`equals` メソッドは一般契約に従って実装されている。

```java
public class PhoneNumber {
    private final short areaCode;
    private final short prefix;
    private final short lineNumber;

    public PhoneNumber(int areaCode, int prefix, int lineNumber){
        rangeCheck(areaCode, 999, "area code");
        rangeCheck(prefix, 999, "prefix");
        rangeCheck(lineNumber, 9999, "line number");

        this.areaCode = (short)areaCode;
        this.prefix = (short)prefix;
        this.lineNumber = (short)lineNumber;
    }

    private static void rangeCheck(int arg, int max, String name){
        if( arg < 0 || arg > max){
            throw new IlligalArgmentException(name + ": " + arg);
        }
    }

    @Override
    public boolean equals(Object o){
        if(o == this) return true;
        if(!(o instanceof PhoneNumber)) return false;
        PhoneNumber pn = (PhoneNumber)o;
        return pn.lineNumber == lineNumber
                && pn.prefix == prefix
                && pn.areaCode == areaCode;
    }

    // hashCodeがないので不完全

    ... // getter, setterは省略
}
```

この `PhoneNumber` を `HashMap` で使用すると仮定してみる。

```java
Map<PhoneNumber, String> map = new HashMap<>();
map.put(new PhoneNumber(900, 800, 7000), "ジョン");
```

ここで、

```java
map.get(new PhoneNumber(900, 800, 7000));
```

を呼び出した時に、期待する結果としては `ジョン` だと思うが、実際は `null` が返る。  
これは2つのインスタンスが関係している。

1つ目のインスタンスは `HashMap` への挿入に使用され、2つ目の等しいインスタンスは検索に使用されている。  
`PhoneNumber` は `hashCode` をオーバライドしていないので、２つの等しいインスタンスは、等しくないハッシュコードを持つ結果となり、`hashCode` 契約を破っている。

```
※Hash系のコレクションは、オブジェクトの検索にhashCodeを使用している。  
で、hashCodeの値が一致した場合には、equalsによる等価性の検査が行われる。  
hashCodeのデフォルト実装はインスタンスに割り当てられたメモリ空間からハッシュ値が生成されるので、
論理的に同一であっても、インスタンスが異なれば、異なるハッシュコードを返すことになる。
```

この問題の修正は、`PhoneNumber` に対して適切な `hashCode` をオーバーライドすること。  
だが、以下のようなメソッドは提供すべきではない。

```java
@Override
public int hashCode(){
    return 40;
}
```

等しいオブジェクトは同じハッシュ値を持つことは保証しているが、**全てのオブジェクトが同じハッシュ値を持つことも保証している**。  
結果何が起こるかというと、すべて同一のハッシュ値を返すため、Hash ではなく LinkList になってしまう。  
つまりは、計算量が大幅に増加して性能劣化につながる。※実質、HashMapとして機能していない。


## hashCodeを実装する

良いハッシュ関数は、等しくないオブジェクトに対しては等しくないハッシュ値を生成する。  
これは `hashCode` 契約の3つ目にあたる。  
理想としては、いかなるインスタンスに対しても、異なるハッシュ値を生成させたいが、非常に困難。  
が、適度に近似するのは、それほど難しくない。

1. 何らかの0ではない定数（例えば17）を、`result` という int 型の変数に格納する。
1. オブジェクト内の意味のあるフィールドf（`equals` で使用されるフィールド）に対して、次のことをする。
    * そのフィールドに対する int のハッシュコード `c` を計算する。
        * fが boolean ならば、`(f ? 0 : 1)` を計算する。
        * fが byte, char, short, int のどれかであれば、`(int)f` を計算する。
        * fが long ならば、`(int)(f >>> 32))` を計算する。
        * fが float ならば、`Float.floatToIntBits(f)` を計算する。
        * fが double ならば、`Double.doubleToIntBits(f)` を計算し、結果の long を int へキャストする。
        * fが 参照型 で、そのクラスの `equals` でそのフィールドを `equals` の再帰的に呼び出しで比較しているならば、そのフィールドに対して `hashCode` を再帰的に呼び出す。もしフィールドが null なら、0を返す。
        * fが 配列 であれば、個々の要素をフィールドとして扱い、再帰的に上記の規則を適用して、intの値を取得する。（`Arrays.hashCode` を使用することも1つの手）
    * 計算された `c` を次のように `result` に入れる。
        * `result = 31 * result + c;`
1. `result` を返す。
1. `hashCode` を実装したら、等しいインスタンスが等しいハッシュ値を返すかどうか自問し、単体テストを書く。

`PhoneNumber` に対して `hashCode` を実装してみる。

```java
@Override
public int hashCode() {
    int result = 17;
    result = 31 * result + areaCode;
    result = 31 * result + prefix;
    result = 31 + result + lineNumber;
    return result;
}
```

ハッシュコードの計算から冗長なフィールドを除外するのは問題ないが、**`equals` で使用していないフィールドを除外するのは必須**。  
除外し忘れると、2つ目の契約を破ることになってしまう。


## hashCodeの実装について

### `result` の初期値が 0 でない値の理由。

仮に `result` の初期値を 0 にした場合、各フィールドのハッシュコード `c` が全て 0 になってしまうと、
異なるオブジェクトと衝突する可能性が増加してしまう。  
今回初期値として設定した 17 は、任意値。

### 31で乗算している理由

まず乗算することで、ハッシュ値はフィールドの順番に依存することになるため、
同じようなフィールドを複数持つクラスの場合には衝突する可能性が減る。  
また、31である理由としては奇数の素数であるため。  
（ちなみに、偶数の素数は2だけだが、2を使うと衝突が増える）

もしも偶数による乗算の場合、オーバーフローした場合に情報が失われる。  
素数を利用しているのは単なる伝統。  
31による乗算は、JVMによりシフトと減算で最適化されるため。

```
31 * i == (i << 5) - i
```

## ハッシュコードをキャッシュする

もしもクラスが不変であり、ハッシュコードを計算するコストが高い場合は、
オブジェクト内にハッシュコードをキャッシュしておくと良い。

キャッシュするタイミングとしては、インスタンス化された時か最初に `hashCode` が呼ばれた時。  
後者は俗に言う **遅延初期化** 。

```java
private volatile int hashCode; // 遅延初期化でキャッシュされたhashCode

@Override
public int hashCode(){
    int result = hashCode;
    if(result == 0){
        result = 17;
        result = 31 * result + areaCode;
        result = 31 * result + prefix;
        result = 31 * result * lineNumber;
        hashCode = result;
    }
    return result;
}
```

## hashCodeの最適化について

パフォーマンスを向上させるために、ハッシュコードの計算からオブジェクトの意味のある部分を排除してはいけない。  
結果的に速くなるかもしれないが、ハッシュ関数としての品質は悪くなる。

また、String, Integer, Dateなどのライブラリは `hashCode` の戻り値として厳密な値を明記している。  
これは**一般的には良い考え方ではない**。  
将来的に、ハッシュ関数の改修が困難になってしまう。