---
title: ワークス徳島NLPリソース
description: ワークス徳島人工知能NLP研究所による自然言語処理のためのソフトウェアと言語資源 - 形態素解析器Sudachiや単語ベクトルchiVe、言語モデルchiTraなど
image: Sudachi.png
author: Works Applications
lang: ja
---


<p align="center"><img width="70" src="./Sudachi.png" alt="Sudachi logo"></p>

[English](index.en)


# ワークス徳島NLPリソース

[ワークス徳島人工知能NLP研究所](https://www.worksap.co.jp/about/csr/nlp/) による自然言語処理のためのソフトウェアと言語資源


## ソフトウェア

- [Sudachi](https://github.com/WorksApplications/Sudachi): 日本語形態素解析器
- [SudachiPy](https://github.com/WorksApplications/SudachiPy): Python版Sudachi
- [elasticsearch-sudachi](https://github.com/WorksApplications/elasticsearch-sudachi): Elasticsearch用Sudachiプラグイン
- [Kintoki](https://github.com/WorksApplications/kintoki): 係り受け解析器
- [jdartsclone](https://github.com/WorksApplications/jdartsclone): ダブル配列によるTrieデータ構造


## 言語資源

- [SudachiDict](https://github.com/WorksApplications/SudachiDict): 日本語形態素解析辞書
- [SudachiDict Synonym](https://github.com/WorksApplications/SudachiDict/blob/develop/docs/synonyms.md): 日本語同義語辞書
- [chiVe](https://github.com/WorksApplications/chiVe): 事前学習済み日本語単語ベクトル
- [chiTra](https://github.com/WorksApplications/sudachiTra): 事前学習済み日本語言語モデル

## コミュニティ

開発者やユーザーの方々が質問したり議論するためのSlackワークスペースを用意しています。

- [https://sudachi-dev.slack.com/](https://sudachi-dev.slack.com/)
- ([こちら](https://join.slack.com/t/sudachi-dev/shared_invite/enQtMzg2NTI2NjYxNTUyLTMyYmNkZWQ0Y2E5NmQxMTI3ZGM3NDU0NzU4NGE1Y2UwYTVmNTViYjJmNDI0MWZiYTg4ODNmMzgxYTQ3ZmI2OWU)から招待を受けてください)


***


## Open Data on AWS

SudachiDictとchiVe、chiTraのデータは、AWSの[Oepn Data Sponsorship Program](https://registry.opendata.aws/sudachi/) によりホストしていただいています。

### SudachiDict

日本語形態素解析辞書です。詳細は [SudachiDict](https://github.com/WorksApplications/SudachiDict) を参照してください。

ビルド済みの辞書ファイルは [こちら](http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/) からダウンロードできます。


### chiVe

事前学習済み日本語単語ベクトルです。詳細は [chiVe](https://github.com/WorksApplications/chiVe) を参照してください。

| 版        | 正規化 | 最低頻度 | 語彙数    | Sudachi | Sudachi辞書           | テキスト                                                                                      | [gensim](https://radimrehurek.com/gensim/)                                                           | [Magnitude](https://github.com/plasticityai/magnitude)                                               |
| --------- | ------ | -------- | --------- | ------- | --------------------- | --------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- |
| v1.2 mc5  | o      | 5        | 3,197,456 | v0.4.3 | 20200722-core         | 9.2GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc5.tar.gz))  | 3.8GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc5_gensim.tar.gz))  | 5.5GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc5.magnitude))  |
| v1.2 mc15 | o      | 15       | 1,454,280 | v0.4.3 | 20200722-core         | 5.0GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc15.tar.gz)) | 1.7GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc15_gensim.tar.gz)) | 2.4GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc15.magnitude)) |
| v1.2 mc30 | o      | 30       | 912,550   | v0.4.3 | 20200722-core         | 3.1GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc30.tar.gz)) | 1.1GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc30_gensim.tar.gz)) | 1.5GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc30.magnitude)) |
| v1.2 mc90 | o      | 90       | 482,223   | v0.4.3 | 20200722-core         | 1.7GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc90.tar.gz)) | 0.6GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc90_gensim.tar.gz)) | 0.8GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc90.magnitude)) |
| v1.1 mc5  | o      | 5        | 3,196,481 | v0.3.0 | 20191030-core         | 11GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5.tar.gz))   | 3.6GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5_gensim.tar.gz))  | 5.5GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5.magnitude))  |
| v1.1 mc15 | o      | 15       | 1,452,205 | v0.3.0 | 20191030-core         | 4.7GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15.tar.gz)) | 1.7GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15_gensim.tar.gz)) | 2.4GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15.magnitude)) |
| v1.1 mc30 | o      | 30       | 910,424   | v0.3.0 | 20191030-core         | 3.0GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30.tar.gz)) | 1.1GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30_gensim.tar.gz)) | 1.5GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30.magnitude)) |
| v1.1 mc90 | o      | 90       | 480,443   | v0.3.0 | 20191030-core         | 1.6GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90.tar.gz)) | 0.6GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90_gensim.tar.gz)) | 0.8GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90.magnitude)) |
| v1.0 mc5  | x      | 5        | 3,644,628 | v0.1.1 | 0.1.1-dictionary-full | 12GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.0-mc5.tar.gz))   | 4.1GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.0-mc5_gensim.tar.gz))  | 6.3GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.0-mc5.magnitude))  |

