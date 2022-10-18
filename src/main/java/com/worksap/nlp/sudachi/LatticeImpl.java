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

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import com.worksap.nlp.sudachi.dictionary.Connection;
import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

class LatticeImpl implements Lattice {

    private final ArrayList<ArrayList<LatticeNodeImpl>> endLists;
    private int size;
    private int capacity;
    private LatticeNodeImpl eosNode;

    private final Grammar grammar;
    private final short[] eosParams;

    LatticeImpl(Grammar grammar) {
        this.grammar = grammar;

        eosParams = grammar.getEOSParameter();

        endLists = new ArrayList<>();
        LatticeNodeImpl bosNode = new LatticeNodeImpl();
        bosNode.bestPreviousNode = bosNode;
        short[] bosParams = grammar.getBOSParameter();
        bosNode.setParameter(bosParams[0], bosParams[1], bosParams[2]);
        // endLists should not contain anything except ArrayLists
        // it is crucial to have monomorphic dispatch here
        ArrayList<LatticeNodeImpl> bos = new ArrayList<>();
        bos.add(bosNode);
        endLists.add(bos);
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
            endLists.add(new ArrayList<>());
        }
        capacity = newSize;
    }

    @Override
    public List<LatticeNodeImpl> getNodesWithEnd(int end) {
        return endLists.get(end);
    }

    @Override
    public List<LatticeNodeImpl> getNodes(int begin, int end) {
        return endLists.get(end).stream().filter(n -> (n.getBegin() == begin)).collect(Collectors.toList());
    }

    @Override
    public LatticeNodeImpl getMinimumNode(int begin, int end) {
        ArrayList<LatticeNodeImpl> ends = endLists.get(end);
        LatticeNodeImpl result = null;
        for (LatticeNodeImpl node: ends) {
            if (node.begin == begin) {
                if (result == null || result.totalCost >= node.cost) {
                    result = node;
                }
            }
        }
        return result;
    }

    @Override
    public void insert(int begin, int end, LatticeNode node) {
        LatticeNodeImpl n = (LatticeNodeImpl) node;
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

        // connection matrix needs to be in the current stack frame to elide field
        // accesses in the hot loop
        final Connection conn = grammar.getConnection();
        int leftId = rNode.leftId;
        conn.validate(leftId); // elide some compiler checks by calling this method

        // all heavy accessed variables must be on stack
        // and written to fields only at the end of the function
        ArrayList<LatticeNodeImpl> endNodes = endLists.get(begin);
        LatticeNodeImpl bestPrevNode = null;
        int minLeftCost = Integer.MAX_VALUE;

        // Using a plain loop decreases the code footprint of this method
        // Do not use iterator-based for-loop here
        // noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < endNodes.size(); ++i) {
            LatticeNodeImpl lNode = endNodes.get(i);
            if (!lNode.isConnectedToBOS()) {
                continue;
            }

            int connectCost = conn.cost(lNode.rightId, leftId);
            if (connectCost == Grammar.INHIBITED_CONNECTION) {
                continue; // this connection is not allowed
            }
            int cost = lNode.totalCost + connectCost;
            if (cost < minLeftCost) {
                minLeftCost = cost;
                bestPrevNode = lNode;
            }
        }
        rNode.totalCost = minLeftCost + rNode.cost;
        rNode.bestPreviousNode = bestPrevNode;
    }

    void connectEosNode() {
        connectNode(eosNode);
    }

    List<LatticeNodeImpl> getBestPath() {
        if (!eosNode.isConnectedToBOS()) { // EOS node
            throw new IllegalStateException("EOS isn't connected to BOS");
        }
        ArrayList<LatticeNodeImpl> result = new ArrayList<>();
        for (LatticeNodeImpl node = eosNode.bestPreviousNode; node != endLists.get(0)
                .get(0); node = node.bestPreviousNode) {
            result.add(node);
        }
        Collections.reverse(result);
        return result;
    }

    String getSurface(LatticeNodeImpl node) {
        return (node.isDefined) ? node.getBaseSurface() : "(null)";
    }

    String getPos(LatticeNodeImpl node) {
        if (!node.isDefined) {
            return "BOS/EOS";
        } else {
            WordInfo wi = node.getWordInfo();
            short posId = wi.getPOSId();
            return (posId < 0) ? "(null)" : String.join(",", grammar.getPartOfSpeechString(posId));
        }
    }

    void dump(PrintStream output) {
        int index = 0;
        for (int i = size + 1; i >= 0; i--) {
            List<LatticeNodeImpl> rNodes = (i <= size) ? endLists.get(i) : Collections.singletonList(eosNode);
            for (LatticeNodeImpl rNode : rNodes) {
                String surface = getSurface(rNode);
                String pos = getPos(rNode);

                output.printf("%d: %d %d %s(%d) %s %d %d %d: ", index, rNode.getBegin(), rNode.getEnd(), surface,
                        rNode.wordId, pos, rNode.leftId, rNode.rightId, rNode.cost);
                index++;

                for (LatticeNodeImpl lNode : endLists.get(rNode.begin)) {
                    int cost = grammar.getConnectCost(lNode.rightId, rNode.leftId);
                    output.printf("%d ", cost);
                }
                output.println();
            }
        }
    }

    JsonObjectBuilder nodeToJson(LatticeNodeImpl node) {
        String surface = getSurface(node);
        String pos = getPos(node);
        int begin = node.getBegin();
        int end = node.getEnd();

        return Json.createObjectBuilder()
                .add("begin", (begin == end && begin == 0) ? JsonValue.NULL : Json.createValue(begin))
                .add("end", (begin == end && begin != 0) ? JsonValue.NULL : Json.createValue(end))
                .add("headword", surface).add("wordId", node.wordId).add("pos", pos).add("leftId", node.leftId)
                .add("rightId", node.rightId).add("cost", node.cost);
    }

    JsonArrayBuilder toJson() {
        JsonArrayBuilder lattice = Json.createArrayBuilder();
        int nodeId = 0;
        for (int i = 0; i <= size + 1; i++) {
            List<LatticeNodeImpl> rNodes = (i <= size) ? endLists.get(i) : Collections.singletonList(eosNode);
            for (LatticeNodeImpl rNode : rNodes) {
                JsonObjectBuilder node = nodeToJson(rNode).add("nodeId", nodeId++);

                JsonArrayBuilder connectCosts = Json.createArrayBuilder();
                for (LatticeNodeImpl lNode : endLists.get(rNode.begin)) {
                    int cost = grammar.getConnectCost(lNode.rightId, rNode.leftId);
                    connectCosts.add(cost);
                }
                node.add("connectCosts", connectCosts);

                lattice.add(node);
            }
        }
        return lattice;
    }
}
