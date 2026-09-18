# Changelog

## [Unreleased](https://github.com/WorksApplications/Sudachi/releases/tag/v)

-

## [v0.8.2](https://github.com/WorksApplications/Sudachi/releases/tag/v0.8.2)

> **CAUTION**
> This release is unstable. It may include breaking changes even between patch versions, so please pin the exact version and review release notes carefully before upgrading.

### Breaking changes

See [migration guide](./docs/migration_guide.md) for details.

- Binary dictionary format changed
- Signatures of many methods changed

### Added

- Add `Stream<Morpheme> Dictionary.entries()` (#248)
  - Stream all morphemes in the dictionary.
  - use `Stream.iterator()` or `Stream.spliterator()` if necessary.
- Add `List<Morpheme> Dictionary.lookup(CharSequence)` (#245)
  - List up morphemes that match the given text (after normalization).
  - If you need to search entries with -1 conjugation cost, use `lookupAllEntries`.
- Add `Morpheme Dictionary.oovMorpheme(posId, surface, ...)` (#245)
  - Create an OOV morpheme with a POS in the dictionary.
- Add `Morphme.normalizedFormMorpheme` and `Morphme.dictionaryFormMorpheme` (#359)
  - Get the normalized form and dictionary form of the morphme as a morpheme instead of a string.
  - We can use this e.g. `Morpheme.normalizedFormMorpheme().readingForm()`.

### Changed

- `Tokenizer` methods now returns `List<Morpheme>` instead of `MorphemeList` (#254)
  - use `Tokenizer.split(List<Morphemes>, SplitMode)` instead of `MorphemeList.split(SplitMode)`.
    - providing null as a split mode now errors.
- `Tokenizer Dictionary.create()` is deprecated (#246)
  - use `Tokenizer Dictionary.tokenizer()` instead.
- `Lexicon.wordIds()` behaviour is changed (#248)
  - dictionary id argument is removed.
  - iterates `Integer` instead of `Ints`.

### Deprecated

- `Iterable<MorphemeList> Tokenizer.tokenizeSentences(Reader)` is removed and `lazyTokenizeSentences` replaced it (#254)
  - `Tokenizer.lazyTokenizeSentences` is deprecated.
- `SentenceSplittingAnalysis` is removed (#254)
- `DictionaryFactory` is deprecated and will be removed in v1.0 (#259)
  - use `Dictionary.load(Config)` instead.

## [v0.8.1](https://github.com/WorksApplications/Sudachi/releases/tag/v0.8.1)

> **CAUTION**
> This release is unstable. It may include breaking changes even between patch versions, so please pin the exact version and review release notes carefully before upgrading.

### Added

- Added new CLI option `--print-reading` (#366)
  - With this, the reading form is printed in addition to the default output fields (surface, pos, normalized form).
  - `-a` will override this.
- Update github workflows (#363, #364, #367)
  - Includes test with JDK 21

## [v0.8.0](https://github.com/WorksApplications/Sudachi/releases/tag/v0.8.0)

> **CAUTION**
> This release is unstable. It may include breaking changes even between patch versions, so please pin the exact version and review release notes carefully before upgrading.

### Changed

- `PathAnchor.None` does NOT resolve now (#361).
- 0-th column of DictionaryPrinter output become normalized (#242).

### Added

- Add TextNormalizer (#242)

## [v0.7.5](https://github.com/WorksApplications/Sudachi/releases/tag/v0.7.5)

### Added

- Some benchmark scripts are added under `benchmark/` (#235)

### Changed

- Behavior of the dictionary printer and builder are changed (#234)
  - DictioaryPrinter now prints word reference as (surface, pos, reading)-triple format.
  - DictionaryBuilder now allow dictionary-form to be triple format.

### Fixed

- [Tutorial](./docs/tutorial.md) is updated (#237)
- The byte order of a ByteBuffer returned by `Config.Resource.asByteBuffer` is now always little endian (#239)
  - Also, the byte order of `StringUtil.readAllBytes` is now little endian.

## [v0.7.4](https://github.com/WorksApplications/Sudachi/releases/tag/v0.7.4)

### Added

- Update tutorial.md (#226)
- Lazy sentence split and tokenization (#231)
  - Add `Tokenizer.lazyTokenizeSentences(SplitMode mode, Readable input)`, that performs analysis lazily and saves memory usage.

### Fixed

- Do not segfault on tokenizing with closed dictionary (#217)
- The default config sudachi.json sets non-existent property joinKanjiNumeric in JoinNumericPlugin (#221)
- fix incorrect size calculation when expand (#227)

### Deprecated

- `Tokenizer.tokenizeSentences(SplitMode mode, Reader input)` are marked as deprecated (#231)
