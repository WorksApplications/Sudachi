---
title: WAP Tokushima NLP Resources
description: Natural language processing software and language resources provided by WAP Tokushima Laboratory of AI and NLP - Tokenizer Sudachi, word embedding chiVe, language model chiTra, and more!
image: Sudachi.png
author: Works Applications
lang: en
---


<p align="center"><img width="70" src="./Sudachi.png" alt="Sudachi logo"></p>

[日本語](index)


# WAP Tokushima NLP Resources

Natural language processing software and language resources provided by [WAP Tokushima Laboratory of AI and NLP](https://www.worksap.co.jp/about/csr/nlp/).


## Software

- [Sudachi](https://github.com/WorksApplications/Sudachi): Japanese Tokenizer (Morphological Analyzer)
- [SudachiPy](https://github.com/WorksApplications/SudachiPy): Python version of Sudachi
- [elasticsearch-sudachi](https://github.com/WorksApplications/elasticsearch-sudachi): Sudachi Plugin for Elasticsearch
- [Kintoki](https://github.com/WorksApplications/kintoki): Dependency Parser
- [jdartsclone](https://github.com/WorksApplications/jdartsclone): TRIE data structure library using Double-Array


## Language Resources

- [SudachiDict](https://github.com/WorksApplications/SudachiDict): Japanese Dictionary for Morphological Analysis
- [SudachiDict Synonym](https://github.com/WorksApplications/SudachiDict/blob/develop/docs/synonyms.md): Japanese Synonym Dictionary
- [chiVe](https://github.com/WorksApplications/chiVe): Japanese Pretrained Word Embeddings
- [chiTra](https://github.com/WorksApplications/sudachiTra): Japanese Pretrained language models

## Community

We have a Slack workspace for developers and users to ask questions and discuss a variety of topics.

- [https://sudachi-dev.slack.com/](https://sudachi-dev.slack.com/)
- (Please get an invitation from [here](https://join.slack.com/t/sudachi-dev/shared_invite/enQtMzg2NTI2NjYxNTUyLTMyYmNkZWQ0Y2E5NmQxMTI3ZGM3NDU0NzU4NGE1Y2UwYTVmNTViYjJmNDI0MWZiYTg4ODNmMzgxYTQ3ZmI2OWU))
  

***


## Open Data on AWS

SudachiDict and chiVe, chiTra data are generously hosted by AWS with their [Oepn Data Sponsorship Program](https://registry.opendata.aws/sudachi/).

### SudachiDict

Japanese dictionaries for morphological analysis. Please refer to [SudachiDict](https://github.com/WorksApplications/SudachiDict) for the detail.

Click [here](http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/) for pre-built dictionaries.


### chiVe

Japanese pretrained word embedding. Please refer to [chiVe](https://github.com/WorksApplications/chiVe) for the detail.

| Version   | Normalized | Min Count | Vocab     | Sudachi | SudachiDict           | Text                                                                                          | [gensim](https://radimrehurek.com/gensim/)                                                           | [Magnitude](https://github.com/plasticityai/magnitude)                                               |
| --------- | ---------- | --------- | --------- | ------- | --------------------- | --------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- |
| v1.3 mc5  | o      | 5        | 2,530,791 | v0.6.8 | 20240109-core         | 3.6GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.3-mc5.tar.gz))  | 2.9GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.3-mc5_gensim.tar.gz))  | -                                                                                                    |
| v1.3 mc15 | o      | 15       | 1,186,019 | v0.6.8 | 20240109-core         | 1.7GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.3-mc15.tar.gz)) | 1.3GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.3-mc15_gensim.tar.gz)) | -                                                                                                    |
| v1.3 mc30 | o      | 30       | 759,011   | v0.6.8 | 20240109-core         | 1.1GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.3-mc30.tar.gz)) | 0.8GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.3-mc30_gensim.tar.gz)) | -                                                                                                    |
| v1.3 mc90 | o      | 90       | 410,533   | v0.6.8 | 20240109-core         | 0.6GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.3-mc90.tar.gz)) | 0.5GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.3-mc90_gensim.tar.gz)) | -                                                                                                    |
| --------- | ------ | -------- | --------- | ------- | --------------------- | --------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- |
| v1.2 mc5  | o      | 5        | 3,197,456 | v0.4.3 | 20200722-core         | 9.2GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc5.tar.gz))  | 3.8GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc5_gensim.tar.gz))  | 5.5GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc5.magnitude))  |
| v1.2 mc15 | o      | 15       | 1,454,280 | v0.4.3 | 20200722-core         | 5.0GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc15.tar.gz)) | 1.7GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc15_gensim.tar.gz)) | 2.4GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc15.magnitude)) |
| v1.2 mc30 | o      | 30       | 912,550   | v0.4.3 | 20200722-core         | 3.1GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc30.tar.gz)) | 1.1GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc30_gensim.tar.gz)) | 1.5GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc30.magnitude)) |
| v1.2 mc90 | o      | 90       | 482,223   | v0.4.3 | 20200722-core         | 1.7GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc90.tar.gz)) | 0.6GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc90_gensim.tar.gz)) | 0.8GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc90.magnitude)) |
| --------- | ------ | -------- | --------- | ------- | --------------------- | --------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- |
| v1.1 mc5  | o      | 5        | 3,196,481 | v0.3.0 | 20191030-core         | 11GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5.tar.gz))   | 3.6GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5_gensim.tar.gz))  | 5.5GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5.magnitude))  |
| v1.1 mc15 | o      | 15       | 1,452,205 | v0.3.0 | 20191030-core         | 4.7GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15.tar.gz)) | 1.7GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15_gensim.tar.gz)) | 2.4GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15.magnitude)) |
| v1.1 mc30 | o      | 30       | 910,424   | v0.3.0 | 20191030-core         | 3.0GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30.tar.gz)) | 1.1GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30_gensim.tar.gz)) | 1.5GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30.magnitude)) |
| v1.1 mc90 | o      | 90       | 480,443   | v0.3.0 | 20191030-core         | 1.6GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90.tar.gz)) | 0.6GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90_gensim.tar.gz)) | 0.8GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90.magnitude)) |
| v1.0 mc5  | x      | 5        | 3,644,628 | v0.1.1 | 0.1.1-dictionary-full | 12GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.0-mc5.tar.gz))   | 4.1GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.0-mc5_gensim.tar.gz))  | 6.3GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.0-mc5.magnitude))  |

