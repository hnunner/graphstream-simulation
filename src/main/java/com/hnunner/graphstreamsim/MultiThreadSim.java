package com.hnunner.graphstreamsim;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JFrame;

import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

public class MultiThreadSim {

	public static void main(String[] args) {
		new MultiThreadSim();
	}

	private static final int DELAY = 500;

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
		Graph graph = new SingleGraph("whoop");

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
				
				//lock.lock();
				String edgeId = null;
				
				try {
					
					// Find 2 nodes to connect, using a weighted random method based on the degree of each node
					// i.e. we are more likely to choose nodes with a small degree
					
					int count = 0;

					
					Node node1 = null, node2 = null;
					
					final int maxDegree = graph.getNodeSet().stream().mapToInt(n -> n.getDegree()).max().getAsInt();
					final Map<Node, Integer> nodeMap =  graph.getNodeSet().stream().collect(Collectors.toMap(n -> n, n -> maxDegree - n.getDegree() + 1));
					
					
					do {
						double bestValue = Double.MAX_VALUE;
						
						for (Entry<Node, Integer> nodeEntry : nodeMap.entrySet()) {
							// Sample an exponential distribution 
							double value = -Math.log(rand.nextDouble()) / Double.valueOf(nodeEntry.getValue());
							
							if (value < bestValue) {
								bestValue = value;
								
								node1 = nodeEntry.getKey();
							}
						}
						
						// Temporarily remove the first chosen node so we don't pick it again
						nodeMap.remove(node1);

						bestValue = Double.MAX_VALUE;
						for (Entry<Node, Integer> nodeEntry : nodeMap.entrySet()) {
							// Sample an exponential distribution 
							double value = -Math.log(rand.nextDouble()) / Double.valueOf(nodeEntry.getValue());

							if (value < bestValue) {
								bestValue = value;
								
								node2 = nodeEntry.getKey();
							}
						}
						
						// Restore the map so we don't have to create the whole thing again
						nodeMap.put(node1, node1.getDegree());
						
						edgeId = "edge|" +  node1.getId() + "," + node2.getId();
						
					} while(edgeExists(edgeId) || count == 3);
					
					if(!edgeExists(edgeId))
						graph.addEdge(edgeId, node1, node2);
					
					// Sleep for a while
					Thread.sleep(DELAY + rand.nextInt(max + 1 -min) + min);
				} catch (EdgeRejectedException e) {
					// For some reason, sometimes an edge is still rejected!
					System.out.println("Edge rejected, " + edgeId + " edge exists: " + edgeExists(edgeId));
				} catch (Exception e) {
					// Catch exceptions from graph too, just to be sure
					e.printStackTrace();
				} finally {
					//lock.unlock();
				}
			}
		}
		
		boolean edgeExists(String edgeId) {
			return graph.getEdge(edgeId) != null;
		}
	}
}
