# Sudachi Benchmark

Sudachi に大規模なテキストを解析させ、実行速度の計測やバグの検出を行う。

## Base Scripts

### benchmark_setup.sh

Sudachi のビルドおよび Sudachi 辞書のビルドを行う。

- ビルドした `sudachi-executable-[VERSION].zip` を `../build/distributions/sudachi/` 以下に展開する
- `data/` 以下に `system_small.dic`, `system_core.dic`, `system_full.dic` をビルドする
  - `data/dictdata/` 以下にダウンロードした Sudachi 辞書データを格納する

command: `benchmark_setup.sh [dict_version]`

- `dict_version`: Sudachi 辞書バージョン (default "20240716")

### benchmark_run.sh

指定のテキストファイルを各辞書タイプ・分割単位で解析する。
解析結果は `/dev/null` に出力、対象ファイルや開始/終了時刻情報を `data/benchmark.log` に追記する。

command: `benchmark_run.sh corpus_file`

- `corpus_file`: 解析対象とするテキストファイル

### benchmark_multithread.sh

指定のテキストファイルを解析するスレッドを指定数同時に実行する。
各スレッドは一つの辞書インスタンスから生成した個別のトークナイザーインスタンスを持たせる。
解析結果は `/dev/null` に出力、対象ファイルや開始/終了時刻情報を `data/benchmark.log` に追記する。

command: `benchmark_multithread.sh corpus_file [num_thread [dict_type]]`

- `corpus_file`: 解析対象とするテキストファイル
- `num_thread`: 作成するスレッド数 (default 3)
- `dict_type`: 使用する辞書タイプ (default "small")

## Corpus scripts

### kyoto-leads-corpus.sh

[Kyoto University Web Document Leads Corpus](https://github.com/ku-nlp/KWDLC) を取得し、setup および run を実行する。

command: `kyoto-leads-corpus.sh`

- 引数なし

### jawikipedia.sh

[Wikipedia 日本語版ダンプデータ](https://ja.wikipedia.org/wiki/Wikipedia:%E3%83%87%E3%83%BC%E3%82%BF%E3%83%99%E3%83%BC%E3%82%B9%E3%83%80%E3%82%A6%E3%83%B3%E3%83%AD%E3%83%BC%E3%83%89)を取得し、setup および run を実行する。
サイズが非常に大きいため、先頭から指定サイズのみを対象とする。

- 事前に [wikiextracutor](https://github.com/attardi/wikiextractor) のインストールが必要
- `data/jawiki_[DUMP_DATE]/` 以下にデータを格納する。

command: `jawikipedia.sh [dump_date [size]]`

- `dump_date`: ダンプデータの生成日時 (default "20240801")
- `size`: 使用するテキストのサイズ (default 100M)

### commoncrawl.sh

[CommonCrawl](https://commoncrawl.org/get-started) データを取得し、setup および run を実行する。
サイズが非常に大きいため、指定数のページのみを対象とする。

非日本語のサンプルとして利用するため、言語判別は行わず、また HTML を抽出して使用する。

- 事前に python および [warcio](https://pypi.org/project/warcio/) のインストールが必要
- `data/cc[CRAWL_DATE]/` 以下にデータを格納する。

command: `commoncrawl.sh [crawl_date [file_index [num_records]]]`

- `crawl_date`: クロールデータの生成日時 (CC-MAIN-\*, default "2024-33")
- `file_index`: 使用する WARC ファイルの warc.paths ファイル中の行数 (default 1)
- `num_records`: 使用するレコード数（対象 WARC の先頭から取得） (default 1000)
  - 目安として、2024-33 では 1000 レコードでおよそ 50M
