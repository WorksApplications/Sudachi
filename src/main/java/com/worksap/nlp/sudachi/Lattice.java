package com.worksap.nlp.sudachi;

import java.util.List;

/**
 * A graph structure used in the morphological analysis.
 *
 * <p>A node of the graph is a morpheme and has a position of the input text.
 * Only the adjacent nodes are connected.
 *
 * @see LatticeNode
 */
public interface Lattice {

    /**
     * Returns the nodes which ends at the specified index.
     *
     * <p>The last position of the nodes is {@code end - 1}.
     *
     * @param end the index to after the last position in the input text
     * @return the list of nodes which end at {@code end}
     *         or an empty list if there is no node
     */
    public List<? extends LatticeNode> getNodesWithEnd(int end);

    /**
     * Returns the nodes at the specified index.
     *
     * <p>The range of nodes begins at the specified {@code begin} and
     * extends to the {@code end - 1}.
     *
     * @param begin the index to the first position in the input text
     * @param end the index to after the last position in the input text
     * @return the list of nodes which start at {@code begin}
     *         and end at {@code end} or an empty list if there is no node
     */
    public List<? extends LatticeNode> getNodes(int begin, int end);

    /**
     * Insert the node at the specified index.
     *
     * <p>The range of the node begins at the specified {@code begin} and
     * extends to the {@code end - 1}.
     *
     * @param begin the index to the first position in the input text
     * @param end the index to after the last position in the input text
     * @param node the node to be inserted
     */
    public void insert(int begin, int end, LatticeNode node);

    /**
     * Remove the node at the specified index.
     *
     * <p>The range of the node begins at the specified {@code begin} and
     * extends to the {@code end - 1}.
     *
     * <p>If the node does not exist at the specified position,
     * this method do nothing.
     *
     * @param begin the index to the first position in the input text
     * @param end the index to after the last position in the input text
     * @param node the node to be removed
     */
    public void remove(int begin, int end, LatticeNode node);

    /**
     * Allocate a new node.
     *
     * @return a new node has no information
     */
    public LatticeNode createNode();
}
