# Sudachi
Sudachi is Japanese morphological analyzer.

# Build

1. Build and install Sudachi.
2. Copy files [system.dic / *.def / sudachiSettings.json] in package of Sudachi and place them under  "src/test/resources/com/worksap/nlp/lucene/sudachi/ja".
3. Build analysis-sudachi.
```
   $ mvn package
```

# Installation
Follow the steps below to install.
1. Change the current directory "/usr/share/elasticsearch".
2. Place the zip file created with "Build" on the moved directory.
3. Command "sudo bin/elasticsearch-plugin install file///usr/share/elasticsearch/<zipfile-name>"
4. Place files [system.dic / *.def / sudachiSettings.json] under ES_HOME.

# Configuration
- tokenizer: Select tokenizer. (sudachi) (string)
- mode: Select mode. (normal or search or extended) (string, default: search)
	- normal: Regular segmentataion.
	Ex) 関西国際空港 / アバラカダブラ
	- search: Use a heuristic to do additional segmentation useful for search.
	Ex）関西国際空港, 関西, 国際, 空港 / アバラカダブラ
	- extended: Similar to search mode, but also unigram unknown words. (experimental)
	Ex）関西国際空港, 関西, 国際, 空港 / アバラカダブラ, ア, バ, ラ, カ, ダ, ブ, ラ
- discard\_punctuation: Select to discard punctuation or not. (bool, default: true)
- settings\_path: Sudachi setting file path. The path may be absolute or relative; relative paths are resolved with respect to ES\_HOME. (string, default: null)
- resources_path: Sudachi dictionary path. The path may be absolute or relative; relative paths are resolved with respect to ES\_HOME. (string, default: null)

**Example**
```
{
  "settings": {
    "index": {
      "analysis": {
        "tokenizer": {
          "sudachi_tokenizer": {
            "type": "sudachi_tokenizer",
            "mode": "search",
	        "discard_punctuation": true,
            "settings_path": "/etc/elasticsearch/sudachiSettings.json",
            "resources_path": "/etc/elasticsearch"
          }
        },
        "analyzer": {
          "sudachi_analyzer": {
            "filter": [
			],
            "tokenizer": "sudachi_tokenizer",
            "type": "custom"
          }
        }
      }
    }
  }
}
```

# Corresponding Filter
## sudachi\_part\_of\_speech
The sudachi\_part\_of\_speech token filter removes tokens that match a set of part-of-speech tags. It accepts the following setting:
- stoptags
 An array of part-of-speech tags that should be removed. It defaults to the stoptags.txt file embedded in the lucene-analysis-sudachi.jar.

**PUT sudachi_sample**
```
{
  "settings": {
    "index": {
      "analysis": {
        "filter": {},
        "tokenizer": {
          "sudachi_tokenizer": {
            "type": "sudachi_tokenizer",
            "settings_path": "/etc/elasticsearch/sudachiSettings.json",
            "resources_path": "/etc/elasticsearch"
          }
        },
        "analyzer": {
          "sudachi_analyzer": {
            "filter": [
              "my_posfilter"
	    ],
            "tokenizer": "sudachi_tokenizer",
            "type": "custom"
          }
        },
        "filter":{
         "my_posfilter":{
          "type":"sudachi_part_of_speech",
          "stoptags":[
           "助詞,格助詞",
           "助詞,終助詞"
          ]
         }
        }
      }
    }
  }
}
```

**POST sudachi_sample**
```
{
    "analyzer":"sudachi_analyzer",
    "text":"寿司がおいしいね"
}
```

**Which responds with:**
```
{
    "tokens": [
        {
            "token": "寿司",
            "start_offset": 0,
            "end_offset": 2,
            "type": "word",
            "position": 0
        },
        {
            "token": "美味しい",
            "start_offset": 3,
            "end_offset": 7,
            "type": "word",
            "position": 2
        }
    ]
}
```
## sudachi\_ja\_stop
The sudachi\_ja\_stop token filter filters out Japanese stopwords (_japanese_), and any other custom stopwords specified by the user. This filter only supports the predefined _japanese_ stopwords list. If you want to use a different predefined list, then use the stop token filter instead.

