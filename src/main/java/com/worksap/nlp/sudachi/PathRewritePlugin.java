package com.worksap.nlp.sudachi;

import java.io.IOException;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

public abstract class PathRewritePlugin extends Plugin {

    public abstract void setUp(Grammar grammar) throws IOException;

    public abstract void rewrite(InputText<?> text, List<LatticeNode> path, Lattice lattice);

    LatticeNode concatenate(List<LatticeNode> path, int begin, int end,
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
}
