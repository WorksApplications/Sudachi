---
title: ワークス徳島NLPリソース
description: ワークス徳島人工知能NLP研究所による自然言語処理のためのソフトウェアと言語資源 - 形態素解析器Sudachiや単語ベクトルchiVeなど
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

## コミュニティ

開発者やユーザーの方々が質問したり議論するためのSlackワークスペースを用意しています。

- [https://sudachi-dev.slack.com/](https://sudachi-dev.slack.com/)
- ([こちら](https://join.slack.com/t/sudachi-dev/shared_invite/enQtMzg2NTI2NjYxNTUyLTMyYmNkZWQ0Y2E5NmQxMTI3ZGM3NDU0NzU4NGE1Y2UwYTVmNTViYjJmNDI0MWZiYTg4ODNmMzgxYTQ3ZmI2OWU)から招待を受けてください)


***


## Open Data on AWS

SudachiDictとchiVeのデータは、AWSの[Oepn Data Sponsorship Program](https://registry.opendata.aws/sudachi/)によりホストしていただいています。

### SudachiDict

日本語形態素解析辞書です。詳細は[SudachiDict](https://github.com/WorksApplications/SudachiDict)を参照してください。

- [sudachi-dictionary-20200722-small.zip](https://sudachi.s3-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-20200722-small.zip) ([sudachi-dictionary-latest-small.zip](https://sudachi.s3-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-latest-small.zip))
- [sudachi-dictionary-20200722-core.zip](https://sudachi.s3-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-20200722-core.zip) ([sudachi-dictionary-latest-core.zip](https://sudachi.s3-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-latest-core.zip))
- [sudachi-dictionary-20200722-full.zip](https://sudachi.s3-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-20200722-full.zip) ([sudachi-dictionary-latest-full.zip](https://sudachi.s3-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-latest-full.zip))

### chiVe

事前学習済み日本語単語ベクトルです。詳細は[chiVe](https://github.com/WorksApplications/chiVe)を参照してください。

| 版        | 正規化 | 最低頻度 | 語彙数    | Sudachi辞書           | テキスト                                                                                                      | [gensim](https://radimrehurek.com/gensim/)                                                                           | [Magnitude](https://github.com/plasticityai/magnitude) |
| --------- | ------ | -------- | --------- | --------------------- | ------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------ |
| v1.0 mc5  | x      | 5        | 3,644,628 | 0.1.1-dictionary-full | 12GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.0-mc5-20190314.tar.gz), 4.9GB)   | 4.1GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.0-mc5-20190314_gensim.tar.gz))  | coming soon                                            |
| v1.1 mc5  | o      | 5        | 3,196,481 | 20191030-core         | 11GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5-20200318.tar.gz), 4.4GB)   | 3.6GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5-20200318_gensim.tar.gz))  | coming soon                                            |
| v1.1 mc15 | o      | 15       | 1,452,205 | 20191030-core         | 4.7GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15-20200318.tar.gz), 2.0GB) | 1.7GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15-20200318_gensim.tar.gz)) | coming soon                                            |
| v1.1 mc30 | o      | 30       | 910,424   | 20191030-core         | 3.0GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30-20200318.tar.gz), 1.3GB) | 1.1GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30-20200318_gensim.tar.gz)) | coming soon                                            |
| v1.1 mc90 | o      | 90       | 480,443   | 20191030-core         | 1.6GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90-20200318.tar.gz), 0.7GB) | 0.6GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90-20200318_gensim.tar.gz)) | coming soon                                            |

#### 「A単位語のみ」の資源

| 版                    | 語彙数          | テキスト                                                                                                                  | [gensim](https://radimrehurek.com/gensim/)                                                                                       | [Magnitude](https://github.com/plasticityai/magnitude) |
| --------------------- | --------------- | ------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------ |
| v1.1 mc5 a-unit-only  | 322,094 (10.1%) | 1.1GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5-20200318-a-unit-only.tar.gz), 465MB)  | 403MB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5-20200318-a-unit-only_gensim.tar.gz))  | coming soon                                            |
| v1.1 mc15 a-unit-only | 276,866 (19.1%) | 952MB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15-20200318-a-unit-only.tar.gz), 400MB) | 347MB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15-20200318-a-unit-only_gensim.tar.gz)) | coming soon                                            |
| v1.1 mc30 a-unit-only | 242,658 (26.7%) | 833MB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30-20200318-a-unit-only.tar.gz), 350MB) | 304GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30-20200318-a-unit-only_gensim.tar.gz)) | coming soon                                            |
| v1.1 mc90 a-unit-only | 189,775 (39.5%) | 652MB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90-20200318-a-unit-only.tar.gz), 274MB) | 237MB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90-20200318-a-unit-only_gensim.tar.gz)) | coming soon                                            |
