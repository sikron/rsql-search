package com.skronawi.rsql.util;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.LogicalNode;
import cz.jirutka.rsql.parser.ast.Node;

import java.util.List;

public class Assertions {

    public static void assertSelectors(Node node, List<String> selectors) {
        if (node instanceof ComparisonNode) {
            String selector = ((ComparisonNode) node).getSelector();
            if (!selectors.contains(selector)) {
                throw new IllegalArgumentException("unsupported selector: " + selector);
            }
        } else if (node instanceof LogicalNode) {

            List<Node> children = ((LogicalNode) node).getChildren();
            for (Node child : children) {
                assertSelectors(child, selectors);
            }
        } else {
            throw new IllegalStateException("only ComparisonNode and LogicalNode supported");
        }
    }
}
