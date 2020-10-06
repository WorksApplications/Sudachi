<p align="center"><img width="70" src="./Sudachi.png" alt="Sudachi logo"></p>

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


## Data on AWS

SudachiDict and chiVe data are generously hosted by AWS with their [Oepn Data Sponsorship Program](https://aws.amazon.com/opendata/).

### SudachiDict

- [sudachi-dictionary-20200722-small.zip](https://sudachi.s3-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-20200722-small.zip) ([sudachi-dictionary-latest-small.zip](https://sudachi.s3-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-latest-small.zip))
- [sudachi-dictionary-20200722-core.zip](https://sudachi.s3-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-20200722-core.zip) ([sudachi-dictionary-latest-core.zip](https://sudachi.s3-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-latest-core.zip))
- [sudachi-dictionary-20200722-full.zip](https://sudachi.s3-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-20200722-full.zip) ([sudachi-dictionary-latest-full.zip](https://sudachi.s3-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-latest-full.zip))

### chiVe

|Version     | Normalized | Min Count | Vocab      | Binary | Text |SudachiDict            | Download |
|----------|-----|------|---------|-----|--------|---------------------|--------|
|v1.0 mc5  |x    |5     |3,644,628|4.1GB|12GB    |0.1.1-dictionary-full| [tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.0-mc5-20190314.tar.gz) (4.9GB) |
|v1.1 mc5 |o    |5     |3,196,481|3.6GB|11GB    |20191030-core        | [tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5-20200318.tar.gz) (4.4GB) |
|v1.1 mc15|o    |15    |1,452,205|1.7GB|4.7GB   |20191030-core        | [tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15-20200318.tar.gz) (2.0GB) |
|v1.1 mc30|o    |30    |910,424  |1.1GB|3.0GB   |20191030-core        | [tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30-20200318.tar.gz) (1.3GB) |
|v1.1 mc90|o    |90    |480,443  |0.6GB|1.6GB   |20191030-core        | [tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90-20200318.tar.gz) (0.7GB) |

The format is based on the original word2vec.

The training algorithm is the same for both v1.0 and v1.1.

"Normalized" indicates if the text is normalized using the tokenizer Sudachi. For example, words `空き缶`, `空缶`, `空き罐`, `空罐`, `空きカン`, `空きかん` will all be normalized to `空き缶`.

"Min Count" indicates the number of minimum appearance count in the training corpus (`min_count` in [gensim](https://radimrehurek.com/gensim/models/word2vec.html)).

Sudachi version: [v0.1.1](https://github.com/WorksApplications/Sudachi/releases/tag/v0.1.1) for chiVe 1.0 and [v0.3.0](https://github.com/WorksApplications/Sudachi/releases/tag/v0.3.0) for chiVe1.1.


#### "A Unit Only" Resources

These files contain only the [SudachiDict](https://github.com/WorksApplications/SudachiDict) A unit words (Not re-training; Simply excluding B unit words, C unit words, and OOV (Out-of-vocabulary) words from the above original resources).

`v1.1 mc90 a-unit-only` is used for the natural language processing tool [spaCy](https://github.com/explosion/spaCy/)'s Japanese models.


| Version               | Normalized  | Min Count | Vocab           | Text  | SudachiDict   | Download                                                                                                                                                                            |
|-----------------------|-------------|-----------|-----------------|-------|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| v1.1 mc5 a-unit-only  | o           |         5 | 322,094 (10.1%) | 1.1GB | 20191030-core | [tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc5-20200318-a-unit-only.tar.gz) (465MB)   |
| v1.1 mc15 a-unit-only | o           |        15 | 276,866 (19.1%) | 952MB | 20191030-core | [tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc15-20200318-a-unit-only.tar.gz) (400MB) |
| v1.1 mc30 a-unit-only | o           |        30 | 242,658 (26.7%) | 833MB | 20191030-core | [tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc30-20200318-a-unit-only.tar.gz) (350MB) |
| v1.1 mc90 a-unit-only | o           |        90 | 189,775 (39.5%) | 652MB | 20191030-core | [tar.gz](https://sudachi.s3-ap-northeast-1.amazonaws.com/chive/chive-1.1-mc90-20200318-a-unit-only.tar.gz) (274MB) |
