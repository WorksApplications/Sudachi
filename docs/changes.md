# 0.8.0

### ABI-incompatible
* `Morpheme.partOfSpeech` returns `POS` object instead of `List<String>` 
* `Lexicon.getCost`, `Lexicon.getLeftId`, `Lexicon.getRightId` are replaced with `lexicon.parameters` which returns all three values packed, at once.