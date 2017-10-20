# Sudachi
Sudachi is Japanese morphological analyzer.

# Build

1. Build and install Sudachi
2. Copy files

    ```sh
    $ cp ../src/main/resources/*.def src/test/resources/com/worksap/nlp/lucene/sudachi/ja
    $ cp ../target/system.dic src/test/resources/com/worksap/nlp/lucene/sudachi/ja
    ```

3. Build

    ```sh
   $ mvn package
   ```

# Installation


# Configuration
- tokenizer: Select tokenizer.(sudachi)(string)  
- mode: Select mode.(normal or search or extended)(string, default: search)  
	- normal: Regular segmentataion.  
	- search: Use a heuristic to do additional
 segmentation useful for search.  
	- extended: Similar to search mode, but also unigram unknown words. (experimental)  
- discard\_punctuation: Select to discard punctuation or not.(bool, default: true)  
- settings\_path: Sudachi setting file path. The path may be absolute or relative; relative paths are resolved with respect to ES\_HOME. (string, default: null)  
- resources_path: Sudachi dictionary path. The path may be absolute or relative; relative paths are resolved with respect to ES\_HOME. (string, default: null)  

**[Example]**

    "tokenizer": {
      "mytokenizer": {
        "type": "sudachi_tokenizer",
        "mode": "search",
        "discard_punctuation": true,
        "settings_path": "sudachiSettings.json",
        "resources_path": "config"
      }
    }

# Corresponding Filter
- sudachi\_part\_of\_speech: Exclude the specified part of speech.  
- sudachi\_ja\_stop: Exclued the specified word.  
- sudachi\_baseform: Convert verbs and adjectives to the dictionary form.  
- sudachi\_readingform: Convert to katakana or romaji reading.  

# License
Copyright (c) 2017 Works Applications Co., Ltd.  
Originally under elasticsearch, https://www.elastic.co/jp/products/elasticsearch  
Originally under lucene, https://lucene.apache.org/

# Sudachi
Sudachi は日本語用の形態素解析器です。

# ビルド方法

1. Sudachi をビルドし、インストールします
2. Sudachi の構成ファイルをコピーします

    ```sh
    $ cp ../src/main/resources/*.def src/test/resources/com/worksap/nlp/lucene/sudachi/ja
    $ cp ../target/system.dic src/test/resources/com/worksap/nlp/lucene/sudachi/ja
    ```

3. プラグインをビルドします

    ```sh
    $ mvn package
    ```

# インストール方法


# 設定
- tokenizer: 利用するトークナイザを選択します。(sudachi)(string)  
- mode: 検索モードを選択します。(normal, search, extended から1つ選択できます。)(string, default: search)  
	- normal: 通常のテキスト分割を行います。  
	- search: 通常のテキスト分割に加え、検索で利用しやすい形にテキスト分割します。  
	- extended: searchモードで実施するテキスト分割に加え、未知語に対し文字unigramでテキスト分割します。  
- discard\_punctuation: 句読点を取り除くかどうかを選択します。(bool, default: true)  
- settings\_path: Sudachiの設定ファイルが配置されているファイルパスを設定します。絶対パス、相対パスともに設定可能です。相対パスの場合はES\_HOMEを基準にします。(string, default: null)  
- resources_path: Sudachiで利用する辞書ファイルのディクショナリパスを設定します。絶対パス、相対パスともに設定可能です。相対パスの場合はES\_HOMEを基準にします。(string, default: null)  

**[例]**

    "tokenizer": {
      "mytokenizer": {
        "type": "sudachi_tokenizer",
        "mode": "search",
        "discard_punctuation": true,
        "settings_path": "sudachiSettings.json",
        "resources_path": "config"
      }
    }

# 対応フィルター
- sudachi\_part\_of\_speech: テキスト分割された結果から特定の品詞を排除することができます。  
- sudachi\_ja\_stop: テキスト分割された結果から特定の語句を排除することができます。  
- sudachi\_baseform: テキスト分割された動詞や形容詞を終止形に変換します。  
- sudachi\_readingform: テキスト分割された結果をカタカナやローマ字に変換します。  

# ライセンス
Copyright (c) 2017 Works Applications Co., Ltd.  
Originally under elasticsearch, https://www.elastic.co/jp/products/elasticsearch  
Originally under lucene, https://lucene.apache.org/
