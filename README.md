# Sudachi

[![Build Status](https://travis-ci.org/WorksApplications/Sudachi.svg?branch=develop)](https://travis-ci.org/WorksApplications/Sudachi)

Sudachi is Japanese morphological analyzer. Morphological analysis consists
mainly of the following tasks.

- Segmentation
- Part-of-speech tagging
- Normalization


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


## Use on the command line

    $ java -jar sudachi-XX.jar [-r conf] [-m mode] [-a] [-d] [-o output] [file...]

### Options

- -r conf specifies the setting file
- -m {A|B|C} specifies the mode of splitting
- -a outputs the dictionary form and the reading form
- -d dump the debug outputs
- -o specifies output file (default: the standard output)

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


## How to use the API

You can find details in the Javadoc.


## The modes of splitting

Sudachi provides three modes of splitting.
In A mode, texts are divided into the shortest units equivalent
to the UniDic short unit. In C mode, it extracts named entities.
In B mode, into the middle units.

The followings are an examples.

    A：医薬/品/安全/管理/責任/者
    B：医薬品/安全/管理/責任者
    C：医薬品安全管理責任者

    A：自転/車/安全/整備/士
    B：自転車/安全/整備士
    C：自転車安全整備士

    A：消費/者/安全/調査/委員/会
    B：消費者/安全/調査/委員会
    C：消費者安全調査委員会

    A：新/国立/美術/館
    B：新/国立/美術館
    C：新国立美術館

In full-text searching, to use A and B can imrove precision and recall.


## Plugins

You can use or make plugins which modify the behavior of Sudachi.

|Type of Plugins  | Example                                     |
|:----------------|:--------------------------------------------|
|Modify the Inputs| Character nomalization                      |
|Make OOVs        | Considering script styles                   |
|Connect Words    | Inhibition, Overwrite costs                 |
|Modify the Path  | Fix  Person names, Equalization of splitting|

### Prepared Plugins

We prepared following plugins.

|Type of Plugins  | Plugin                   |                                |
|:----------------|:-------------------------|:-------------------------------|
|Modify the Inputs| character nomalization   |Full/half-width, Cases, Variants|
|                 | normalization of prolong symbols*| Normalize "~", "ー"s   |
|Make OOVs        | Make one character OOVs  | Use as the fallback            |
|                 | MeCab compatible OOVs    |                                |
|Connect Words    | Inhibition               | Specified by part-of-speech    |
|Modify the Path  | Join Katakata OOVs       |                                |
|                 | Join numerics            |                                |
|                 | Equalization of splitting*| Smooth of OOVs and not OOVs   |
|                 | Normalize numerics*  | Normalize Kanji numerics and scales|
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

## Sudachi

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

## Darts-clone

Port of the original Darts-clone to Java. The original one is written
by Susumu Yata.

- https://github.com/s-yata/darts-clone

## System dictionary

This dictionary includes UniDic and a part of NEologd.

- http://unidic.ninjal.ac.jp/
- https://github.com/neologd/mecab-ipadic-neologd

-----
# Sudachi

Sudachi は日本語形態素解析器です。形態素解析はおもに以下の3つの処理を
おこないます。

- テキスト分割
- 品詞付与
- 正規化処理


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


## コマンドラインツール

    $ java -jar sudachi-XX.jar [-r conf] [-m mode] [-a] [-d] [-o output] [file...]

### オプション

- -r conf 設定ファイルを指定
- -m {A|B|C} 分割モード
- -a 読み、辞書形も出力
- -d デバッグ情報の出力
- -o 出力ファイル (指定がない場合は標準出力)

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


## ライブラリの利用

ライブラリとしての利用は Javadoc を参照してください。


## 分割モード

Sudachi では短い方から A, B, C の3つの分割モードを提供します。
A は UniDic 短単位相当、C は固有表現相当、B は A, C の中間的な単位です。

以下に例を示します。

    A：医薬/品/安全/管理/責任/者
    B：医薬品/安全/管理/責任者
    C：医薬品安全管理責任者

    A：自転/車/安全/整備/士
    B：自転車/安全/整備士
    C：自転車安全整備士

    A：消費/者/安全/調査/委員/会
    B：消費者/安全/調査/委員会
    C：消費者安全調査委員会

    A：新/国立/美術/館
    B：新/国立/美術館
    C：新国立美術館

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
|                 | 長音正規化*              | 「~」や長音記号連続の正規化    |
|未知語処理       | 1文字未知語              | フォールバックとして利用       |
|                 | MeCab互換                |                                |
|単語接続処理     | 品詞接続禁制             | カスタマイズ可能               |
|出力解修正       | カタカナ未知語まとめ上げ |                                |
|                 | 数詞まとめ上げ           |                                |
|                 | 分割粒度調整*            | 未知語/既知語の分割粒度の平滑化|
|                 | 数詞正規化*              | 漢数詞や位取りの正規化         |
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
