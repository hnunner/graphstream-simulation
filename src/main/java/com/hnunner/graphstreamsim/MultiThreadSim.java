package com.hnunner.graphstreamsim;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

public class MultiThreadSim {

	public static void main(String[] args) {
		new MultiThreadSim();
	}

	private static final int DELAY = 100;

	final int nrOfNodes = 100;
	final int nrOfWorkers = 10;

	final ExecutorService service = Executors.newFixedThreadPool(nrOfWorkers);
	final ReentrantLock lock = new ReentrantLock();
	
	final Graph graph;
	final JFrame frame;
	final ViewPanel view;
	final RandomEdge[] workers;

	public MultiThreadSim() {
		graph = createGraph();

		workers = new RandomEdge[nrOfWorkers];
		for (int i = 0; i < nrOfWorkers; i++) 
			workers[i] = new RandomEdge(i, graph, 20);
		
		view = createView(graph);
		frame = createFrame();

		setupFrame(frame, view);

		frame.setVisible(true);
	}

	Graph createGraph() {
		Graph graph = new MultiGraph("whoop");

		for (int i = 0; i < nrOfNodes; i++)
			graph.addNode(Integer.toString(i));

		return graph;
	}

	ViewPanel createView(Graph graph) {
		Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		viewer.enableAutoLayout();
		return viewer.addDefaultView(false); // false indicates "no JFrame".
	}

	JFrame createFrame() {
		JFrame frame = new JFrame();
		frame.setBounds(100, 100, 500, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		return frame;
	}

	void setupFrame(JFrame frame, ViewPanel view) {

		JButton btnSimulate = new JButton("SIMULATE BLYAT");
		btnSimulate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startSimulation();
			}
		});

		frame.getContentPane().add(btnSimulate, BorderLayout.NORTH);
		frame.getContentPane().add(view, BorderLayout.CENTER);
	}

	void startSimulation() {
		
		for(int i = 0; i < nrOfWorkers; i++) 
			service.submit(workers[i]);

	}

	class RandomEdge implements Runnable {

		final Random rand = new Random();
		final Graph graph;
		final int workerId, edgeCount;

		public RandomEdge(int workerId, Graph graph, int edgeCount) {
			this.workerId = workerId;
			this.edgeCount = edgeCount;
			this.graph = graph;
		}

		@Override
		public void run() {
			
			int max = DELAY/3;
			int min = -DELAY/3;
			
			for (int i = 0; i < edgeCount; i++) {
				
				lock.lock();
				
				try {
					
					int count = 0;
					int a = rand.nextInt(graph.getNodeCount());
					int b = (a + 1) % graph.getNodeCount();
					String edgeId = "edge|" + workerId + ":" + a + "," + b;
					
					// Try finding a valid edge, but give up after 10 attempts
					while(graph.getEdge(edgeId) != null || count >= 10) {
						a = rand.nextInt(graph.getNodeCount());
						b = (a + 1) % graph.getNodeCount();
						edgeId = "edge|" + workerId + ":" + a + ":" + b;
						count++;
					}
					
					if(graph.getEdge(edgeId) == null) {
						System.out.println(edgeId);
						graph.addEdge(edgeId, a, b);
					} else {
						System.out.println("Could not add edge " + edgeId);
					}
	
					// Sleep for a while
					Thread.sleep(DELAY + rand.nextInt(max + 1 -min) + min);
					
				} catch (Exception e) {
					// Catch exceptions from graph too, just to be sure
					e.printStackTrace();
				} finally {
					lock.unlock();
				}
			}
		}
		

	}
}
