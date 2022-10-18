package com.worksap.nlp.sudachi

import com.worksap.nlp.sudachi.dictionary.DictionaryAccess
import com.worksap.nlp.sudachi.dictionary.Lexicon
import com.worksap.nlp.sudachi.dictionary.POS
import com.worksap.nlp.sudachi.dictionary.WordInfo


fun DictionaryAccess.morpheme(id: Int): Morpheme {
    val node = LatticeNodeImpl(lexicon, 0, id)

    val l = MorphemeList(
        UTF8InputTextBuilder(node.baseSurface, grammar).build(),
        grammar,
        lexicon,
        listOf(node),
        false,
        Tokenizer.SplitMode.A
    )
    return l[0]
}

val Morpheme.wordInfo: WordInfo
    get() = (this as MorphemeImpl).wordInfo

val String.pos: POS
    get() = POS(this.split(","))