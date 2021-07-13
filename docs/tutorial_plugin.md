# Sudachi プラグインのチュートリアル

このチュートリアルでは、Sudachi プラグイン機構を使って **分かち書き** を実現する。

※なお、分かち書き機能は最新版で標準実装されている。

## 事前知識

### 分かち書き

分かち書きとは以下のように、文を形態素ごとにスペースで区切ることである。
```
今日は散歩に行く -> 今日 は 散歩 に 行く
```

### Sudachi プラグイン

Sudachi には設定をカスタマイズする機構があり、 `-r` オプションを使い `sudachi.json` を書き換えることで実現できる。

出力フォーマットを指定するクラスの `SimpleMorphemeFormatter` で変更できるポイントは以下のとおりである。
- `delimiter` : 形態素ごとの区切り。デフォルトは `\n`
- `eos` : 文ごとの区切りで End of Sentence の略。デフォルトは `\nEOS\n`
- `columnDelimiter` : 各フィールドごとの区切り。 デフォルトは `\t`

例えば `EOS` 部分を改行コードのみに変更したいときは以下のように指定する。
```sh
$ java -jar target/sudachi-0.5.2-SNAPSHOT.jar -s '{"formatterPlugin":[{"class":"com.worksap.nlp.sudachi.SimpleMorphemeFormatter", "eos": "\n"}]}'
今日は散歩に行く
今日    名詞,普通名詞,副詞可能,*,*,*    今日
は      助詞,係助詞,*,*,*,*     は
散歩    名詞,普通名詞,サ変可能,*,*,*    散歩
に      助詞,格助詞,*,*,*,*     に
行く    動詞,非自立可能,*,*,五段-カ行,終止形-一般       行く
明日は明日の風が吹く
明日    名詞,普通名詞,副詞可能,*,*,*    明日
は      助詞,係助詞,*,*,*,*     は
明日    名詞,普通名詞,副詞可能,*,*,*    明日
の      助詞,格助詞,*,*,*,*     の
風      名詞,普通名詞,一般,*,*,*        風
が      助詞,格助詞,*,*,*,*     が
吹く    動詞,一般,*,*,五段-カ行,終止形-一般     吹く
```

## プラグイン機構を使って分かち書きを実現する

本チュートリアルでは、プラグイン機構を用いて自作の java クラスを指定することで Sudachi の出力を変更してみる。

出力のフォーマットを操作しているのは上述の通り、 `MorphemeFormatterPlugin` クラスを extends した `SimpleMorphemeFormatter` クラスである。

```java
package com.worksap.nlp.sudachi;

import java.io.IOException;
import java.util.Arrays;

/**
 * Provides a formatter for {@link Morpheme}
 *
 * <p>
 * The following is an example of settings.
 * 
 * <pre>
 * {@code
 *   {
 *     "class"   : "com.worksap.nlp.sudachi.SimpleFormatter",
 *     "delimiter"  : "\n",
 *     "eos" : "\nEOS\n",
 *     "columnDelimiter" : "\t"
 *   }
 * }
 * </pre>
 *
 * {@code delimiter} is the delimiter of the morphemes. {@code eos} is printed
 * at the position of EOS. {@code columnDelimiter} is the delimiter of the
 * fields.
 */
public class SimpleMorphemeFormatter extends MorphemeFormatterPlugin {

    protected String columnDelimiter;

    @Override
    public void setUp() throws IOException {
        super.setUp();
        columnDelimiter = settings.getString("columnDelimiter", "\t");
    }

    @Override
    public String formatMorpheme(Morpheme morpheme) {
        String output = morpheme.surface() + columnDelimiter + String.join(",", morpheme.partOfSpeech())
                + columnDelimiter + morpheme.normalizedForm();
        if (showDetails) {
            output += columnDelimiter + morpheme.dictionaryForm() + columnDelimiter + morpheme.readingForm()
                    + columnDelimiter + morpheme.getDictionaryId() + columnDelimiter
                    + Arrays.toString(morpheme.getSynonymGroupIds()) + columnDelimiter
                    + ((morpheme.isOOV()) ? "(OOV)" : "");
        }
        return output;
    }
}
```

今回はこのクラスに代わるクラスを自分で用意して、分かち書きを実現する。


### 1. SurfaceFormatter.java を用意

`src/main/java/com/worksap/nlp/sudachi/` 下に `SurfaceFormatter.java` を作成し、以下の内容を記述する。

```java
/*
 * Copyright (c) 2020 Works Applications Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.worksap.nlp.sudachi;

import java.io.IOException;

public class SurfaceFormatter extends MorphemeFormatterPlugin {

    @Override
    public void setUp() throws IOException {
        super.setUp();
        delimiter = settings.getString("delimiter", " ");
        eosString = settings.getString("eos", "\n");
    }

    @Override
    public String formatMorpheme(Morpheme morpheme) {
        String output = morpheme.surface();
        return output;
    }
}
```

ライセンスのコメント部分については、このパッケージ内に用意する以上必要なものである。<br>
（ `mvn spotless:apply` で自動付与することができる。書きたくない場合はパッケージを分ける必要があるが、今回は対象としない。）

`SimpleMorphemeFormatter.java` と同じように `MorphemeFormatterPlugin` を extends して、作成する。

- `setUp` メソッドで `delimiter` と `eos` をそれぞれ設定する。
   - `delimiter` とは形態素ごとの区切りを指すので、分かち書きを実現するため空白とした。
   - `eos` とは文の末尾を表す End of Sentence の意であり、今回は改行することとした。
- `formatMorpheme` が実際の形態素ごとの出力を返すメソッドなので、表層形のみを返すように記述した。


### 2. ビルド

`mvn install` でビルド。

- 注意1：依存ライブラリがない場合以下を実行
   - `mvn -DoutputDirectory=target dependency:copy-dependencies`
- 注意2：辞書がない場合は実行ディレクトリに辞書（ `system_core.dic` ）を配置
   - http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/
      ```
      $ ls
      LICENSE-2.0.txt  README.md  docs  licenses  pom.xml  src  system_core.dic  target
      ```


### 3. 実行

`-r` オプションを使い、自作クラスである `SurfaceFormatter` を指定して実行。

```
$ java -jar target/sudachi-0.5.2-SNAPSHOT.jar -s '{"formatterPlugin":[{"class":"com.worksap.nlp.sudachi.SurfaceFormatter"}]}'
今日は散歩に行く
今日 は 散歩 に 行く
明日は明日の風が吹く
明日 は 明日 の 風 が 吹く
```

以下のように入力ファイル（ `input.txt` ）を用意して、一気に分かち書きすることもできる。
```
$ cat input.txt
今日は散歩に行く
明日は明日の風が吹く

$ java -jar target/sudachi-0.5.2-SNAPSHOT.jar -s '{"formatterPlugin":[{"class":"com.worksap.nlp.sudachi.SurfaceFormatter"}]}' input.txt
今日 は 散歩 に 行く
明日 は 明日 の 風 が 吹く
```
