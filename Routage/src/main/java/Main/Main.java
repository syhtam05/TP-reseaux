package Main;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.stream.file.FileSourceDGS;
import java.util.*;
import java.io.*;
import java.nio.file.*;

// --- STRUCTURE DE DONNÉES ---

class InterfaceReseau {
    String nom;
    Noeud proprietaire;
    public InterfaceReseau(String nom, Noeud proprietaire) {
        this.nom = nom;
        this.proprietaire = proprietaire;
    }
}

class Liaison {
    InterfaceReseau intf1, intf2;
    int poids;
    public Liaison(InterfaceReseau i1, InterfaceReseau i2, int p) {
        this.intf1 = i1;
        this.intf2 = i2;
        this.poids = p;
    }
    
    public InterfaceReseau obtenirAutreInterface(Noeud actuel) {
        // Remplacement du ternaire par IF/ELSE
        if (this.intf1.proprietaire == actuel) {
            return this.intf2;
        } else {
            return this.intf1;
        }
    }
}

class Noeud {
    String nom;
    List<Liaison> liaisons = new ArrayList<>();
    public Noeud(String nom) { this.nom = nom; }
    public void ajouterLiaison(Liaison l) { this.liaisons.add(l); }
}

// --- LOGIQUE RÉSEAU ---

class GestionnaireReseau {
    List<Noeud> tousLesNoeuds = new ArrayList<>();

    public void chargerDepuisFichier(String cheminFichier) throws IOException {
        List<String> lignes = Files.readAllLines(Paths.get(cheminFichier));
        for (String ligne : lignes) {
            String[] p = ligne.trim().split("\\s+");
            if (p.length < 2) continue;

            if (p[0].equals("an")) {
                tousLesNoeuds.add(new Noeud(p[1]));
            }
            if (p[0].equals("ae")) {
                Noeud s = trouver(p[2]), d = trouver(p[3]);
                if (s != null && d != null) {
                    // Analyse du poids (weight:X)
                    int w = 1;
                    if (p.length > 4 && p[4].contains(":")) {
                        w = Integer.parseInt(p[4].split(":")[1]);
                    }
                    Liaison l = new Liaison(new InterfaceReseau("p_"+d.nom, s), new InterfaceReseau("p_"+s.nom, d), w);
                    s.ajouterLiaison(l); d.ajouterLiaison(l);
                }
            }
        }
    }

    public Noeud trouver(String nom) {
        for (Noeud n : tousLesNoeuds) if (n.nom.equals(nom)) return n;
        return null;
    }

    public List<Noeud> calculerChemin(Noeud dep, Noeud arr) {
        if (dep == null || arr == null) return new ArrayList<>();
        Map<Noeud, Integer> dist = new HashMap<>();
        Map<Noeud, Noeud> parents = new HashMap<>();
        PriorityQueue<Noeud> pq = new PriorityQueue<>(Comparator.comparingInt(n -> dist.getOrDefault(n, 999999)));
        
        for (Noeud n : tousLesNoeuds) dist.put(n, 999999);
        dist.put(dep, 0); pq.add(dep);

        while (!pq.isEmpty()) {
            Noeud u = pq.poll();
            if (u == arr) break;
            for (Liaison l : u.liaisons) {
                Noeud v = l.obtenirAutreInterface(u).proprietaire;
                int alt = dist.get(u) + l.poids;
                if (alt < dist.get(v)) {
                    dist.put(v, alt);
                    parents.put(v, u);
                    pq.add(v);
                }
            }
        }
        LinkedList<Noeud> chemin = new LinkedList<>();
        for (Noeud n = arr; n != null; n = parents.get(n)) chemin.addFirst(n);
        return chemin;
    }

    // VERSION SÉCURISÉE SANS TERNAIRE
    public void afficherToutesLesTablesDeRoutage() {
        System.out.println("\n=== TABLES DE ROUTAGE IP ===");
        for (Noeud n : tousLesNoeuds) {
            System.out.println("Table de " + n.nom + ":");
            for (Noeud dest : tousLesNoeuds) {
                if (n == dest) continue;
                List<Noeud> path = calculerChemin(n, dest);
                if (path.size() > 1) {
                    Noeud next = path.get(1);
                    for (Liaison l : n.liaisons) {
                        if (l.obtenirAutreInterface(n).proprietaire == next) {
                            InterfaceReseau out;
                            if (l.intf1.proprietaire == n) {
                                out = l.intf1;
                            } else {
                                out = l.intf2;
                            }
                            System.out.println("  -> Dest: " + dest.nom + " | Sortie: " + out.nom);
                            break;
                        }
                    }
                }
            }
        }
    }

