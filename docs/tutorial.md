# 日本語形態素解析器 Sudachi チュートリアル

以下のような環境で動作させることを前提とする。

- Windows 10 バージョン1803以降
- Windows Subsystem for Linux
- Ubuntu 18.04 LTS

単体の Ubuntu や他の Linux デストリビューションでも同様の手順で動作する。


# クイックスタート

ビルド済みの Sudachi ファイルと、ビルド済みの Sudachi 辞書ファイルがあるので、それをダウンロードし、組み合わせて実行する。


## Windows コマンドプロンプトの場合

### 1. Sudachi のビルド済みフォルダをダウンロード
- リリースページに行く
  - https://github.com/WorksApplications/Sudachi/releases/
- 最新のビルド済みファイル（ここでは `sudachi-0.5.1-executable.zip` ）をダウンロード
- 展開
  - ダウンロードした zip ファイルを右クリックし、 `すべて展開` をクリック
     
### 2. Sudachi 辞書をダウンロード
- リリースページに行く
  - http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/
- 最新のビルド済み辞書（ここでは `sudachi-dictionary-20200722-core.zip` ）をダウンロード
  - ここで full, core, small は、それぞれ辞書サイズが大、中、小であることを示している。
- 展開
  - ダウンロードした zip ファイルを右クリックし、 `すべて展開` をクリック
     
### 3. Sudachi 辞書を移動
- ステップ2 で展開したファイルの中の、辞書ファイル（ `system_core.dic` ）をステップ1 で展開したフォルダ（ `sudachi-0.5.1-executable\sudachi-0.5.1` の下）に移動


### 4. コマンドプロンプトから実行

移動
```
> cd sudachi-0.5.1-executable\sudachi-0.5.1
```
実行
```
sudachi-0.5.1-executable\sudachi-0.5.1>java -jar sudachi-0.5.1.jar
国会議事堂
国会議事堂      名詞,固有名詞,一般,*,*,*        国会議事堂
EOS
```
または以下でも可
```
sudachi-0.5.1-executable\sudachi-0.5.1>echo 国会議事堂| java -jar sudachi-0.5.1.jar
国会議事堂      名詞,固有名詞,一般,*,*,*        国会議事堂
EOS
```


## Linux の場合
### 1. Sudachi のビルド済みフォルダをダウンロード
- リリースページに行く
  - https://github.com/WorksApplications/Sudachi/releases/
- 最新のビルド済みファイル（ここでは `sudachi-0.5.1-executable.zip` ）をダウンロード
- 展開
  ```
  $ unzip sudachi-0.5.1-executable.zip
  ```
  
### 2. Sudachi 辞書をダウンロード
- リリースページに行く
  - http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/
- 最新のビルド済み辞書（ここでは `sudachi-dictionary-20200722-core.zip` ）をダウンロード
  - ここで full, core, small は、それぞれ辞書サイズが大、中、小であることを示している。
- 展開
  ```
  $ unzip sudachi-dictionary-20200722-core.zip
  ```
  
### 3. Sudachi 辞書を移動
- ステップ2で展開したファイルの中の、辞書ファイル（ `system_core.dic` ）をステップ1で展開したフォルダ（ `sudachi-0.5.1-executable/sudachi-0.5.1` の下）に移動
  ```
  $ mv sudachi-dictionary-20200722-core/sudachi-dictionary-20200722/system_core.dic sudachi-0.5.1-executable/sudachi-0.5.1/
  ```
  
### 4. 実行
ファイルがある場所に移動
```
$ cd sudachi-0.5.1-executable/sudachi-0.5.1/
```
実行
```
$ java -jar sudachi-0.5.1.jar
国会議事堂
国会議事堂      名詞,固有名詞,一般,*,*,*        国会議事堂
EOS
```
または以下でも可
```
$ echo 国会議事堂 | java -jar sudachi-0.5.1.jar
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
$ mvn clean package
```
この時点で実行しようとすると、依存ライブラリがないという以下のエラーが発生する。
```
$ java -jar target/sudachi-0.5.2-SNAPSHOT.jar
Exception in thread "main" java.lang.NoClassDefFoundError: javax/json/JsonValue
        at com.worksap.nlp.sudachi.JapaneseDictionary.buildSettings(JapaneseDictionary.java:92)
        at com.worksap.nlp.sudachi.SudachiCommandLine.getFormatter(SudachiCommandLine.java:82)
        at com.worksap.nlp.sudachi.SudachiCommandLine.main(SudachiCommandLine.java:196)
Caused by: java.lang.ClassNotFoundException: javax.json.JsonValue
        at java.net.URLClassLoader.findClass(URLClassLoader.java:382)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:418)
        at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:352)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:351)
        ... 3 more
```
以下を実行し依存ライブラリを target に取得
```
$ mvn -DoutputDirectory=target dependency:copy-dependencies
```
この時点で実行すると、辞書がないという以下のエラーが発生する。
```
$ java -jar target/sudachi-0.5.2-SNAPSHOT.jar
Exception in thread "main" java.io.FileNotFoundException: system_core.dic (No such file or directory)
        at java.io.FileInputStream.open0(Native Method)
        at java.io.FileInputStream.open(FileInputStream.java:195)
        at java.io.FileInputStream.<init>(FileInputStream.java:138)
        at java.io.FileInputStream.<init>(FileInputStream.java:93)
        at com.worksap.nlp.sudachi.MMap.map(MMap.java:52)
        at com.worksap.nlp.sudachi.dictionary.BinaryDictionary.<init>(BinaryDictionary.java:33)
        at com.worksap.nlp.sudachi.dictionary.BinaryDictionary.readSystemDictionary(BinaryDictionary.java:54)
        at com.worksap.nlp.sudachi.JapaneseDictionary.readSystemDictionary(JapaneseDictionary.java:109)
        at com.worksap.nlp.sudachi.JapaneseDictionary.<init>(JapaneseDictionary.java:57)
        at com.worksap.nlp.sudachi.DictionaryFactory.create(DictionaryFactory.java:81)
        at com.worksap.nlp.sudachi.SudachiCommandLine.main(SudachiCommandLine.java:202)
```
core 辞書をダウンロードし、実行ディレクトリに配置<br>
http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/
```
$ ls
LICENSE-2.0.txt  README.md  docs  licenses  pom.xml  src  system_core.dic  target
```
実行
```
$ java -jar target/sudachi-0.5.2-SNAPSHOT.jar
国会議事堂
国会議事堂      名詞,固有名詞,一般,*,*,*        国会議事堂
EOS
```