#### 「A単位語のみ」の資源

| 版              | 語彙数          | テキスト                                                                                            | [gensim](https://radimrehurek.com/gensim/)                                                                 | [Magnitude](https://github.com/plasticityai/magnitude)                                                     |
| --------------- | --------------- | --------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| v1.1 mc5 aunit  | 322,094 (10.1%) | 1.1GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5-aunit.tar.gz))  | 0.4GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5-aunit_gensim.tar.gz))  | 0.5GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5-aunit.magnitude))  |
| v1.1 mc15 aunit | 276,866 (19.1%) | 1.0GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15-aunit.tar.gz)) | 0.3GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15-aunit_gensim.tar.gz)) | 0.4GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15-aunit.magnitude)) |
| v1.1 mc30 aunit | 242,658 (26.7%) | 0.8GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30-aunit.tar.gz)) | 0.3GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30-aunit_gensim.tar.gz)) | 0.4GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30-aunit.magnitude)) |
| v1.1 mc90 aunit | 189,775 (39.5%) | 0.7GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90-aunit.tar.gz)) | 0.2GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90-aunit_gensim.tar.gz)) | 0.3GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90-aunit.magnitude)) |


#### 追加学習用のフルモデル

| 版        | [gensim](https://radimrehurek.com/gensim/) (full)                                                         |
| --------- | --------------------------------------------------------------------------------------------------------- |
| v1.2 mc5  | 6.7GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc5_gensim-full.tar.gz))  |
| v1.2 mc15 | 3.0GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc15_gensim-full.tar.gz)) |
| v1.2 mc30 | 1.9GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc30_gensim-full.tar.gz)) |
| v1.2 mc90 | 1.0GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc90_gensim-full.tar.gz)) |


### chiTra

日本語形態素解析器 [SudachiPy](https://github.com/WorksApplications/sudachi.rs/tree/develop/python) で事前学習済みの大規模な言語モデルを利用するためのライブラリです。
詳細は [chiTra](https://github.com/WorksApplications/SudachiTra) を参照ください。

| 版      | 正規化                 | SudachiTra | Sudachi | Sudachi辞書    | テキスト     | 事前学習済みモデル                                                                           |
| ------- | ---------------------- | ---------- | ------- | ------------- | ------------ | ------------------------------------------------------------------------------------------- |
| v1.0    | normalized_and_surface | v0.1.7     | 0.6.2   | 20211220-core | NWJC (148GB) | 395 MB ([tar.gz](https://sudachi.s3.ap-northeast-1.amazonaws.com/chitra/chiTra-1.0.tar.gz)) | 
| v1.1    | normalized_nouns       | v0.1.8     | 0.6.6   | 20220729-core | NWJC with additional cleaning (79GB) | 396 MB ([tar.gz](https://sudachi.s3.ap-northeast-1.amazonaws.com/chitra/chiTra-1.1.tar.gz)) |
