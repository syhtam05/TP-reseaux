package main;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import java.util.*;

public class ReseauApp {

    public static void main(String[] args) {
        System.setProperty("org.graphstream.ui", "swing");

        Graph graph = new SingleGraph("Topologie Reseau");
        graph.setAttribute("ui.stylesheet", getStyleSheet());
        
        // Empêcher le mouvement du graphe
        graph.setAttribute("layout.force", 0);
        graph.setAttribute("layout.quality", 0);

        // --- Construction de la topologie ---
        addNode(graph, "M1", "Machine", 0, 1);
        addNode(graph, "S1", "Switch", 1, 1);
        addNode(graph, "S2", "Switch", 2, 2);
        addNode(graph, "S3", "Switch", 2, 0);
        addNode(graph, "M2", "Machine", 3, 1);

        addEdge(graph, "M1", "S1", "eth0", "p1");
        addEdge(graph, "S1", "S2", "p2", "p1");
        addEdge(graph, "S2", "S3", "p2", "p1");
        addEdge(graph, "S1", "S3", "p3", "p2");
        addEdge(graph, "S3", "M2", "p3", "eth0");

        Viewer viewer = graph.display();
        viewer.disableAutoLayout();

        // --- Traitements ---
        calculerChemin(graph, "M1", "M2");
        genererTableRoutage(graph, "S1");
        etablirCircuitVirtuel(graph, "M1", "M2", 100);
    }

    private static void addNode(Graph g, String id, String type, double x, double y) {
        Node n = g.addNode(id);
        n.setAttribute("ui.label", id);
        n.setAttribute("ui.class", type);
        n.setAttribute("x", x);
        n.setAttribute("y", y);
    }

    private static void addEdge(Graph g, String u, String v, String portU, String portV) {
        Edge e = g.addEdge(u + "-" + v, u, v);
        e.setAttribute("port_" + u, portU);
        e.setAttribute("port_" + v, portV);
        e.setAttribute("ui.label", portU + ":" + portV);
        e.setAttribute("weight", 1);
    }

    public static void calculerChemin(Graph g, String startId, String endId) {
        Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "weight");
        dijkstra.init(g);
        dijkstra.setSource(g.getNode(startId));
        dijkstra.compute();

        Path path = dijkstra.getPath(g.getNode(endId));
        
        System.out.println("=== CHEMIN LE PLUS COURT (" + startId + " vers " + endId + ") ===");
        for (Node n : path.getNodePath()) {
            n.setAttribute("ui.style", "fill-color: red; size: 30px;");
            System.out.print(n.getId() + " ");
        }
        System.out.println("\n");
        
        for (Edge e : path.getEdgePath()) {
            // FIX: 'size' est le mot-clé correct pour l'épaisseur en GraphStream 2.0
            e.setAttribute("ui.style", "fill-color: red; size: 3px;");
        }
    }

    public static void genererTableRoutage(Graph g, String switchId) {
        Node s = g.getNode(switchId);
        if (s == null) return;

        System.out.println("=== TABLE DE ROUTAGE : " + switchId + " ===");
        System.out.println("Dest. | Interface");
        System.out.println("---------------");

        Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "weight");
        dijkstra.init(g);
        dijkstra.setSource(s);
        dijkstra.compute();

        for (Node dest : g) {
            if (dest == s) continue;
            Path p = dijkstra.getPath(dest);
            if (p.getEdgePath().size() > 0) {
                Edge firstEdge = p.getEdgePath().get(0);
                Object pAttr = firstEdge.getAttribute("port_" + switchId);
                String port = (pAttr != null) ? pAttr.toString() : "??";
                System.out.printf("%-5s | %s%n", dest.getId(), port);
            }
        }
        System.out.println();
    }

    public static void etablirCircuitVirtuel(Graph g, String src, String dst, int vciStart) {
        System.out.println("=== ÉTABLISSEMENT CIRCUIT VIRTUEL ===");
        Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "weight");
        dijkstra.init(g);
        dijkstra.setSource(g.getNode(src));
        dijkstra.compute();
        
        Path p = dijkstra.getPath(g.getNode(dst));
        List<Node> nodes = p.getNodePath();
        List<Edge> edges = p.getEdgePath();
        
        int currentVci = vciStart;
        for (int i = 1; i < nodes.size() - 1; i++) {
            Node sw = nodes.get(i);
            Edge edgeIn = edges.get(i-1);
            Edge edgeOut = edges.get(i);
            
            String portIn = edgeIn.getAttribute("port_" + sw.getId()).toString();
            String portOut = edgeOut.getAttribute("port_" + sw.getId()).toString();
            
            System.out.println("Switch " + sw.getId() + ": [In:" + portIn + ", VCI:" + currentVci + "] -> " +
                               "[Out:" + portOut + ", VCI:" + (currentVci + 1) + "]");
            currentVci++;
        }
    }

    private static String getStyleSheet() {
        return "node { size: 25px; text-size: 16; text-alignment: under; text-background-mode: rounded-box; text-background-color: white; }" +
               "node.Machine { fill-color: #3498db; shape: box; }" +
               "node.Switch { fill-color: #2ecc71; shape: diamond; }" +
               "edge { text-size: 12; fill-color: #95a5a6; }";
    }
}