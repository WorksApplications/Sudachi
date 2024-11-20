# 旧形式のユーザー辞書からの移行手順 (V1)

旧形式（レガシー形式）の Sudachi バイナリ辞書は現在の Sudachi では利用できません。
辞書ソースである lexicon CSV ファイルについても、記述方法が変更されています（旧形式のものも利用可能です）。

本文書では旧型式の Sudachi 辞書から新形式 (V1) に移行するための手順を記述します。

## 辞書ソースファイル (lexicon CSV)

辞書ソースである lexicon CSV ファイルは、標準の記述方法が変更されました。
現在は旧形式のものも利用可能ですが、新形式への移行を推奨します。

形式の詳細については [user_dict.md](./user_dict.md) を参照してください。
本文書では移行に必要な部分のみを扱います。

### 移行のための差分の概要

旧形式から新形式への移行にあたっては、以下の編集が必要となります。

#### ヘッダー

lexicon の 1 行目にヘッダー行を追加し、記述する項目の種類と順序を指定します。
項目名については大文字小文字および "\_" の有無は無視して処理されます。

旧形式に対応するヘッダー行は以下になります。

```csv
SURFACE,LEFT_ID,RIGHT_ID,COST,WRITING,POS1,POS2,POS3,POS4,POS5,POS6,READING_FORM,NORMALIZED_FORM,DICTIONARY_FORM,MODE,SPLIT_A,SPLIT_B,WORD_STRUCTURE
```

#### 空項目

旧形式では項目の値がない場合、"\*" を指定していましたが、新形式では空文字列とする必要があります。
辞書形 ID、A/B/C 単位分割情報、第 17 項目（現 Word_Structure）について、"\*" を空文字列に置き換えます。

なお品詞項目の "\*" は空値とは異なるためそのままとしてください。

#### 語参照

辞書形や分割情報の項目では他の語への参照を記述する場合があります。
新形式における語参照は、参照先の語の「見出し表記、品詞、読み」の組でのみ記述が可能です。

旧形式での行番号による参照は使用できません。

### 移行用スクリプト

移行用スクリプト [`migrate_legacy_user_lexicon_v1.sh`](../scripts/migrate_legacy_user_lexicon_v1.sh) が利用できます。

スクリプトの実行には、参照するシステムバイナリ辞書を指定する必要があります。
別途[配布ページ](http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/)から取得してください。

旧形式のソースファイル内でシステム辞書内の語を参照している場合、以前と異なるシステム辞書を用いると、参照先がずれる可能性があります。
このスクリプトの使用においては、対象 lexicon ファイルの作成時に参照したバージョンのシステム辞書を指定してください。

新形式へ変換した lexicon ファイルは任意のバージョンのシステム辞書と共に使用可能になります。

例： `dict/system.dic` を参照し、`old_lexicon.csv` を変換したものを `new_lexicon.csv` に出力する

```bash
cd /path/to/sudachi
./scripts/migrate_legacy_user_lexicon_v1.sh old_lexicon.csv ./dict/system.dic > new_lexicon.csv
```

## バイナリ辞書

旧形式の Sudachi バイナリ辞書は現在の Sudachi では読み込むことができません。
新形式のバイナリ辞書として再ビルドする必要があります。

### 1. lexicon CSV からの移行

対象のバイナリ辞書に対応する辞書ソースである lexicon CSV ファイルが存在する場合は、それを元に新形式のバイナリ辞書をビルドすることができます。
上記の辞書ソースファイルの移行方法に従い、新形式のソースファイルに変換したのち、新形式でのビルドを行ってください。

旧形式の lexicon ファイルからでもビルド可能ですが、非推奨です。
旧形式のソースファイル内でシステム辞書内の語を参照している場合、以前と異なるシステム辞書を用いると、参照先がずれる可能性があります。

ユーザー辞書のビルドでは、参照するシステムバイナリ辞書を指定する必要があります。
別途[配布ページ](http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/)から取得してください。

例： `dict/system.dic` を参照し、`user_lexicon.csv` からバイナリユーザー辞書 `new_user.dic` をビルドする

```bash
unzip -d "./sudachi" "./build/distributions/sudachi-executable-1.0.0.zip"
java -Dfile.encoding=UTF-8 \
    -cp ./sudachi/sudachi-1.0.0.jar \
    com.worksap.nlp.sudachi.dictionary.UserDictionaryBuilder \
    -s ./dict/system.dic -o new_user.dic user_lexicon.csv
```

### 2. バイナリ辞書からの移行

バイナリ辞書のみが存在する場合、現在の Sudachi バージョンでは移行ができません。
過去バージョンの Sudachi にて、DictionaryPrinter を用いて辞書ソースファイルへの変換を行ってください。
これは旧形式での出力となるため、さらに上記の辞書ソースファイルの移行も必要となります。

ユーザー辞書のプリントでは、参照するシステムバイナリ辞書を指定する必要があります。
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
