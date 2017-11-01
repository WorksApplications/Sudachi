/*
 * Copyright (c) 2017 Works Applications Co., Ltd.
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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

class LatticeImpl implements Lattice {

    private ArrayList<List<LatticeNodeImpl>> endLists;
    private int size;
    private int capacity;
    private LatticeNodeImpl eosNode;

    private Grammar grammar;
    private short[] eosParams;

    LatticeImpl(Grammar grammar) {
        this.grammar = grammar;

        eosParams = grammar.getEOSParameter();

        endLists = new ArrayList<>();
        LatticeNodeImpl bosNode = new LatticeNodeImpl();
        short[] bosParams = grammar.getBOSParameter();
        bosNode.setParameter(bosParams[0], bosParams[1], bosParams[2]);
        bosNode.isConnectedToBOS = true;
        endLists.add(Collections.singletonList(bosNode));
    }

    void resize(int size) {
        if (size > capacity) {
            expand(size);
        }
        this.size = size;

        eosNode = new LatticeNodeImpl();
        eosNode.setParameter(eosParams[0], eosParams[1], eosParams[2]);
        eosNode.begin = eosNode.end = size;
    }

    void clear() {
        for (int i = 1; i < size + 1; i++) {
            endLists.get(i).clear();
        }
        size = 0;
        eosNode = null;
    }

    void expand(int newSize) {
        endLists.ensureCapacity(newSize + 1);
        for (int i = size + 1; i < newSize + 1; i++) {
            endLists.add(new ArrayList<LatticeNodeImpl>());
        }
        capacity = newSize;
    }

    @Override
    public List<LatticeNodeImpl> getNodesWithEnd(int end) {
        return endLists.get(end);
    }

    @Override
    public List<LatticeNodeImpl> getNodes(int begin, int end) {
        return endLists.get(end).stream()
            .filter(n -> ((LatticeNodeImpl)n).begin == begin)
            .collect(Collectors.toList());
    }

    @Override
    public void insert(int begin, int end, LatticeNode node) {
        LatticeNodeImpl n = (LatticeNodeImpl)node;
        endLists.get(end).add(n);
        n.begin = begin;
        n.end = end;

        connectNode(n);
    }

    @Override
    public void remove(int begin, int end, LatticeNode node) {
        endLists.get(end).remove(node);
    }

    @Override
    public LatticeNode createNode() {
        return new LatticeNodeImpl();
    }

    boolean hasPreviousNode(int index) {
        return !endLists.get(index).isEmpty();
    }

    void connectNode(LatticeNodeImpl rNode) {
        int begin = rNode.begin;
        rNode.totalCost = Integer.MAX_VALUE;
        for (LatticeNodeImpl lNode : endLists.get(begin)) {
            if (!lNode.isConnectedToBOS) {
                continue;
            }
            short connectCost
                = grammar.getConnectCost(lNode.rightId, rNode.leftId);
            if (connectCost == Grammar.INHIBITED_CONNECTION) {
                continue; // this connection is not allowed
            }
            int cost = lNode.totalCost + connectCost;
            if (cost < rNode.totalCost) {
                rNode.totalCost = cost;
                rNode.bestPreviousNode = lNode;
            }
        }
        rNode.isConnectedToBOS = !(rNode.bestPreviousNode == null);
        rNode.totalCost += rNode.cost;
    }

    List<LatticeNode> getBestPath() {
        connectNode(eosNode);
        if (!eosNode.isConnectedToBOS) { // EOS node
            throw new IllegalStateException("EOS isn't connected to BOS");
        }
        ArrayList<LatticeNode> result = new ArrayList<>();
        for (LatticeNodeImpl node = eosNode;
             node != endLists.get(0).get(0);
             node = node.bestPreviousNode) {
            result.add(node);
        }
        Collections.reverse(result);
        return result;
    }

    void dump(PrintStream output) {
        int index = 0;
        for (int i = size + 1; i >= 0; i--) {
            List<LatticeNodeImpl> rNodes
                = (i <= size) ? endLists.get(i) : Collections.singletonList(eosNode);
            for (LatticeNodeImpl rNode : rNodes) {
                String surface;
                String pos;
                if (rNode.wordId < 0 && rNode.extraWordInfo == null) {
                    surface = "(null)";
                    pos = "BOS/EOS";
                } else {
                    WordInfo wi = rNode.getWordInfo();
                    surface = wi.getSurface();
                    short posId = wi.getPOSId();
                    if (posId < 0) {
                        pos = "(null)";
                    } else {
                        pos = String.join(",", grammar.getPartOfSpeechString(posId));
                    }
                }

                output.print(String.format("%d: %d %d %s(%d) %s %d %d %d: ",
                                           index,
                                           rNode.getBegin(), rNode.getEnd(),
                                           surface, rNode.wordId, pos,
                                           rNode.leftId, rNode.rightId, rNode.cost));
                index++;

                for (LatticeNodeImpl lNode : endLists.get(rNode.begin)) {
                    int cost = grammar.getConnectCost(lNode.rightId, rNode.leftId);
                    output.print(String.format("%d ", cost));
                }
                output.println();
            }
        }
    }
}
