import java.io.*;
import java.util.*;

public class Portfolio3 {
    public static void main(String[] args) {
        MatrixGraph g = new MatrixGraph();
        //Graph g = new EdgeGraph();

        // Load edges from the file and insert them into the graph in both directions
        for (String s : loadStrings("combi.txt")) {
            // Trim the spaces around each part of the split string before parsing
            String[] a = s.split(",");
            g.insertEdge(a[0].trim(), a[1].trim(), Integer.parseInt(a[2].trim()));
            g.insertEdge(a[1].trim(), a[0].trim(), Integer.parseInt(a[2].trim()));
        }

        //Set<Edge> mst = minimumSpanningTree(g);
        //System.out.println(mst);

        // Print the graph structure
        g.printGraph();

        HashSet<Vertex> visited = new HashSet<>();
        g.visitDepthFirst(g.vertex("DATA"), visited);

        // Call countConnectedComponents() on the MatrixGraph instance
        g.countConnectedComponents();

        g.printNonOverlappingGroups();
    }

        static Set<Edge> minimumSpanningTree(Graph g){
            Collection<Edge> edges=g.edges();
            HashSet<Edge> mst=new HashSet<>();
            HashSet<Vertex> frontier=new HashSet<>();
            for(Edge e:edges){frontier.add(e.from);break;}
            while(true) {
                Edge nearest = null;
                for (Edge e : edges) {
                    if (!frontier.contains(e.from)) continue;
                    if (frontier.contains(e.to)) continue;
                    if (nearest == null || nearest.weight > e.weight)
                        nearest = e;
                }
                if(nearest==null)break;
                mst.add(nearest);
                frontier.add(nearest.to);
            }
            return mst;
        }

    static ArrayList<String> loadStrings(String f) {
        ArrayList<String> list = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(f));
            String s;
            while ((s = in.readLine()) != null) {
                list.add(s);
            }
            in.close();
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return null;
        }
        return list;
    }
}



// Abstract graph class with only essential methods
abstract class Graph {
    abstract void insertEdge(String v, String u, int w);
    abstract void printGraph();
    private HashMap<String, Vertex> vertex = new HashMap<>();
    public Vertex vertex(String s) {
        if (!vertex.containsKey(s)) vertex.put(s, new Vertex(s));
        return vertex.get(s);
    }
    public Collection<Vertex> vertices() {
        return vertex.values();
    }
    abstract Collection<Edge> edges();
    abstract Collection<Edge> outEdge(Vertex v);
    public void visitDepthFirst(Vertex v, Set<Vertex> visited) {
        if (visited.contains(v)) return;
        visited.add(v); // Mark as visited
        for (Edge e : outEdge(v)) {
            visitDepthFirst(e.to, visited);
        }
    }
}

class Vertex{
    String name;
    Vertex(String s){name=s;}
    public String toString(){return name;}
}

class Edge{
    Vertex from,to;
    int weight;
    Edge(Vertex from,Vertex to,int w){this.from=from; this.to=to; weight=w;}
    public String toString(){return from.name+" - "+weight+" -> "+to.name; }
}



class MatrixGraph extends Graph {
    private ArrayList<ArrayList<Integer>> matrix = new ArrayList<>();
    private ArrayList<Vertex> vertexList = new ArrayList<>();
    private HashMap<Vertex, Integer> vertexIndex = new HashMap<>();
    void insertEdge(String u, String v, int w) {
        Vertex u1 = vertex(u);
        Vertex v1 = vertex(v);
        if (!vertexIndex.containsKey(u1)) {
            vertexIndex.put(u1, vertexList.size());
            vertexList.add(u1);
        }
        if (!vertexIndex.containsKey(v1)) {
            vertexIndex.put(v1, vertexList.size());
            vertexList.add(v1);
        }
        setMatrix(vertexIndex.get(u1), vertexIndex.get(v1), w);
    }


