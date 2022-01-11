# Sudachi

<p align="center"><a href="https://nlp.worksap.co.jp/"><img width="70" src="./docs/Sudachi.png" alt="Sudachi logo"></a></p>

<p align="center">
    <a href="https://github.com/WorksApplications/Sudachi/actions/workflows/build.yml"><img src="https://github.com/WorksApplications/Sudachi/actions/workflows/build.yml/badge.svg" alt="Build"></a>
    <a href="https://sonarcloud.io/dashboard/index/com.worksap.nlp.sudachi"><img src="https://sonarcloud.io/api/project_badges/measure?project=com.worksap.nlp.sudachi&metric=alert_status" alt="Quality Gate"></a>
</p>

[日本語 README](#sudachi-日本語readme)


Sudachi is Japanese morphological analyzer. Morphological analysis consists
mainly of the following tasks.

- Segmentation
- Part-of-speech tagging
- Normalization

## Tutorial

For a tutorial on installation, please refer to the [tutorial page](/docs/tutorial.md).

For a tutorial on the plugin, please refer to the [plugin tutorial page](/docs/tutorial_plugin.md).

## Features

Sudachi has the following features.

- Multiple-length segmentation
    + You can change the mode of segmentations
    + Extract morphemes and named entities at once
- Large lexicon
    + Based on UniDic and NEologd
- Plugins
    + You can change the behavior of processings
- Work closely with the synonym dictionary
    + We will release the sysnonym dictionary at a later date


## Dictionaries

Sudachi has three types of dictionaries.

- Small: includes only the vocabulary of UniDic
- Core: includes basic vocabulary (default)
- Full: includes miscellaneous proper nouns

Click [here](http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/) for pre-built dictionaries.
For more details, see [SudachiDict](https://github.com/WorksApplications/SudachiDict).

### How to use the small / full dictionary

Run the command line tool with the configuration string

```
$ java -jar sudachi-XX.jar -s '{"systemDict":"system_small.dic"}'
```

## Use on the command line

```
$ java -jar sudachi-XX.jar [-r conf] [-s json] [-m mode] [-a] [-d] [-f] [-o output] [file...]
```

### Options

- -r conf specifies the setting file (overrids -s)
- -s json additional settings (overrids -r)
- -p directory root directory of resources
- -m {A|B|C} specifies the mode of splitting
- -a outputs the dictionary form and the reading form
- -d dump the debug outputs
- -o specifies output file (default: the standard output)
- -t separate words with spaces
- -ts separate words with spaces, and break line for each sentence
- -f ignore errors

### Examples

    $ echo 東京都へ行く | java -jar target/sudachi.jar
    東京都  名詞,固有名詞,地名,一般,*,*     東京都
    へ      助詞,格助詞,*,*,*,*     へ
    行く    動詞,非自立可能,*,*,五段-カ行,終止形-一般       行く
    EOS

    $ echo 東京都へ行く | java -jar target/sudachi.jar -a
    東京都  名詞,固有名詞,地名,一般,*,*     東京都  東京都  トウキョウト
    へ      助詞,格助詞,*,*,*,*     へ      へ      エ
    行く    動詞,非自立可能,*,*,五段-カ行,終止形-一般       行く    行く    イク
    EOS

    $ echo 東京都へ行く | java -jar target/sudachi.jar -m A
    東京    名詞,固有名詞,地名,一般,*,*     東京
    都      名詞,普通名詞,一般,*,*,*        都
    へ      助詞,格助詞,*,*,*,*     へ
    行く    動詞,非自立可能,*,*,五段-カ行,終止形-一般       行く
    EOS

    $ echo 東京都へ行く | java -jar target/sudachi.jar -t
    東京都 へ 行く

## How to use the API

You can find details in the Javadoc.

To compile an application with Sudachi API, declare a dependency on Sudachi in maven project.

```
<dependency>
  <groupId>com.worksap.nlp</groupId>
  <artifactId>sudachi</artifactId>
  <version>0.5.3</version>
</dependency>
```

## The modes of splitting

Sudachi provides three modes of splitting.
In A mode, texts are divided into the shortest units equivalent
to the UniDic short unit. In C mode, it extracts named entities.
In B mode, into the middle units.

The followings are examples in the core dictionary.

    A：選挙/管理/委員/会
    B：選挙/管理/委員会
    C：選挙管理委員会

    A：客室/乗務/員
    B：客室/乗務員
    C：客室乗務員

    A：労働/者/協同/組合
    B：労働者/協同/組合
    C：労働者協同組合

    A：機能/性/食品
    B：機能性/食品
    C：機能性食品

The followings are examples in the full dictionary.

    A：医薬/品/安全/管理/責任/者
    B：医薬品/安全/管理/責任者
    C：医薬品安全管理責任者

    A：消費/者/安全/調査/委員/会
    B：消費者/安全/調査/委員会
    C：消費者安全調査委員会

    A：さっぽろ/テレビ/塔
    B：さっぽろ/テレビ塔
    C：さっぽろテレビ塔

    A：カンヌ/国際/映画/祭
    B：カンヌ/国際/映画祭
    C：カンヌ国際映画祭

In full-text searching, to use A and B can improve precision and recall.

## Plugins

You can use or make plugins which modify the behavior of Sudachi.

|Type of Plugins  | Example                                     |
|:----------------|:--------------------------------------------|
|Modify the Inputs| Character normalization                     |
|Make OOVs        | Considering script styles                   |
|Connect Words    | Inhibition, Overwrite costs                 |
|Modify the Path  | Fix  Person names, Equalization of splitting|

### Prepared Plugins

We prepared following plugins.

|Type of Plugins  | Plugin                   |                                |
|:----------------|:-------------------------|:-------------------------------|
|Modify the Inputs| character normalization  |Full/half-width, Cases, Variants|
|                 | normalization of prolong symbols| Normalize "~", "ー"s   |
|                 | Remove yomigana          | Remove yomigana in parentheses |
|Make OOVs        | Make one character OOVs  | Use as the fallback            |
|                 | MeCab compatible OOVs    |                                |
|Connect Words    | Inhibition               | Specified by part-of-speech    |
|Modify the Path  | Join Katakata OOVs       |                                |
|                 | Join numerics            |                                |
|                 | Equalization of splitting*| Smooth of OOVs and not OOVs   |
|                 | Normalize numerics   | Normalize Kanji numerics and scales|
|                 | Estimate person names*   |                                |

\* will be released at a later date.

## Normalized Form

Sudachi normalize the following variations.

- Okurigana
    + e.g. 打込む → 打ち込む
- Script
    + e.g. かつ丼 → カツ丼
- Variant
    + e.g. 附属 → 付属
- Misspelling
    + e.g. シュミレーション → シミュレーション
- Contracted form
    + e.g. ちゃあ → ては

## Character Normalization

`DefaultInputTextPlugin` normalizes an input text in the following order.

1. To lower case by `Character.toLowerCase()`
2. Unicode normalization by NFKC

When `rewrite.def` has the following descriptions, `DefaultInputTextPlugin` stops the above processing and aplies the followings.

- Ignore

```
# single code point: this character is skipped in character normalization
髙
```

- Replace

```
# rewrite rule: <target> <replacement>
A' Ā
```

If the number of characters increases as a result of character normalization, Sudachi may output morphemes whose length is 0 in the original input text.

## User Dictionary

To create and use your own dictionaries, please refer to [docs/user_dict.md](https://github.com/WorksApplications/Sudachi/blob/develop/docs/user_dict.md).

## Comparison with MeCab and Kuromoji

|                       | Sudachi | MeCab | kuromoji   |
|:----------------------|:--------|:------|:-----------|
|Multiple Segmentation  | Yes     | No    | Limited ^a |
|Normalization          | Yes     | No    | Limited ^b |
|Joining, Correction    | Yes     | No    | Limited ^b |
|Use multiple user dictionary| Yes     | Yes   | No    |
|Saving Memory          | Good ^c | Poor  | Good       |
|Accuracy               | Good    | Good  | Good       |
|Speed                  | Good    | Excellent | Good   |

- ^a: approximation with n-best
- ^b: with Lucene filters
- ^c: memory sharing with multiple Java VMs


## Future Releases

- Speeding up
- Releasing plugins
- Improving the accuracy
- Adding more split informations
- Adding more normalized forms
- Fix reading forms (pronunciation -> Furigana)
- Coodinating segmentations with the synonym dictionary


## Licenses

### Sudachi

Sudachi by Works Applications Co., Ltd. is licensed under the [Apache License, Version2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

   Copyright (c) 2017 Works Applications Co., Ltd.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

### Logo

![Sudachi Logo](docs/Sudachi.png)

This logo or a modified version may be used by anyone to refer to the morphological analyzer Sudachi, but does not indicate endorsement by Works Applications Co., Ltd.

Copyright (c) 2017 Works Applications Co., Ltd.


## Elasticsearch

We release a plug-in for Elasticsearch.

- https://github.com/WorksApplications/elasticsearch-sudachi

## Python

An implementation of Sudachi in Python

- https://github.com/WorksApplications/SudachiPy


## Slack

We have a Slack workspace for developers and users to ask questions and discuss a variety of topics.

- https://sudachi-dev.slack.com/
- (Please get an invitation from [here](https://join.slack.com/t/sudachi-dev/shared_invite/enQtMzg2NTI2NjYxNTUyLTMyYmNkZWQ0Y2E5NmQxMTI3ZGM3NDU0NzU4NGE1Y2UwYTVmNTViYjJmNDI0MWZiYTg4ODNmMzgxYTQ3ZmI2OWU))


## Citing Sudachi

We have published a paper about Sudachi and its language resources; "[Sudachi: a Japanese Tokenizer for Business](http://www.lrec-conf.org/proceedings/lrec2018/summaries/8884.html)" (Takaoka et al., LREC2018).

When citing Sudachi in papers, books, or services, please use the follow BibTex entry;

```
@InProceedings{TAKAOKA18.8884,
  author = {Kazuma Takaoka and Sorami Hisamoto and Noriko Kawahara and Miho Sakamoto and Yoshitaka Uchida and Yuji Matsumoto},
  title = {Sudachi: a Japanese Tokenizer for Business},
  booktitle = {Proceedings of the Eleventh International Conference on Language Resources and Evaluation (LREC 2018)},
  year = {2018},
  month = {may},
  date = {7-12},
  location = {Miyazaki, Japan},
  editor = {Nicoletta Calzolari (Conference chair) and Khalid Choukri and Christopher Cieri and Thierry Declerck and Sara Goggi and Koiti Hasida and Hitoshi Isahara and Bente Maegaard and Joseph Mariani and Hélène Mazo and Asuncion Moreno and Jan Odijk and Stelios Piperidis and Takenobu Tokunaga},
  publisher = {European Language Resources Association (ELRA)},
  address = {Paris, France},
  isbn = {979-10-95546-00-9},
  language = {english}
  }
```


-----
# Sudachi (日本語README)

[English README](#sudachi)

Sudachi は日本語形態素解析器です。形態素解析はおもに以下の3つの処理を
おこないます。

- テキスト分割
- 品詞付与
- 正規化処理

## チュートリアル

インストールのチュートリアルは、[インストールのチュートリアル](/docs/tutorial.md)を参照ください。

プラグインのチュートリアルは、[プラグインのチュートリアル](/docs/tutorial_plugin.md)を参照ください。<br>
プラグイン機構を用いて、分かち書きを実現しています。

## Sudachi の特長

Sudachi は従来の形態素解析器とくらべ、以下のような特長があります。

- 複数の分割単位の併用
    + 必要に応じて切り替え
    + 形態素解析と固有表現抽出の融合
- 多数の収録語彙
    + UniDic と NEologd をベースに調整
- 機能のプラグイン化
    + 文字正規化や未知語処理に機能追加が可能
- 同義語辞書との連携
    + 後日公開予定


## 辞書の取得

Sudachi には3種類の辞書があります。

- Small: UniDic の収録語とその正規化表記、分割単位を収録
- Core: 基本的な語彙を収録 (デフォルト)
- Full: 雑多な固有名詞まで収録

ビルド済みの辞書は[こちら](http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/)で配布しています。
くわしくは [SudachiDict](https://github.com/WorksApplications/SudachiDict) をごらんください。

### スモール/フル辞書の利用方法

コマンドラインツールで設定文字列を指定します

```
$ java -jar sudachi-XX.jar -s '{"systemDict":"system_small.dic"}'
```

## コマンドラインツール

```
$ java -jar sudachi-XX.jar [-r conf] [-s json] [-m mode] [-a] [-d] [-f] [-o output] [file...]
```

### オプション

- -r conf 設定ファイルを指定 (-s と排他)
- -s json デフォルト設定の上書き (-r と排他)
- -p directory リソースの起点となるディレクトリを指定
- -m {A|B|C} 分割モード
- -a 読み、辞書形も出力
- -d デバッグ情報の出力
- -o 出力ファイル (指定がない場合は標準出力)
- -t 単語をスペース区切りで出力
- -ts 単語をスペース区切りで出力、文末で改行を出力
- -f エラーを無視して処理を続行する

### 出力例

    $ echo 東京都へ行く | java -jar target/sudachi.jar
    東京都  名詞,固有名詞,地名,一般,*,*     東京都
    へ      助詞,格助詞,*,*,*,*     へ
    行く    動詞,非自立可能,*,*,五段-カ行,終止形-一般       行く
    EOS

    $ echo 東京都へ行く | java -jar target/sudachi.jar -a
    東京都  名詞,固有名詞,地名,一般,*,*     東京都  東京都  トウキョウト
    へ      助詞,格助詞,*,*,*,*     へ      へ      エ
    行く    動詞,非自立可能,*,*,五段-カ行,終止形-一般       行く    行く    イク
    EOS

    $ echo 東京都へ行く | java -jar target/sudachi.jar -m A
    東京    名詞,固有名詞,地名,一般,*,*     東京
    都      名詞,普通名詞,一般,*,*,*        都
    へ      助詞,格助詞,*,*,*,*     へ
    行く    動詞,非自立可能,*,*,五段-カ行,終止形-一般       行く
    EOS

    $ echo 東京都へ行く | java -jar target/sudachi.jar -t
    東京都 へ 行く


## ライブラリの利用

ライブラリとしての利用は Javadoc を参照してください。

Maven プロジェクトで利用する場合は以下の dependency を追加してください。

```
<dependency>
  <groupId>com.worksap.nlp</groupId>
  <artifactId>sudachi</artifactId>
  <version>0.5.3</version>
</dependency>
```

## 分割モード

Sudachi では短い方から A, B, C の3つの分割モードを提供します。
A は UniDic 短単位相当、C は固有表現相当、B は A, C の中間的な単位です。

以下に例を示します。

(コア辞書利用時)

    A：選挙/管理/委員/会
    B：選挙/管理/委員会
    C：選挙管理委員会

    A：客室/乗務/員
    B：客室/乗務員
    C：客室乗務員

    A：労働/者/協同/組合
    B：労働者/協同/組合
    C：労働者協同組合

    A：機能/性/食品
    B：機能性/食品
    C：機能性食品

(フル辞書利用時)

    A：医薬/品/安全/管理/責任/者
    B：医薬品/安全/管理/責任者
    C：医薬品安全管理責任者

    A：消費/者/安全/調査/委員/会
    B：消費者/安全/調査/委員会
    C：消費者安全調査委員会

    A：さっぽろ/テレビ/塔
    B：さっぽろ/テレビ塔
    C：さっぽろテレビ塔

    A：カンヌ/国際/映画/祭
    B：カンヌ/国際/映画祭
    C：カンヌ国際映画祭

検索用途であれば A と C を併用することで、再現率と適合率を向上させる
ことができます。

## 機能追加プラグイン

Sudachi では形態素解析の各ステップをフックして処理を差し込むプラグイン機構を
提供しています。

|プラグイン       | 処理例                       |
|:----------------|:-----------------------------|
|入力テキスト修正 | 異体字統制、表記補正         |
|未知語処理       | 文字種による調整             |
|単語接続処理     | 品詞接続禁制、コスト値上書き |
|出力解修正       | 人名処理、分割粒度調整       |

プラグインを作成することでユーザーが独自の処理をおこなうことができます。

### システム提供プラグイン

システム提供のプラグインとして以下のものを利用できます。

|処理部分         | プラグイン               |                                |
|:----------------|:-------------------------|:-------------------------------|
|入力テキスト修正 | 文字列正規化             | 全半角、大文字/小文字、異体字  |
|                 |                          | カスタマイズ可能               |
|                 | 長音正規化               | 「~」や長音記号連続の正規化    |
|                 | 読みがな削除             | 括弧内の読み仮名を削除         |
|未知語処理       | 1文字未知語              | フォールバックとして利用       |
|                 | MeCab互換                |                                |
|単語接続処理     | 品詞接続禁制             | カスタマイズ可能               |
|出力解修正       | カタカナ未知語まとめ上げ |                                |
|                 | 数詞まとめ上げ           |                                |
|                 | 分割粒度調整*            | 未知語/既知語の分割粒度の平滑化|
|                 | 数詞正規化               | 漢数詞や位取りの正規化         |
|                 | 人名補正*                | 敬称や前後関係から人名部を推定 |

\* は後日公開予定

## 表記正規化

Sudachi のシステム辞書では以下のような表記正規化を提供します。

- 送り違い
    + 例) 打込む → 打ち込む
- 字種
    + 例) かつ丼 → カツ丼
- 異体字
    + 例) 附属 → 付属
- 誤用
    + 例) シュミレーション → シミュレーション
- 縮約
    + 例) ちゃあ → ては

## 文字正規化

デフォルトで適用されるプラグイン `DefaultInputTextPlugin` で入力文に対して以下の順で正規化をおこないます。

1. `Character.toLowerCase()` をつかった小文字化
2. NFKC をつかった Unicode 正規化

ただし、`rewrite.def` に以下の記述があった場合は上記の処理は適用されず、こちらの処理が優先されます。

- 正規化抑制

```
# コードポイントが1つのみ記述されている場合は、文字正規化を抑制します
髙
```

- 置換

```
# 置換対象文字列 置換先文字列
A' Ā
```

文字正規化の結果、文字数が増えた場合、原文上では長さが0になる形態素が出力されることがあります。

## ユーザー辞書

ユーザー辞書の作成と利用方法については、[docs/user_dict.md](https://github.com/WorksApplications/Sudachi/blob/develop/docs/user_dict.md)をご覧ください。

## MeCab / kuromoji との比較

|                       | Sudachi | MeCab | kuromoji |
|:----------------------|:-------:|:-----:|:--------:|
|分割単位の併用         | ○      | ×    | △ ^1   |
|文字正規化、表記正規化 | ○      | ×    | △ ^2   |
|まとめ上げ、補正処理   | ○      | ×    | △ ^2   |
|複数ユーザ辞書の利用   | ○      | ○    | ×       |
|省メモリ               | ◎ ^3  | △    | ○       |
|解析精度               | ○      | ○    | ○       |
|解析速度               | △      | ○    | △       |

- ^1: n-best解による近似
- ^2: Lucene フィルター併用
- ^3: メモリマップ利用による複数 JavaVM での辞書共有


## 今後のリリースでの対応予定

- 高速化
- 未実装プラグインの整備
- 解析精度向上
- 分割情報の拡充
- 正規化表記の拡充
- 読み情報の整備 (発音読み → ふりがな読み)
- 同義語辞書との連携

## Elasticsearch

Elasticsearch で Sudachi をつかうためのプラグインも公開しています。

- https://github.com/WorksApplications/elasticsearch-sudachi

## Python

Python 版も公開しています。

- https://github.com/WorksApplications/SudachiPy


## Slack

開発者やユーザーの方々が質問したり議論するためのSlackワークスペースを用意しています。

- https://sudachi-dev.slack.com/
- ([こちら](https://join.slack.com/t/sudachi-dev/shared_invite/enQtMzg2NTI2NjYxNTUyLTMyYmNkZWQ0Y2E5NmQxMTI3ZGM3NDU0NzU4NGE1Y2UwYTVmNTViYjJmNDI0MWZiYTg4ODNmMzgxYTQ3ZmI2OWU)から招待を受けてください)


## Sudachiの引用

Sudachiとその言語資源について、論文を発表しています; "[Sudachi: a Japanese Tokenizer for Business](http://www.lrec-conf.org/proceedings/lrec2018/summaries/8884.html)" (Takaoka et al., LREC2018).

Sudachiを論文や書籍、サービスなどで引用される際には、以下のBibTexをご利用ください。

```
@InProceedings{TAKAOKA18.8884,
  author = {Kazuma Takaoka and Sorami Hisamoto and Noriko Kawahara and Miho Sakamoto and Yoshitaka Uchida and Yuji Matsumoto},
  title = {Sudachi: a Japanese Tokenizer for Business},
  booktitle = {Proceedings of the Eleventh International Conference on Language Resources and Evaluation (LREC 2018)},
  year = {2018},
  month = {may},
  date = {7-12},
  location = {Miyazaki, Japan},
  editor = {Nicoletta Calzolari (Conference chair) and Khalid Choukri and Christopher Cieri and Thierry Declerck and Sara Goggi and Koiti Hasida and Hitoshi Isahara and Bente Maegaard and Joseph Mariani and Hélène Mazo and Asuncion Moreno and Jan Odijk and Stelios Piperidis and Takenobu Tokunaga},
  publisher = {European Language Resources Association (ELRA)},
  address = {Paris, France},
  isbn = {979-10-95546-00-9},
  language = {english}
  }
```
