package com.worksap.nlp.sudachi;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.worksap.nlp.sudachi.dictionary.Grammar;

public abstract class WordLookingUpPlugin {

    public abstract void setUp(Grammar grammar) throws IOException;

    public abstract List<LatticeNode> provideOOV(String text,
                                                 List<Integer> otherWordsLength);

    List<LatticeNode> getOOV(UTF8InputText inputText, int offset,
                             List<int[]> otherWords) {
        byte[] bytes = inputText.getByteText();
        String text =
            new String(bytes, offset, bytes.length - offset);
        List<Integer> othersLength = otherWords.stream()
            .map(o -> new String(bytes, offset, o[1] - offset).length())
            .collect(Collectors.toList());

        List<LatticeNode> nodes = provideOOV(text, othersLength);
        for (LatticeNode node : nodes) {
            LatticeNodeImpl n = (LatticeNodeImpl)node;
            n.begin = offset;
            n.end = offset + node.getWordInfo().getLength();
        }
        return nodes;
    }

    LatticeNode createNode() {
        LatticeNode node = new LatticeNodeImpl();
        node.setOOV();
        return node;
    }
}