**PUT sudachi_sample**
```
{
  "settings": {
    "index": {
      "analysis": {
        "filter": {},
        "tokenizer": {
          "sudachi_tokenizer": {
            "type": "sudachi_tokenizer",
            "settings_path": "/etc/elasticsearch/sudachiSettings.json",
            "resources_path": "/etc/elasticsearch"
          }
        },
        "analyzer": {
          "sudachi_analyzer": {
            "filter": [
              "my_stopfilter"
	    ],
            "tokenizer": "sudachi_tokenizer",
            "type": "custom"
          }
        },
        "filter":{
         "my_stopfilter":{
          "type":"sudachi_ja_stop",
          "stoptags":[
            "_japanese_",
            "は",
            "です"
          ]
         }
        }
      }
    }
  }
}
```

**POST sudachi_sample**
```
{
 "analyzer":"sudachi_analyzer",
 "text":"私は宇宙人です。"
}
```

**Which responds with:**
```
{
    "tokens": [
        {
            "token": "私",
            "start_offset": 0,
            "end_offset": 1,
            "type": "word",
            "position": 0
        },
        {
            "token": "宇宙",
            "start_offset": 2,
            "end_offset": 4,
            "type": "word",
            "position": 2
        },
        {
            "token": "人",
            "start_offset": 4,
            "end_offset": 5,
            "type": "word",
            "position": 3
        }
    ]
}
```

## sudachi\_baseform
The sudachi\_baseform token filter replaces terms with their SudachiBaseFormAttribute. This acts as a lemmatizer for verbs and adjectives.

**PUT sudachi_sample**
```
{
  "settings": {
    "index": {
      "analysis": {
        "filter": {},
        "tokenizer": {
          "sudachi_tokenizer": {
            "type": "sudachi_tokenizer",
            "settings_path": "/etc/elasticsearch/sudachiSettings.json",
            "resources_path": "/etc/elasticsearch"
          }
        },
        "analyzer": {
          "sudachi_analyzer": {
            "filter": [
              "sudachi_baseform"
            ],
            "tokenizer": "sudachi_tokenizer",
            "type": "custom"
          }
        }
      }
    }
  }
}
```

**POST sudachi_sample**
```
{
  "analyzer": "sudachi_analyzer",
  "text": "飲み"
}
```

**Which responds with:**
```
{
    "tokens": [
        {
            "token": "飲む",
            "start_offset": 0,
            "end_offset": 2,
            "type": "word",
            "position": 0
        }
    ]
}
```

## sudachi\_readingform
Convert to katakana or romaji reading.
The sudachi\_readingform token filter replaces the token with its reading form in either katakana or romaji. It accepts the following setting:

- use_romaji
 Whether romaji reading form should be output instead of katakana. Defaults to false.
 When using the pre-defined sudachi_readingform filter, use_romaji is set to true. The default when defining a custom     sudachi_readingform, however, is false. The only reason to use the custom form is if you need the katakana reading form:

**PUT sudachi_sample**
```
{
    "settings": {
        "index": {
            "analysis": {
                "filter": {
                    "romaji_readingform": {
                        "type": "sudachi_readingform",
                        "use_romaji": true
                    },
                    "katakana_readingform": {
                        "type": "sudachi_readingform",
                        "use_romaji": false
                    }
                },
                "tokenizer": {
                    "sudachi_tokenizer": {
                        "type": "sudachi_tokenizer",
                        "settings_path": "/etc/elasticsearch/sudachiSettings.json",
                        "resources_path": "/etc/elasticsearch"
                    }
                },
                "analyzer": {
                    "romaji_analyzer": {
                        "tokenizer": "sudachi_tokenizer",
                        "filter": [
                            "romaji_readingform"
                        ]
                    },
                    "katakana_analyzer": {
                        "tokenizer": "sudachi_tokenizer",
                        "filter": [
                            "katakana_readingform"
                        ]
                    }
                }
            }
        }
    }
}
```

**POST sudachi_sample**
```
{
  "analyzer": "katakana_analyzer",
  "text": "寿司"  ・・・[1]
}
```
```
{
  "analyzer": "romaji_analyzer",
  "text": "寿司"  ・・・[2]
}
```
[1] Returns スシ.
[2] Returns sushi.

# License
Copyright (c) 2017 Works Applications Co., Ltd.
Originally under elasticsearch, https://www.elastic.co/jp/products/elasticsearch
Originally under lucene, https://lucene.apache.org/
