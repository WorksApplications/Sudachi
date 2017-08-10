package com.worksap.nlp.sudachi;

import java.util.List;
import java.util.Set;

import com.worksap.nlp.sudachi.dictionary.CategoryType;
import com.worksap.nlp.sudachi.dictionary.Grammar;

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
                        concatenate(path, beginIndex, i, lattice);
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
                    concatenate(path, beginIndex, i, lattice);
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
                    concatenate(path, beginIndex, i, lattice);
                    i = beginIndex + 1;
                }
                type = null;
                beginIndex = -1;
            }
        }
        if (beginIndex >= 0) {
            concatenate(path, beginIndex, path.size(), lattice);
        }
    }
}
