# 日本語形態素解析器 Sudachi チュートリアル

以下のような環境で動作させることを前提とする。

- Windows 10 バージョン1803以降
- Windows Subsystem for Linux
- Ubuntu 18.04 LTS

単体の Ubuntu や他の Linux デストリビューションでも同様の手順で動作する。


# クイックスタート

Sudachi の実行にはビルド済みの配布パッケージとビルド済みの辞書バイナリが必要になる。
それぞれをダウンロードして実行する。

**注意** Sudachi v0.8.2 にて辞書のバイナリ形式が変更されている。v0.8.2 以降を使用する場合は、V1 形式、v0.8.1 以前を使用する場合は V0 形式の辞書バイナリが必要になる。以下では Sudachi v0.8.2 の使用を前提に説明を行う。
これについての詳細は[バージョン移行ガイド](./migration_guide.md)を参照すること。


## Windows コマンドプロンプトの場合

### 1. Sudachi のビルド済みフォルダをダウンロード
- リリースページに行く
  - https://github.com/WorksApplications/Sudachi/releases/
- 最新のビルド済みファイル（ここでは `sudachi-0.8.2-executable.zip` ）をダウンロード
- 展開
  - ダウンロードした zip ファイルを右クリックし、 `すべて展開` をクリック
    - 以下では展開先フォルダ名を `sudachi-0.8.2-executable\` としたものとする

### 2. Sudachi 辞書をダウンロード
- リリースページに行く
  - http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/v1
  - V0 形式バイナリを取得したい場合は http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/v0 へ移動する
- 最新のビルド済み辞書（ここでは `sudachi-dictionary-20260723-core.zip` ）をダウンロード
  - ここで full, core, small は、それぞれ辞書サイズが大、中、小であることを示している。
- 展開
  - ダウンロードした zip ファイルを右クリックし、 `すべて展開` をクリック

### 3. Sudachi 辞書を移動
- ステップ2 で展開したファイルの中の、辞書ファイル（ `system_core.dic` ）をステップ1 で展開したフォルダ（ `sudachi-0.8.2-executable\` の下）に移動


### 4. コマンドプロンプトから実行

移動
```
> cd sudachi-0.8.2-executable\
```
実行
```
sudachi-0.8.2-executable>java -jar sudachi-0.8.2.jar
国会議事堂
国会議事堂      名詞,固有名詞,一般,*,*,*        国会議事堂
EOS
```

または以下でも可
```
sudachi-0.8.2-executable>echo 国会議事堂| java -jar sudachi-0.8.2.jar
国会議事堂      名詞,固有名詞,一般,*,*,*        国会議事堂
EOS
```

small または full 辞書は辞書ファイル名が異なるため、使用時にはその指定が必要になる
```
sudachi-0.8.2-executable>echo 国会議事堂| java -jar sudachi-0.8.2.jar --systemDict system_full.dic
国会議事堂      名詞,固有名詞,一般,*,*,*        国会議事堂
EOS
```


## Linux の場合
### 1. Sudachi のビルド済みフォルダをダウンロード
- リリースページに行く
  - https://github.com/WorksApplications/Sudachi/releases/
- 最新のビルド済みファイル（ここでは `sudachi-0.8.2-executable.zip` ）をダウンロード
- 展開
  ```
  $ unzip sudachi-0.8.2-executable.zip
  ```

### 2. Sudachi 辞書をダウンロード
- リリースページに行く
  - http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/v1
  - V0 形式バイナリを取得したい場合は http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/v0 へ移動する
- 最新のビルド済み辞書（ここでは `sudachi-dictionary-20260723-core.zip` ）をダウンロード
  - ここで full, core, small は、それぞれ辞書サイズが大、中、小であることを示している。
- 展開
  ```
  $ unzip sudachi-dictionary-20260723-core.zip
  ```

### 3. Sudachi 辞書を移動
- ステップ2で展開したファイルの中の、辞書ファイル（ `system_core.dic` ）をステップ1で展開したフォルダ（ `$PWD` の下）に移動
  - 
  ```
  $ mv sudachi-dictionary-20260723/system_core.dic ./
  ```

### 4. 実行

実行
```
$ java -jar sudachi-0.8.2.jar
国会議事堂
国会議事堂      名詞,固有名詞,一般,*,*,*        国会議事堂
EOS
```

または以下でも可
```
$ echo 国会議事堂 | java -jar sudachi-0.8.2.jar
国会議事堂      名詞,固有名詞,一般,*,*,*        国会議事堂
EOS
```

small または full 辞書は辞書ファイル名が異なるため、使用時にはその指定が必要になる
```
$ echo 国会議事堂 | java -jar sudachi-0.8.2.jar --systemDict system_full.dic
国会議事堂      名詞,固有名詞,一般,*,*,*        国会議事堂
EOS
```

# ビルドから実行する場合

## Linux の場合

```
$ git clone https://github.com/WorksApplications/Sudachi
$ cd Sudachi/
```

ビルド

```
$ ./gradlew build
```

配布用アーカイブの展開

```
$ unzip build/distributions/sudachi-executable-0.8.2.zip -d ./target
# もしくは
$ mkdir ./target
$ tar -xf build/distributions/sudachi-executable-0.8.2.tar --directory ./target
```

この時点で実行すると、辞書がないという以下のエラーが発生する。

```
$ java -jar target/sudachi-0.8.2.jar
Exception in thread "main" java.lang.IllegalArgumentException: Failed to resolve file: system_core.dic
Tried roots: [Classpath{prefix=}, Filesystem{base=}]
        at com.worksap.nlp.sudachi.Config$Resource$NotFound.makeException(Config.java:1082)
        at com.worksap.nlp.sudachi.Config$Resource$NotFound.consume(Config.java:1062)
        at com.worksap.nlp.sudachi.dictionary.BinaryDictionary.loadSystem(BinaryDictionary.java:81)
        at com.worksap.nlp.sudachi.JapaneseDictionary.setupSystemDictionary(JapaneseDictionary.java:86)
        at com.worksap.nlp.sudachi.JapaneseDictionary.<init>(JapaneseDictionary.java:52)
        at com.worksap.nlp.sudachi.DictionaryFactory.create(DictionaryFactory.java:58)
        at com.worksap.nlp.sudachi.SudachiCommandLine.main(SudachiCommandLine.java:313)
```

上と同様に core 辞書をダウンロードし、実行ディレクトリに配置
http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/

```
$ ls
... system_core.dic ...
```

実行

```
$ java -jar target/sudachi-0.8.2.jar
国会議事堂
国会議事堂      名詞,固有名詞,一般,*,*,*        国会議事堂
EOS
```

small または full 辞書は辞書ファイル名が異なるため、使用時にはその指定が必要になる
```
$ java -jar target/sudachi-0.8.2.jar --systemDict system_full.dic
国会議事堂      名詞,固有名詞,一般,*,*,*        国会議事堂
EOS
```
