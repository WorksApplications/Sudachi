# Changelog

## [Unreleased](https://github.com/WorksApplications/Sudachi/releases/tag/v)

-

## [v0.8.1](https://github.com/WorksApplications/Sudachi/releases/tag/v0.8.1)

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
