package com.hnunner.graphstreamsim;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

/**
 * @author Hendrik Nunner
 */
public class Simulation {

    private static final int DELAY = 1000;

    /**
     * @param args
     *          runtime arguments
     * @throws InterruptedException
     *          for Thread.sleep()
     */
    public static void main(String[] args) throws InterruptedException {

        // graph
        Graph graph = new SingleGraph("GraphStreamSimulation");
        graph.display();

        graph.addNode("0");
        graph.addNode("1");
        graph.addNode("2");
        graph.addNode("3");
        graph.addNode("4");
        graph.addNode("5");
        Thread.sleep(DELAY);

        for (int i = 0; i < 6; i++) {
            String edgeId = "0" + i;
            graph.addEdge(edgeId, 1, i);
            Thread.sleep(DELAY);
        }

    }

}