#### "A Unit Only" Resources

| Version         | Vocab           | Text                                                                                                | [gensim](https://radimrehurek.com/gensim/)                                                                 | [Magnitude](https://github.com/plasticityai/magnitude)                                                     |
| --------------- | --------------- | --------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| v1.1 mc5 aunit  | 322,094 (10.1%) | 1.1GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5-aunit.tar.gz))  | 0.4GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5-aunit_gensim.tar.gz))  | 0.5GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5-aunit.magnitude))  |
| v1.1 mc15 aunit | 276,866 (19.1%) | 1.0GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15-aunit.tar.gz)) | 0.3GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15-aunit_gensim.tar.gz)) | 0.4GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15-aunit.magnitude)) |
| v1.1 mc30 aunit | 242,658 (26.7%) | 0.8GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30-aunit.tar.gz)) | 0.3GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30-aunit_gensim.tar.gz)) | 0.4GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30-aunit.magnitude)) |
| v1.1 mc90 aunit | 189,775 (39.5%) | 0.7GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90-aunit.tar.gz)) | 0.2GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90-aunit_gensim.tar.gz)) | 0.3GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90-aunit.magnitude)) |


#### Training continuable chiVe

| Version   | [gensim](https://radimrehurek.com/gensim/) (full)                                                         |
| --------- | --------------------------------------------------------------------------------------------------------- |
| v1.3 mc5  | 5.5GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.3-mc5_gensim-full.tar.gz))  |
| v1.3 mc15 | 2.6GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.3-mc15_gensim-full.tar.gz)) |
| v1.3 mc30 | 1.7GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.3-mc30_gensim-full.tar.gz)) |
| v1.3 mc90 | 0.9GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.3-mc90_gensim-full.tar.gz)) |
| --------- | --------------------------------------------------------------------------------------------------------- |
| v1.2 mc5  | 6.7GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc5_gensim-full.tar.gz))  |
| v1.2 mc15 | 3.0GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc15_gensim-full.tar.gz)) |
| v1.2 mc30 | 1.9GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc30_gensim-full.tar.gz)) |
| v1.2 mc90 | 1.0GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.2-mc90_gensim-full.tar.gz)) |


### chiTra

The library for using large-scale pre-trained language models with the Japanese tokenizer [SudachiPy](https://github.com/WorksApplications/sudachi.rs/tree/develop/python).
Please refer to [chiTra](https://github.com/WorksApplications/SudachiTra) for the detail.

| Version | Normalized             | SudachiTra | Sudachi | SudachiDict   | Text         | Pretrained Model                                                                            |
| ------- | ---------------------- | ---------- | ------- | ------------- | ------------ | ------------------------------------------------------------------------------------------- |
| v1.0    | normalized_and_surface | v0.1.7     | 0.6.2   | 20211220-core | NWJC (148GB) | 395 MB ([tar.gz](https://sudachi.s3.ap-northeast-1.amazonaws.com/chitra/chiTra-1.0.tar.gz)) | 
| v1.1    | normalized_nouns       | v0.1.8     | 0.6.6   | 20220729-core | NWJC with additional cleaning (79GB) | 396 MB ([tar.gz](https://sudachi.s3.ap-northeast-1.amazonaws.com/chitra/chiTra-1.1.tar.gz)) |
