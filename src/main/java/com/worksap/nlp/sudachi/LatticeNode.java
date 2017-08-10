package com.worksap.nlp.sudachi;

import com.worksap.nlp.sudachi.dictionary.WordInfo;

/**
 * A node of {@link Lattice}.
 *
 * <p>The node has the parameters of connection, the position in the input
 * text, and the information of morpheme as {@link WordInfo}
 *
 * <p>Allocation of a node in the plugins must be done through
 * {@link Lattice#createNode}.
 *
 * @see Lattice
 * @see WordInfo
 */
public interface LatticeNode {

    /**
     * Set the parameters of connection.
     *
     * @param leftId the left-ID of connection
     * @param rightId the right-ID of connection
     * @param cost the word occurrence cost
     */
    public void setParameter(short leftId, short rightId, short cost);

    /**
     * Returns the index to the first position of the node in the input text.
     *
     * @return the index to the first position of the node in the input text.
     */
    public int getBegin();

    /**
     * Returns the index to after the last position of the node
     * in the input text.
     * The last position of the nodes is {@code getEnd() - 1}.
     *
     * @return the index to after the last position of the node
     * in the input text.
     */
    public int getEnd();

    /**
     * Set the indexes of the node in the input text.
     *
     * The last position of the nodes is {@code end - 1}.
     *
     * @param begin the index to the first position of the node
     *        in the input text.
     * @param end the index to after the last position of the node
     *        in the input text.
     */
    public void setRange(int begin, int end);

    /**
     * Returns {@code true} if, and only if, the node is out of vocabulary.
     *
     * @return {@code true} if the node is OOV, otherwise {@code false}
     */
    public boolean isOOV();

    /**
     * Makes the node out of vocabulary.
     */
    public void setOOV();

    /**
     * Returns the morpheme information of the node.
     *
     * @return the morpheme information of the node
     * @see WordInfo
     */
    public WordInfo getWordInfo();

    /**
     * Sets the morpheme information to the node.
     *
     * @param wordInfo the morpheme information
     * @see WordInfo
     */
    public void setWordInfo(WordInfo wordInfo);

    /**
     * Returns the cost of the path from the beginning of sentence
     * to this node.
     *
     * @return the cost of the path from BOS to this node
     */
    public int getPathCost();
}
