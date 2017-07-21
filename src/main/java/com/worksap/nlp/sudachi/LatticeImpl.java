package com.worksap.nlp.sudachi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.worksap.nlp.sudachi.dictionary.Grammar;

class LatticeImpl implements Lattice {

    private List<List<LatticeNodeImpl>> beginLists;
    private List<List<LatticeNodeImpl>> endLists;
    private int size;

    private Grammar grammar;

    LatticeImpl(int size, Grammar grammar) {
        this.size = size;
        this.grammar = grammar;

        LatticeNodeImpl bosNode = new LatticeNodeImpl();
        short[] bosParams = grammar.getBOSParameter();
        bosNode.setParameter(bosParams[0], bosParams[1], bosParams[2]);

        LatticeNodeImpl eosNode = new LatticeNodeImpl();
        short[] eosParams = grammar.getEOSParameter();
        eosNode.setParameter(eosParams[0], eosParams[1], eosParams[2]);
        eosNode.begin = eosNode.end = size;

        beginLists = new ArrayList<List<LatticeNodeImpl>>(size + 1);
        for (int i = 0; i < size; i++) {
            beginLists.add(new ArrayList<LatticeNodeImpl>());
        }
        beginLists.add(Collections.singletonList(eosNode));

        endLists = new ArrayList<List<LatticeNodeImpl>>(size + 1);
        endLists.add(Collections.singletonList(bosNode));
        for (int i = 1; i < size + 1; i++) {
            endLists.add(new ArrayList<LatticeNodeImpl>());
        }
    }

    @Override
    public List<LatticeNodeImpl> getNodesWithBegin(int begin) {
        return beginLists.get(begin);
    }

    @Override
    public List<LatticeNodeImpl> getNodesWithEnd(int end) {
        return endLists.get(end);
    }

    @Override
    public List<LatticeNodeImpl> getNodes(int begin, int end) {
        return beginLists.get(begin).stream()
            .filter(n -> ((LatticeNodeImpl)n).end == end)
            .collect(Collectors.toList());
    }

    @Override
    public void insert(int begin, int end, LatticeNode node) {
        LatticeNodeImpl n = (LatticeNodeImpl)node;
        beginLists.get(begin).add(n);
        endLists.get(end).add(n);
        n.begin = begin;
        n.end = end;
    }

    @Override
    public void remove(int begin, int end, LatticeNode node) {
        beginLists.get(begin).remove(node);
        endLists.get(end).remove(node);
    }

    @Override
    public LatticeNode createNode() {
        return new LatticeNodeImpl();
    }

    boolean hasPreviousNode(int index) {
        return !endLists.get(index).isEmpty();
    }

    List<LatticeNodeImpl> getBestPath() {
        viterbi();
        ArrayList<LatticeNodeImpl> result = new ArrayList<>();
        for (LatticeNodeImpl node = beginLists.get(size).get(0);
             node != endLists.get(0).get(0);
             node = node.bestPreviousNode) {
            result.add(node);
        }
        Collections.reverse(result);
        return result;
    }

    private void viterbi() {
        for (int i = 0; i < size + 1; i++) {
            for (LatticeNodeImpl rNode : beginLists.get(i)) {
                rNode.totalCost = Integer.MAX_VALUE;
                for (LatticeNodeImpl lNode : endLists.get(i)) {
                    int cost = lNode.totalCost
                        + grammar.getConnectCost(lNode.rightId, rNode.leftId);
                    if (cost < rNode.totalCost) {
                        rNode.totalCost = cost;
                        rNode.bestPreviousNode = lNode;
                    }
                }
                rNode.totalCost += rNode.cost;
            }
        }
    }
}