    // VERSION SÉCURISÉE SANS TERNAIRE
    public void afficherCircuits(List<Noeud> chemin) {
        System.out.println("\n--- CIRCUITS VIRTUELS (VCI) ---");
        int vci = 100;
        for (int i = 0; i < chemin.size() - 1; i++) {
            Noeud actuel = chemin.get(i);
            Noeud suivant = chemin.get(i + 1);
            for (Liaison l : actuel.liaisons) {
                InterfaceReseau vers = l.obtenirAutreInterface(actuel);
                if (vers.proprietaire == suivant) {
                    InterfaceReseau de;
                    if (l.intf1.proprietaire == actuel) {
                        de = l.intf1;
                    } else {
                        de = l.intf2;
                    }
                    System.out.println("["+actuel.nom+"] Port "+de.nom+" --(VCI:"+vci+")--> ["+suivant.nom+"] Port "+vers.nom);
                    vci++; 
                    break;
                }
            }
        }
    }

    public void afficherGraphique(String fichierDGS, List<Noeud> chemin) {
	    System.setProperty("org.graphstream.ui", "swing");
	    Graph graph = new SingleGraph("Topologie");

	    // --- STYLE MIS À JOUR POUR LES POIDS ---
	    String style = "node { " +
	                   "   size: 40px; fill-color: #2c3e50; text-mode: normal; " +
	                   "   text-alignment: center; text-color: white; text-size: 16px; " +
	                   "} " +
	                   "node.path { fill-color: #e74c3c; } " +
	                   "edge { " +
	                   "   fill-color: #bdc3c7; stroke-width: 2px; " +
	                   "   text-size: 14px; text-color: #2c3e50; " +
	                   "   text-background-mode: rounded-box; " + // Fond pour le poids
	                   "   text-background-color: #ffffff; " +
	                   "   text-padding: 3px; " +
	                   "} " +
	                   "edge.path { fill-color: #e74c3c; stroke-width: 5px; }";
	    
	    graph.setAttribute("ui.stylesheet", style);

	    FileSourceDGS source = new FileSourceDGS();
	    source.addSink(graph);
	    try { 
	        source.readAll(fichierDGS); 
	    } catch (Exception e) { 
	        e.printStackTrace(); 
	    }

	    // Affichage des noms des noeuds
	    for (Node n : graph) {
	        n.setAttribute("ui.label", n.getId());
	    }

	    // --- AJOUT DU POIDS SUR LES LIGNES ---
	    // On parcourt les liaisons pour récupérer le poids et l'afficher
	    for (Noeud n : tousLesNoeuds) {
	        for (Liaison l : n.liaisons) {
	            Noeud voisin = l.obtenirAutreInterface(n).proprietaire;
	            Edge ge = graph.getEdge(n.nom + "-" + voisin.nom);
	            if (ge == null) ge = graph.getEdge(voisin.nom + "-" + n.nom);
	            
	            if (ge != null) {
	                ge.setAttribute("ui.label", l.poids); // Affiche le poids
	            }
	        }
	    }

	    // Coloration du chemin plus court
	    for (Noeud n : chemin) {
	        Node gn = graph.getNode(n.nom);
	        if (gn != null) gn.setAttribute("ui.class", "path");
	    }

	    for (int i = 0; i < chemin.size() - 1; i++) {
	        Node n1 = graph.getNode(chemin.get(i).nom);
	        Node n2 = graph.getNode(chemin.get(i+1).nom);
	        if (n1 != null && n2 != null) {
	            Edge e = n1.getEdgeBetween(n2);
	            if (e != null) e.setAttribute("ui.class", "path");
	        }
	    }

	    graph.display(true);
	}}

public class Main {
    public static void main(String[] args) {
        GestionnaireReseau gr = new GestionnaireReseau();
        String nomFichier = "reseau.dgs"; 
        
        try {
            gr.chargerDepuisFichier(nomFichier);
            Scanner sc = new Scanner(System.in);
            System.out.print("Depart : "); String s = sc.next();
            System.out.print("Arrivee : "); String d = sc.next();

            Noeud start = gr.trouver(s);
            Noeud end = gr.trouver(d);

            if (start != null && end != null) {
                List<Noeud> chemin = gr.calculerChemin(start, end);
                gr.afficherToutesLesTablesDeRoutage();
                gr.afficherCircuits(chemin);
                gr.afficherGraphique(nomFichier, chemin);
            } else {
                System.out.println("Noeuds incorrects.");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}