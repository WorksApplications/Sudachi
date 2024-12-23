ユーザーバイナリ辞書の移行手順

# レガシー形式 -> V1 形式

旧形式（レガシー形式、v0.7 までの形式）の Sudachi バイナリ辞書は v0.8 以降の Sudachi では利用できません。
辞書ソースである lexicon CSV ファイルについても、記述方法が変更されています（v0.8 ではレガシー形式のものも利用可能です）。

本文書ではレガシー形式の Sudachi 辞書から新形式 (V1 形式) に移行するための手順を記述します。

## 辞書ソースファイル (lexicon CSV)

辞書ソースである lexicon CSV ファイルは、標準の記述方法が変更されました。
v0.8 ではレガシー形式のものも利用可能ですが、新形式への移行を推奨します。

本文書では移行に必要な部分のみを扱います。
V1 形式の詳細については [user_dict.md](./user_dict.md)、レガシー形式の詳細については [user_dict_legacy.md](./user_dict_legacy.md) を参照してください。

### 移行のための差分

レガシー形式から V1 形式への移行にあたっては、以下の変更が必要となります。

#### ヘッダー

lexicon の 1 行目にはヘッダー行をおき、記述する項目の種類と順序を指定するようになりました。
項目名については大文字小文字および "\_" の有無は無視して処理されます。

レガシー形式に対応する以下のヘッダー行を lexicon の 1 行目に追加してください。

```csv
SURFACE,LEFT_ID,RIGHT_ID,COST,WRITING,POS1,POS2,POS3,POS4,POS5,POS6,READING_FORM,NORMALIZED_FORM,DICTIONARY_FORM,MODE,SPLIT_A,SPLIT_B,WORD_STRUCTURE
```

#### 空項目

レガシー形式では項目の値がない場合、"\*" を指定していましたが、V1 形式では空文字列とする必要があります。
辞書形 ID (dictionary_form)、A/B 単位分割情報 (split_a, split_b)、第 17 項目（word_structure）について、"\*" を空文字列に置き換えてください。

なお品詞項目の "\*" は空値とは異なるためそのままとしてください。

#### 語参照

正規化形や辞書形、分割情報の項目では他の語への参照を記述する場合があります。
V1 形式における語参照は、参照先の語の「見出し表記、品詞、読み」の組でのみ記述が可能です。
レガシー形式では可能だった行番号による参照は使用できません。

辞書形 ID (dictionary_form)、A/B 単位分割情報 (split_a, split_b)、第 17 項目（word_structure）について、行番号での記述を参照先の語の「見出し表記、品詞、読み」の組に変更してください。
各項目は , （コンマ） で分割し、その項目全体を " （ダブルクォーテーション）で括ります。

例（split_a）：
`...,1/2,...` -> `...,"東京,名詞,固有名詞,地名,一般,*,*,トウキョウ/都,名詞,普通名詞,一般,*,*,*,ト",...`

### 移行用スクリプト

移行用スクリプト [`migrate_legacy_user_lexicon_v1.sh`](../scripts/migrate_legacy_user_lexicon_v1.sh) が利用できます。

スクリプトの実行には、参照するシステムバイナリ辞書（V1 形式）を指定する必要があります。
別途[配布ページ](#TODO)から取得するか、[lexicon CSV](http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict-raw/) からビルドしてください。

レガシー形式のソースファイル内でシステム辞書内の語を参照している場合、以前と異なるシステム辞書を用いると、参照先がずれる可能性があります。
このスクリプトの使用においては、対象 lexicon ファイルの作成時に参照したバージョンのシステム辞書を指定するか、変換後に内容を確認してください。
一度 V1 形式へ変換した lexicon CSV ファイルは任意のバージョンのシステム辞書と共にビルド可能になります。

例： `dict/system.dic` を参照し、`old_lexicon.csv` を変換したものを `new_lexicon.csv` に出力する

```bash
cd /path/to/sudachi
./scripts/migrate_legacy_user_lexicon_v1.sh old_lexicon.csv ./dict/system.dic > new_lexicon.csv
```

## バイナリ辞書

レガシー形式の Sudachi バイナリ辞書は現在の Sudachi では読み込むことができません。
V1 形式のバイナリ辞書として再ビルドする必要があります。

### 1. lexicon CSV からの移行

対象のバイナリ辞書に対応する辞書ソースである lexicon CSV ファイルが存在する場合は、それを元に V1 形式のバイナリ辞書をビルドすることができます。
上記の辞書ソースファイルの移行方法に従い、V1 形式の lexicon ファイルに変換したのち、V1 形式でのビルドを行ってください。

レガシー形式の lexicon ファイルからでもビルド可能ですが、非推奨です。
レガシー形式のソースファイル内でシステム辞書内の語を参照している場合、以前と異なるシステム辞書を用いると、参照先がずれる可能性があります。

ユーザー辞書のビルドでは、参照するシステムバイナリ辞書（V1 形式）を指定する必要があります。
別途[配布ページ](#TODO)から取得してください。

例： `dict/system.dic` を参照し、`user_lexicon.csv` からバイナリユーザー辞書 `new_user.dic` をビルドする

```bash
unzip -d "./sudachi" "./build/distributions/sudachi-executable-1.0.0.zip"
java -Dfile.encoding=UTF-8 \
    -cp ./sudachi/sudachi-1.0.0.jar \
    com.worksap.nlp.sudachi.dictionary.UserDictionaryBuilder \
    -s ./dict/system.dic -o new_user.dic user_lexicon.csv
```

### 2. バイナリ辞書からの移行

バイナリ辞書のみが存在する場合、Sudachi v0.8 では移行ができません。
バージョン v0.7 の Sudachi にて、DictionaryPrinter を用いて辞書ソースファイルへの変換を行ってください。
これはレガシー形式での出力となるため、加えて上記の辞書ソースファイルの移行が必要となります。

ユーザー辞書のプリントでは、参照するシステムバイナリ辞書（レガシー形式）を指定する必要があります。
別途[配布ページ](http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/)から取得してください。

ユーザー辞書内でシステム辞書内の語を参照している場合、ビルド時と異なるシステム辞書を用いると、参照先がずれる可能性があります。
対象バイナリ辞書のビルドの際に参照したシステム辞書を指定するようにしてください。

例： `dict/system.dic` を参照し、バイナリユーザー辞書 `user.dic` の語を `user_lexicon.csv` に出力する

```bash
unzip -d "./sudachi" "./build/distributions/sudachi-executable-0.8.0.zip"
java -Dfile.encoding=UTF-8 \
    -cp ./sudachi/sudachi-0.8.0.jar \
    com.worksap.nlp.sudachi.dictionary.DictionaryPrinter \
    -s ./dict/system.dic -o user_lexicon.csv user.dic \
```
