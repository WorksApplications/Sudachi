バージョン移行ガイド

# from v0.8.1 to v0.8.2

## バイナリ辞書

Sudachi v0.8.2 にて辞書のバイナリ形式が V1 形式に変更され、v0.8.1 までのバイナリ辞書（V0 形式）は使用できなくなりました。
移行には以下の対応が必要です。

- V1 形式のシステム辞書を取得する
  - [V1 形式バイナリ辞書配布ページ](http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/v1) にて配布しています。
- ユーザー辞書を V1 形式に再ビルドする
  - このとき、実行時に使用するものと同じシステム辞書を使用する必要があります。
- 辞書配布ページの URL を使用している場合は更新する
  - V1 形式バイナリ辞書配布ページ：http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/v1
  - V1 形式辞書ソース配布ページ：http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict-raw/v1

ユーザー辞書の再ビルドについては[ユーザー辞書移行ガイド](./migrate_user_dictionary.md)を参照してください。

ユーザー辞書には、ビルド時に使用したシステム辞書の識別情報が記録されるようになりました。実行時にこれと異なるシステム辞書が指定された場合はエラーとなります。
システム辞書を更新する際には、使用するすべてのユーザー辞書についてもそのシステム辞書で再ビルドする必要があります。

## Dictionary

### Dictionary の生成

`DictionaryFactory` クラスは廃止されます。

辞書インスタンスの生成には、代わりに `Dictionary.load(Config)` を使用してください。

## Tokenizer

### Tokenizer の生成

トークナイザーを生成する `Dictionary.create()` は廃止されます。

代わりに `Dictionary.tokenizer()` を使用してください。

### シグネチャの変更

Tokenizer の各メソッドの返り値型は `MorphemeList` から `List<Morpheme>` に変更されました。

`MorphemeList.split(SplitMode)` を使用していた場合は、代わりに `Tokenizer.split(List<Morpheme>, SplitMode)` を使用してください。
また `SplitMode` 引数がヌルの場合、エラーとなるようになりました。

### tokenizeSentences の挙動の変更

`Iterable<MorphemeList> Tokenizer.tokenizeSentences(Reader)` は OOM の可能性があったため廃止されました。
v0.8 では同名の `Iterator<List<Morpheme>> Tokenizer.tokenizeSentences(Readable)` に処理内容が置き換わっています。
返り値型が `Iterable` から `Iterator` に変更されていることに注意してください。

これの挙動は `Tokenizer.lazyTokenizeSentences` と同値であり、このため `lazyTokenizeSentences` は廃止されます。
`lazyTokenizeSentences` を使用していた場合は `tokenizeSentences` に変更してください。

## TextNormalizer

辞書に基づいた `TextNormalizer` を生成する `TextNormalizer.formDictionary(JapaneseDictionary)` は廃止されます。
代わりに `Dictionary.textNormalizer()` を使用してください。

## 廃止されたクラス

以下のクラスは廃止されました

### `SentenceSplittingAnalysis`

代わりに `SentenceSplittingLazyAnalysis` を使用してください。
