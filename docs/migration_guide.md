バージョン移行ガイド

# from v0.7 to v0.8

## バイナリ辞書

Sudachi 辞書のバイナリ形式が変更され、v0.7 までのバイナリ辞書は使用できなくなりました。
システム辞書については [配布ページ](#TODO) より再取得してください。

ユーザー辞書についても再ビルドが必要です。
詳細は[ユーザー辞書移行ガイド](./migrate_user_dictionary.md)を参照してください。

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
