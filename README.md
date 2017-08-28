# Sudachi

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

https://github.com/s-yata/darts-clone

## System dictionary



-----
# Sudachi

Sudachi は日本語形態素解析器。

## Sudachi の特長

- 複数の分割単位の併用
    + 必要に応じて切り替え
    + 形態素解析と固有表現抽出の融合
- 多数の収録語彙
    + UniDic と NEologd をベースに調整
- 機能のプラグイン化
    + 文字正規化や未知語処理に機能追加が可能
- 同義語辞書との連携
    + 後日公開予定

## インストール

...

## コマンドラインツール

  $ java -jar sudachi-XX.jar [-m mode] [-a] [-d] [-o output] [file...]

### オプション

- m [A|B|C] 分割モード
- a 読み、辞書形も出力
- d デバッグ情報の出力
- o 出力ファイル (指定がない場合は標準出力)


## ライブラリの利用

ライブラリとしての利用は Javadoc を参照してください。


## 分割モード

...

## 機能追加プラグイン

Sudachi では形態素解析の各ステップをフックして処理を差し込むプラグイン機構を
提供しています。

|プラグイン       | 処理例                       |
|:---------------:|:----------------------------:|
|入力テキスト修正 | 異体字統制、表記補正         |
|未知語処理       | 文字種による調整             |
|単語接続処理     | 品詞接続禁制、コスト値上書き |
|出力解修正       | 人名処理、分割粒度調整       |

プラグインを作成することでユーザーが独自の処理をおこなうことができます。

### システム提供プラグイン

システム提供のプラグインとして以下のものを利用できます。

|処理部分         | プラグイン               |                                |
|:---------------:|:------------------------:|:------------------------------:|
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
    + 例) 玉蜀黍 → トウモロコシ
- 異体字
    + 例) 附属 → 付属
- 連濁
    + 例) いとごんにゃく → いとこんにゃく
- 誤用
    + 例) シュミレーション → シミュレーション
- 縮約
    + 例) ちゃあ → ては


## MeCab / kuromoji との比較

|                       | Sudachi | MeCab | kuromoji |
|:---------------------:|:-------:|:-----:|:--------:|
|分割単位の併用         | ○      | ×    | △[^1]   |
|文字正規化、表記正規化 | ○      | ×    | △[^2]   |
|まとめ上げ、補正処理   | ○      | ×    | △[^2]   |
|複数ユーザ辞書の利用   | ○      | ○    | ×       |
|省メモリ               | ◎[^3]  | △    | ○       |
|解析精度               | ○      | ○    | ○       |
|解析速度               | △      | ○    | △       |

[^1]: n-best解による近似
[^2]: Lucene フィルター併用
[^3]: メモリマップ利用による複数 JavaVM での辞書共有


## 今後のリリース予定


## Licenses

...