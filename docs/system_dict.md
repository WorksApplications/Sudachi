# Sudachi システム辞書作成方法

## 辞書ソース

以下の配布ページにてビルド前のシステム辞書ソースファイルを配布しています：
http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict-raw/

各レキシコンはそれぞれ追加分の語のみを含みます。すなわち、

- small 辞書のビルドには small_lex
- core 辞書のビルドには small_lex と core_lex
- full 辞書のビルドには small_lex, core_lex と full_lex

がそれぞれ必要です。

またビルドに追加で必要となる `matrix.def` ファイルもここから取得できます。

品詞リストファイル `pos.csv` については、[本リポジトリに配置](../src/main/resources/pos.csv)しています。

## バイナリ辞書の作成

ユーザー辞書ソースファイルからバイナリ辞書ファイルを作成します。

`java -Dfile.encoding=UTF-8 -cp sudachi-XX.jar com.worksap.nlp.sudachi.dictionary.DictionaryBuilder -m matrix.def -p pos.csv -o output.dic [-d description] [-s signature] input.csv [additional input.csv...]`

例：

```bash
java -Dfile.encoding=UTF-8 -cp ./build/distributions/sudachi-0.7.6-SNAPSHOT.jar \
    com.worksap.nlp.sudachi.dictionary.DictionaryBuilder \
    -m matrix.def \
    -p ./src/main/resources/pos/csv \
    -o core_20260116_v1.dict \
    -d "sample system core dictionary" \
    small_lex.csv core_lex.csv
```

### 引数

- `-m matrix.def`: （必須）連結コスト行列ファイル
- `-p pos.csv`: （オプショナル）品詞CSVファイル
    - 未指定の場合はソースファイルでの出現順に品詞を採番します
- `-o output.dic`: （必須）出力先バイナリ辞書ファイル名
- `-d description`: （オプショナル）バイナリ辞書のヘッダーに埋め込むコメント文字列
- `-s signature`: （オプショナル）辞書識別用文字列
- `input.csv [additional input.csv...]`: ユーザ辞書ソースファイル
  - core/full 辞書のビルド時は small, core, full の順に与える必要があります
