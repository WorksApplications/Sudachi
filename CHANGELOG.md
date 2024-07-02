# Changelog

## [Unreleased]

-

## v0.7.4

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
