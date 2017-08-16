package com.worksap.nlp.sudachi;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.worksap.nlp.sudachi.dictionary.CategoryType;
import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

/**
 * A plugin that rewrite the best path of the lattice.
 *
 * <p>{@link Dictionary} initialize this plugin with {@link Settings}.
 * It can be referred as {@link Plugin#settings}.
 *
 * <p>The following is an example of settings.
 * <pre>{@code
 *   {
 *     "class" : "com.worksap.nlp.sudachi.PathRewritePlugin",
 *     "example" : "example setting"
 *   }
 * }</pre>
 */
public abstract class PathRewritePlugin extends Plugin {

    /**
     * Set up the plugin.
     *
     * {@link Tokenizer} calls this method for setting up this plugin.
     *
     * @param grammar the grammar of the system dictionary
     * @throws IOException if reading something is failed
     */
    public void setUp(Grammar grammar) throws IOException {}

    /**
     * Rewrite the path of the lattice.
     * The path is a list of nodes of the lattice. To join some nodes
     * you can use {@link #concatenate} or {@link #concatenateOov}.
     *
     * @param text the input text
     * @param path the best path of the lattice
     * @param lattice the lattice
     */
    public abstract void rewrite(InputText<?> text, List<LatticeNode> path, Lattice lattice);

    /**
     * Concatenate the sequence of nodes in the path.
     * The sequence begins at the specified {@code begin} and
     * extends to the node at index {@code end - 1}.
     * 
     * <p>The concatenated node has the POS ID of the head of the sequence.
     * 
     * @param path the path
     * @param begin the beginning index
     * @param end the ending index
     * @param lattice the lattice
     * @return the concatenated node
     * @throws IndexOutOfBoundsException if if {@code begin} or {@code end}
     *         are negative, greater than the length of the sequence,
     *         or {@code begin} equals or is greater than {@code end}
     */
    public LatticeNode concatenate(List<LatticeNode> path, int begin, int end,
                                   Lattice lattice) {
        if (begin >= end) {
            throw new IndexOutOfBoundsException("begin >= end");
        }
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

    /**
     * Concatenate the sequence of nodes in the path.
     * The sequence begins at the specified {@code begin} and
     * extends to the node at index {@code end - 1}.
     * 
     * <p>The concatenated node is marked as OOV.
     * 
     * @param path the path
     * @param begin the beginning index
     * @param end the ending index
     * @param posId the POS ID of the concatenated node
     * @param lattice the lattice
     * @return the concatenated OOV node
     * @throws IndexOutOfBoundsException if {@code begin} or {@code end}
     *         are negative, greater than the length of the sequence,
     *         or {@code begin} equals or is greater than {@code end}
     */
    public LatticeNode concatenateOov(List<LatticeNode> path, int begin, int end, short posId,
                                      Lattice lattice) {
        if (begin >= end) {
            throw new IndexOutOfBoundsException("begin >= end");
        }
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

    /**
     * Return the set of the category types of the node.
     *
     * @param text the input text
     * @param node the node
     * @return the set of the category types of the node
     */
    public Set<CategoryType> getCharCategoryTypes(InputText<?> text, LatticeNode node) {
        return text.getCharCategoryTypes(node.getBegin(), node.getEnd());
    }
}