    void printGraph() {
        // Set a fixed column width for the name column based on the longest name length
        int nameColumnWidth = 12; // Add some padding for aesthetics

        // Set a fixed column width for the other columns
        int columnWidth = 6;

        // Prepare to print the column headers vertically
        String[] headers = new String[nameColumnWidth];

        // Build each line of the vertical header
        for (int i = 0; i < nameColumnWidth; i++) {
            StringBuilder line = new StringBuilder(" ".repeat(nameColumnWidth)); // Start with space for name column
            for (Vertex v : vertexList) {
                // Print each character of the vertex name vertically, or a space if shorter
                line.append(i < v.name.length() ? v.name.charAt(i) : ' ');
                line.append(" ".repeat(columnWidth - 1)); // Add spacing between columns
            }
            headers[i] = line.toString();
        }

        // Print the vertical headers
        for (String line : headers) {
            System.out.println(line);
        }

        // Print each row of the matrix with formatted spacing
        for (int i = 0; i < vertexList.size(); i++) {
            // Print the vertex name with dynamic column width
            System.out.printf("%-" + nameColumnWidth + "s", vertexList.get(i).name);
            ArrayList<Integer> row = matrix.get(i);
            for (int j = 0; j < vertexList.size(); j++) {
                Integer value = (j < row.size() && row.get(j) != null) ? row.get(j) : 0;
                System.out.printf("%-" + columnWidth + "d", value); // Print edge weight with fixed column width
            }
            System.out.println();
        }
        System.out.println();
    }

    Collection<Edge> edges() {
        return List.of();
    }

    Collection<Edge> outEdge(Vertex v) {
        List<Edge> edges = new ArrayList<>();
        Integer index = vertexIndex.get(v);
        if (index == null || index >= matrix.size()) return edges;

        ArrayList<Integer> row = matrix.get(index);
        for (int j = 0; j < vertexList.size(); j++) {
            if (j < row.size() && row.get(j) != null && row.get(j) > 0) {
                edges.add(new Edge(v, vertexList.get(j), row.get(j)));
            }
        }
        return edges;
    }


    private void setMatrix(int i, int j, int w) {
        // Ensure the matrix has enough rows for index `i`
        while (matrix.size() <= i) {
            matrix.add(new ArrayList<>(Collections.nCopies(matrix.size(), null)));
        }

        // Ensure the matrix has enough rows for index `j`
        while (matrix.size() <= j) {
            matrix.add(new ArrayList<>(Collections.nCopies(matrix.size(), null)));
        }

        // Expand each row to the current matrix size, to ensure each row has enough columns
        for (ArrayList<Integer> row : matrix) {
            while (row.size() < matrix.size()) {
                row.add(null);
            }
        }

        // Set the weight for both directions (assuming an undirected graph)
        matrix.get(i).set(j, w);
        matrix.get(j).set(i, w);  // Ensure symmetry in the matrix
    }


    public int countConnectedComponents() {
        HashSet<Vertex> visited = new HashSet<>();
        int components = 0;
        System.out.println("Connected Components:");
        for (Vertex v : vertices()) {
            if (!visited.contains(v)) {
                // New connected component found
                HashSet<Vertex> componentVertices = new HashSet<>();
                visitDepthFirst(v, componentVertices);

                // Print component summary
                System.out.println("Component " + (components + 1) + ": " + componentVertices);
                visited.addAll(componentVertices);
                components++;
            }
        }
        System.out.println("Total connected components: " + components);
        return components;
    }

    void printNonOverlappingGroups() {
        HashSet<Vertex> visited = new HashSet<>();
        int groupCount = 1;

        for (Vertex data : vertices()) {
            if (visited.contains(data)) continue;

            // Initialize the group with the starting vertex
            HashSet<Vertex> group = new HashSet<>();
            group.add(data);

            // Find potential group members with no student overlap
            for (Vertex potential : vertices()) {
                if (group.contains(potential) || visited.contains(potential)) continue;

                boolean canBeAdded = true;
                for (Vertex member : group) {
                    if (getWeight(this, member, potential) != 0) { // Check if there's student overlap
                        canBeAdded = false;
                        break;
                    }
                }

                if (canBeAdded) group.add(potential);
            }

            // Mark all vertices in this group as visited
            visited.addAll(group);

            // Print the group with its number
            System.out.println("Group " + groupCount + ": " + group);
            groupCount++;
        }
    }

    static int getWeight(Graph g, Vertex v, Vertex w) {
        for(Edge e:g.outEdge(v))
            if(e.to == w) return e.weight;
        return 0;
    }

    boolean isValidGroup(Set<Vertex> group) {
        for (Vertex v1 : group) {
            for (Vertex v2 : group) {
                if (v1 != v2 && getWeight(this, v1, v2) != 0) {
                    return false; // Overlap found, group is not valid
                }
            }
        }
        return true; // No overlap found, group is valid
    }
}