/*
 * Copyright (c) 2021 Works Applications Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * <p>
 * {@link Dictionary} initialize this plugin with {@link Settings}. It can be
 * referred as {@link Plugin#settings}.
 *
 * <p>
 * The following is an example of settings.
 * 
 * <pre>
 * {@code
 *   {
 *     "class" : "com.worksap.nlp.sudachi.PathRewritePlugin",
 *     "example" : "example setting"
 * }
 * }
 * </pre>
 */
public abstract class PathRewritePlugin extends Plugin {

    /**
     * Set up the plugin.
     *
     * {@link Tokenizer} calls this method for setting up this plugin.
     *
     * @param grammar
     *            the grammar of the system dictionary
     * @throws IOException
     *             if reading something is failed
     */
    public void setUp(Grammar grammar) throws IOException {
    }

    /**
     * Rewrite the path of the lattice. The path is a list of nodes of the lattice.
     * To join some nodes you can use {@link #concatenate} or
     * {@link #concatenateOov}.
     *
     * @param text
     *            the input text
     * @param path
     *            the best path of the lattice
     * @param lattice
     *            the lattice
     */
    public abstract void rewrite(InputText text, List<LatticeNodeImpl> path, Lattice lattice);

    /**
     * Concatenate the sequence of nodes in the path. The sequence begins at the
     * specified {@code begin} and extends to the node at index {@code end - 1}.
     * 
     * <p>
     * The concatenated node has the POS ID of the head of the sequence.
     * 
     * @param path
     *            the path
     * @param begin
     *            the beginning index
     * @param end
     *            the ending index
     * @param lattice
     *            the lattice
     * @param normalizedForm
     *            if {@code normalizedForm} is {@code null}, concatenate the
     *            normalizedForms of each words
     * @return the concatenated node
     * @throws IndexOutOfBoundsException
     *             if {@code begin} or {@code end} are negative, greater than the
     *             length of the sequence, or {@code begin} equals or is greater
     *             than {@code end}
     */
    public LatticeNode concatenate(List<LatticeNodeImpl> path, int begin, int end, Lattice lattice, String normalizedForm) {
        if (begin >= end) {
            throw new IndexOutOfBoundsException("begin >= end");
        }
        int b = path.get(begin).getBegin();
        int e = path.get(end - 1).getEnd();
        short posId = path.get(begin).getWordInfo().getPOSId();
        StringBuilder surface = new StringBuilder();
        StringBuilder normalizedFormBuilder = new StringBuilder();
        StringBuilder dictionaryForm = new StringBuilder();
        StringBuilder readingForm = new StringBuilder();
        for (int i = begin; i < end; i++) {
            WordInfo info = path.get(i).getWordInfo();
            surface.append(info.getSurface());
            if (normalizedForm == null) {
                normalizedFormBuilder.append(info.getNormalizedForm());
            }
            dictionaryForm.append(info.getDictionaryForm());
            readingForm.append(info.getReadingForm());
        }

        String s = surface.toString();
        LatticeNodeImpl node = LatticeNodeImpl.makeOov(
                b, e,
                posId,
                s,
                (normalizedForm == null) ? normalizedFormBuilder.toString() : normalizedForm,
                dictionaryForm.toString(),
                readingForm.toString()
        );
        replaceNode(path, begin, end, node);
        return node;
    }

    /**
     * Concatenate the sequence of nodes in the path. The sequence begins at the
     * specified {@code begin} and extends to the node at index {@code end - 1}.
     * 
     * <p>
     * The concatenated node is marked as OOV.
     * 
     * @param path
     *            the path
     * @param begin
     *            the beginning index
     * @param end
     *            the ending index
     * @param factory
     *            factory for creating an OOV lattice node
     * @param lattice
     *            the lattice
     * @return the concatenated OOV node
     * @throws IndexOutOfBoundsException
     *             if {@code begin} or {@code end} are negative, greater than the
     *             length of the sequence, or {@code begin} equals or is greater
     *             than {@code end}
     */
    public LatticeNode concatenateOov(List<LatticeNodeImpl> path, int begin, int end, LatticeNodeImpl.OOVFactory factory, Lattice lattice) {
        if (begin >= end) {
            throw new IndexOutOfBoundsException("begin >= end");
        }
        int b = path.get(begin).getBegin();
        int e = path.get(end - 1).getEnd();

        LatticeNodeImpl node = lattice.getMinimumNode(b, e);
        if (node != null) {
            replaceNode(path, begin, end, node);
            return node;
        }

        StringBuilder surface = new StringBuilder();
        for (int i = begin; i < end; i++) {
            String s = path.get(i).getBaseSurface();
            surface.append(s);
        }

        String s = surface.toString();
        node = factory.make(b, e, s);
        replaceNode(path, begin, end, node);
        return node;
    }

    /**
     * Return the set of the category types of the node.
     *
     * @param text
     *            the input text
     * @param node
     *            the node
     * @return the set of the category types of the node
     */
    public Set<CategoryType> getCharCategoryTypes(InputText text, LatticeNode node) {
        return text.getCharCategoryTypes(node.getBegin(), node.getEnd());
    }

    private void replaceNode(List<LatticeNodeImpl> path, int begin, int end, LatticeNodeImpl node) {
        path.subList(begin, end).clear();
        path.add(begin, node);
    }
}
