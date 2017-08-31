# How to build the system dictionary

1. Download UniDic

Download from the source from the following URL.

    https://ja.osdn.net/projects/unidic/releases/

- unidic-mecab-2.1.2_src.zip

Extract it.

    unzip unidic-mecab-2.1.2_src.zip

2. Build the system dictionary

    java -Xmx1g -Dfile.encoding=UTF-8 -cp ../target/sudachi-0.1-SNAPSHOT.jar;../target/javax.json-1.1.jar com.worksap.nlp.sudachi.dictionary.DictionaryBuilder sudachi_lex.csv unidic-mecab-2.1.2_src/matrix.def ../system.dic
