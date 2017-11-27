package com.hnunner.graphstreamsim;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

/**
 *
 * @author Hendrik Nunner
 */
public class SwingSimulation {

    private static final int DELAY = 1000;

    private JFrame frame;
    private Graph graph;

    /**
     * Launch the application.
     *
     * @param args
     *          runtime arguments
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    SwingSimulation window = new SwingSimulation();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public SwingSimulation() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     * @throws InterruptedException
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 146, 101);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton btnSimulate = new JButton("Simulate");
        btnSimulate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                simulate();
            }
        });
        frame.getContentPane().add(btnSimulate, BorderLayout.CENTER);


        // graph
        graph = new SingleGraph("GraphStreamSimulation");
        graph.display();

        graph.addNode("0");
        graph.addNode("1");
        graph.addNode("2");
        graph.addNode("3");
        graph.addNode("4");
        graph.addNode("5");
    }

    private void simulate() {
        for (int i = 0; i < 6; i++) {
            String edgeId = "0" + i;
            graph.addEdge(edgeId, 1, i);
            System.out.println("added edge between node " + 1 + " and node " + i);
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
