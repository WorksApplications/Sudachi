package com.worksap.nlp.sudachi;

import java.util.List;
import java.util.Set;

import com.worksap.nlp.sudachi.dictionary.CategoryType;
import com.worksap.nlp.sudachi.dictionary.Grammar;

/**
 * A plugin for concatenation of the numerics.
 *
 * This plugin concatenate the sequence of numerics.
 * Only the sequence of digits are joined by default.
 *
 * <p>The following is an example of settings.
 * <pre>{@code
 *   {
 *     "class" : "com.worksap.nlp.sudachi.JoinNumericPlugin",
 *     "joinKanjiNumeric" : true,
 *     "joinAllNumeric"   : false
 *   }
 * }</pre>
 *
 * <p>If {@code joinKanjiNumeric} is {@code true}, the sequence of
 * Kanji numerics are joined.
 * <p>If {@code joinAllNumeric} is {@code true}, the sequence of digits
 * and Kanji numerics are joined.
 */
class JoinNumericPlugin extends PathRewritePlugin {

    boolean joinKanjiNumeric;
    boolean joinAllNumeric;

    @Override
    public void setUp(Grammar grammar) {
        joinKanjiNumeric = settings.getBoolean("joinKanjiNumeric", false);
        joinAllNumeric = settings.getBoolean("joinAllNumeric", false);
    }

    @Override
    public void rewrite(InputText<?> text, List<LatticeNode> path, Lattice lattice) {
        int beginIndex = -1;
        CategoryType type = null;
        for (int i = 0; i < path.size(); i++) {
            LatticeNode node = path.get(i);
            Set<CategoryType> types = getCharCategoryTypes(text, node);
            if (types.contains(CategoryType.NUMERIC)) {
                if (type == CategoryType.NUMERIC) {
                    continue;
                }
                if (type == CategoryType.KANJINUMERIC) {
                    if (joinAllNumeric) {
                        continue;
                    } else if (joinKanjiNumeric) {
                        if (i - beginIndex > 1) {
                            concatenate(path, beginIndex, i, lattice);
                        }
                        i = beginIndex + 1;
                    }
                }
                type = CategoryType.NUMERIC;
                beginIndex = i;
            } else if (types.contains(CategoryType.KANJINUMERIC)) {
                if (type == CategoryType.KANJINUMERIC) {
                    continue;
                }
                if (type == CategoryType.NUMERIC) {
                    if (joinAllNumeric) {
                        continue;
                    }
                    if (i - beginIndex > 1) {
                        concatenate(path, beginIndex, i, lattice);
                    }
                    i = beginIndex + 1;
                }
                if (joinKanjiNumeric) {
                    type = CategoryType.KANJINUMERIC;
                    beginIndex = i;
                } else {
                    type = null;
                    beginIndex = -1;
                }
            } else {
                if (beginIndex >= 0) {
                    if (i - beginIndex > 1) {
                        concatenate(path, beginIndex, i, lattice);
                    }
                    i = beginIndex + 1;
                }
                type = null;
                beginIndex = -1;
            }
        }
        if (beginIndex >= 0 && path.size() - beginIndex > 1) {
            concatenate(path, beginIndex, path.size(), lattice);
        }
    }
}
