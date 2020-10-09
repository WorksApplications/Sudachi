---
title: WAP Tokushima NLP Resources
description: Natural language processing software and language resources provided by WAP Tokushima Laboratory of AI and NLP - Tokenizer Sudachi, word embedding chiVe, and more!
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
- [SudachiDict Synonym](https://github.com/WorksApplications/SudachiDict/blob/develop/docs/synonyms.md): Japansese Synonym Dictionary
- [chiVe](https://github.com/WorksApplications/chiVe): Japanese Pretrained Word Embedding

## Community

We have a Slack workspace for developers and users to ask questions and discuss a variety of topics.

- [https://sudachi-dev.slack.com/](https://sudachi-dev.slack.com/)
- (Please get an invite from [here](https://join.slack.com/t/sudachi-dev/shared_invite/enQtMzg2NTI2NjYxNTUyLTMyYmNkZWQ0Y2E5NmQxMTI3ZGM3NDU0NzU4NGE1Y2UwYTVmNTViYjJmNDI0MWZiYTg4ODNmMzgxYTQ3ZmI2OWU))
  

***


## Open Data on AWS

SudachiDict and chiVe data are generously hosted by AWS with their [Oepn Data Sponsorship Program](https://registry.opendata.aws/sudachi/).

### SudachiDict

Japanese dictionaries for morphological analysis. Please refer to [SudachiDict](https://github.com/WorksApplications/SudachiDict) for the detail.

- [sudachi-dictionary-20200722-small.zip](https://sudachi.s3-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-20200722-small.zip) ([sudachi-dictionary-latest-small.zip](https://sudachi.s3-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-latest-small.zip))
- [sudachi-dictionary-20200722-core.zip](https://sudachi.s3-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-20200722-core.zip) ([sudachi-dictionary-latest-core.zip](https://sudachi.s3-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-latest-core.zip))
- [sudachi-dictionary-20200722-full.zip](https://sudachi.s3-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-20200722-full.zip) ([sudachi-dictionary-latest-full.zip](https://sudachi.s3-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-latest-full.zip))

### chiVe

Japanese pretrained word embedding. Please refer to [chiVe](https://github.com/WorksApplications/chiVe) for the detail.

| Version   | Normalized | Min Count | Vocab     | SudachiDict           | Text                                                                                                   | [gensim](https://radimrehurek.com/gensim/)                                                                    | [Magnitude](https://github.com/plasticityai/magnitude)                                                        |
| --------- | ---------- | --------- | --------- | --------------------- | ------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------- |
| v1.0 mc5  | x          | 5         | 3,644,628 | 0.1.1-dictionary-full | 12GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.0-mc5-20190314.tar.gz))   | 4.1GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.0-mc5-20190314_gensim.tar.gz))  | 6.7GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.0-mc5-20190314.magnitude))  |
| v1.1 mc5  | o          | 5         | 3,196,481 | 20191030-core         | 11GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5-20200318.tar.gz))   | 3.6GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5-20200318_gensim.tar.gz))  | 6.0GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5-20200318.magnitude))  |
| v1.1 mc15 | o          | 15        | 1,452,205 | 20191030-core         | 4.7GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15-20200318.tar.gz)) | 1.7GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15-20200318_gensim.tar.gz)) | 2.6GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15-20200318.magnitude)) |
| v1.1 mc30 | o          | 30        | 910,424   | 20191030-core         | 3.0GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30-20200318.tar.gz)) | 1.1GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30-20200318_gensim.tar.gz)) | 1.6GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30-20200318.magnitude)) |
| v1.1 mc90 | o          | 90        | 480,443   | 20191030-core         | 1.6GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90-20200318.tar.gz)) | 0.6GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90-20200318_gensim.tar.gz)) | 0.8GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90-20200318.magnitude)) |

#### "A Unit Only" Resources

| Version               | Vocab           | Text                                                                                                               | [gensim](https://radimrehurek.com/gensim/)                                                                                | [Magnitude](https://github.com/plasticityai/magnitude)                                                                    |
| --------------------- | --------------- | ------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------- |
| v1.1 mc5 a-unit-only  | 322,094 (10.1%) | 1.1GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5-20200318-a-unit-only.tar.gz))  | 0.4GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5-20200318-a-unit-only_gensim.tar.gz))  | 0.5GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5-20200318-a-unit-only.magnitude))  |
| v1.1 mc15 a-unit-only | 276,866 (19.1%) | 1.0GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15-20200318-a-unit-only.tar.gz)) | 0.3GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15-20200318-a-unit-only_gensim.tar.gz)) | 0.4GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15-20200318-a-unit-only.magnitude)) |
| v1.1 mc30 a-unit-only | 242,658 (26.7%) | 0.8GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30-20200318-a-unit-only.tar.gz)) | 0.3GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30-20200318-a-unit-only_gensim.tar.gz)) | 0.4GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30-20200318-a-unit-only.magnitude)) |
| v1.1 mc90 a-unit-only | 189,775 (39.5%) | 0.7GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90-20200318-a-unit-only.tar.gz)) | 0.2GB ([tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90-20200318-a-unit-only_gensim.tar.gz)) | 0.3GB ([.magnitude](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90-20200318-a-unit-only.magnitude)) |
