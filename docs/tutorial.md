# 日本語形態素解析器 Sudachi チュートリアル

以下のような環境で動作させることを前提に手順を書いていきます。

- Windows 10 バージョン1803以降
- Windows Subsystem for Linux
- Ubuntu 18.04 LTS

もちろん単体の Ubuntu や他の Linux デストリビューションでも同様の手順で動作します。


## ビルド

Maven と Git を利用します。

```
$ sudo apt-get install maven git
```

辞書ソースがおおきいため、Git LFSが必要になります。 

```
$ sudo add-apt-repository ppa:git-core/ppa
$ sudo apt-get update
$ curl -s https://packagecloud.io/install/repositories/github/git-lfs/script.deb.sh | sudo bash
$ sudo apt-get install git-lfs
$ git lfs install
```

ソースコードを取得して、ビルドします。 

```
$ git lfs clone https://github.com/WorksApplications/Sudachi.git
$ cd Sudachi
$ mvn package
```

## コマンドラインツールによる解析
コマンドラインツールを引数なしで起動すると、標準入力から入力をうけつけます。 

```
$ cd target
$ java -jar sudachi-0.1.2-SNAPSHOT.jar
きょうはいい天気ですね。
きょう  名詞,普通名詞,副詞可能,*,*,*    今日
は      助詞,係助詞,*,*,*,*     は
いい    形容詞,非自立可能,*,*,形容詞,連体形-一般        良い
天気    名詞,普通名詞,一般,*,*,*        天気
です    助動詞,*,*,*,助動詞-デス,終止形-一般    です
ね      助詞,終助詞,*,*,*,*     ね
。      補助記号,句点,*,*,*,*   。
EOS
```

分割単位を変えることもできます。(デフォルトはC単位) 

```
$ java -jar sudachi-0.1.2-SNAPSHOT.jar
勤労者財産形成貯蓄
勤労者財産形成貯蓄      名詞,普通名詞,一般,*,*,*        勤労者財産形成貯蓄
EOS
$ java -jar sudachi-0.1.2-SNAPSHOT.jar -m B
勤労者財産形成貯蓄
勤労者  名詞,普通名詞,一般,*,*,*        勤労者
財産    名詞,普通名詞,一般,*,*,*        財産
形成    名詞,普通名詞,サ変可能,*,*,*    形成
貯蓄    名詞,普通名詞,サ変可能,*,*,*    貯蓄
EOS
$ java -jar sudachi-0.1.2-SNAPSHOT.jar -m A
勤労者財産形成貯蓄
勤労    名詞,普通名詞,サ変可能,*,*,*    勤労
者      接尾辞,名詞的,一般,*,*,*        者
財産    名詞,普通名詞,一般,*,*,*        財産
形成    名詞,普通名詞,サ変可能,*,*,*    形成
貯蓄    名詞,普通名詞,サ変可能,*,*,*    貯蓄
EOS
```

Full 辞書を利用する場合は設定ファイルを指定します。 

```
$ java -jar sudachi-0.1.2-SNAPSHOT.jar -r ../src/main/resources/sudachi_fulldict.json
鳩サブレー
鳩サブレー      名詞,固有名詞,一般,*,*,*        鳩サブレー
EOS
```
