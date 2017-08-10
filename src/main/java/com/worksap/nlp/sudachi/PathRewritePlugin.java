package com.worksap.nlp.sudachi;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.worksap.nlp.sudachi.dictionary.CategoryType;
import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

public abstract class PathRewritePlugin extends Plugin {

    public void setUp(Grammar grammar) throws IOException {}

    public abstract void rewrite(InputText<?> text, List<LatticeNode> path, Lattice lattice);

    public LatticeNode concatenate(List<LatticeNode> path, int begin, int end,
                                   Lattice lattice) {
        int b = path.get(begin).getBegin();
        int e = path.get(end - 1).getEnd();
        short posId = path.get(begin).getWordInfo().getPOSId();
        StringBuilder surface = new StringBuilder();
        int length = 0;
        StringBuilder normalizedForm = new StringBuilder();
        StringBuilder dictionaryForm = new StringBuilder();
        StringBuilder reading = new StringBuilder();
        for (int i = begin; i < end; i++) {
            WordInfo info = path.get(i).getWordInfo();
            surface.append(info.getSurface());
            length += info.getLength();
            normalizedForm.append(info.getNormalizedForm());
            dictionaryForm.append(info.getDictionaryForm());
            reading.append(info.getReading());
        }
        WordInfo wi = new WordInfo(surface.toString(), (short)length, posId,
                                   normalizedForm.toString(),
                                   dictionaryForm.toString(),
                                   reading.toString());

        LatticeNode node = lattice.createNode();
        node.setRange(b, e);
        node.setWordInfo(wi);
        path.subList(begin, end).clear();
        path.add(begin, node);
        return node;
    }

    public LatticeNode concatenateOov(List<LatticeNode> path, int begin, int end, short posId,
                                      Lattice lattice) {
        int b = path.get(begin).getBegin();
        int e = path.get(end - 1).getEnd();
        StringBuilder surface = new StringBuilder();
        int length = 0;
        for (int i = begin; i < end; i++) {
            WordInfo info = path.get(i).getWordInfo();
            surface.append(info.getSurface());
            length += info.getLength();
        }
        String s = surface.toString();
        WordInfo wi = new WordInfo(s, (short)length, posId, s, s, "");

        LatticeNode node = lattice.createNode();
        node.setRange(b, e);
        node.setWordInfo(wi);
        node.setOOV();
        path.subList(begin, end).clear();
        path.add(begin, node);
        return node;
    }

    public Set<CategoryType> getCharCategoryTypes(InputText<?> text, LatticeNode node) {
        return text.getCharCategoryTypes(node.getBegin(), node.getEnd());
    }
}
