package cz.nitramek;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class GraphWork {

    private final Graph graph;

    public GraphWork() {
        graph = new SingleGraph("Agents alive");
//        setGraphStyles(graph);

    }

//    private static void setGraphStyles(Graph graph) {
//        try {
//            Path stylePath = Paths.get(Main.class.getClass().getResource("/styles.css").toURI());
//            String sheetAsStr = Files.lines(stylePath).collect(Collectors.joining());
//            graph.addAttribute("ui.stylesheet", sheetAsStr);
//        } catch (URISyntaxException | IOException e1) {
//            e1.printStackTrace();
//        }
//    }

    public void visible(boolean visibile) {
        graph.display(visibile);
    }

    Node addNode(String id) {
        Node node = graph.addNode(id);
        node.addAttribute("ui.label", id);
        return node;
    }

    void classifyNode(String id, String classz) {
        graph.getNode(id).addAttribute("ui.class", classz);
    }

    void addEdge(String node1, String node2, String label) {
        Edge edge = graph.addEdge(node1 + node2, node1, node2, true);
        edge.addAttribute("ui.label", label);

    }

}
