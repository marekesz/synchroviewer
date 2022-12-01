
package algorithms;

import models.Automaton;
import models.InverseAutomaton;

import java.util.Arrays;

public abstract class Connectivity {

    public static boolean isStronglyConnected(Automaton automaton, InverseAutomaton inverseAutomaton) {
        if (automaton.getN() == 0)
            return false;

        boolean[] visited = new boolean[automaton.getN()];
        int vertex = 0; // choose arbitrary vertex

        dfs(vertex, visited, automaton);
        for (boolean val : visited) {
            if (!val)
                return false;
        }

        Arrays.fill(visited, false);
        dfsReversed(vertex, visited, automaton, inverseAutomaton);
        for (boolean val : visited) {
            if (!val)
                return false;
        }

        return true;
    }

    private static void dfs(int vertex, boolean[] visited, Automaton automaton) {
        visited[vertex] = true;
        int[][] matrix = automaton.getMatrix();
        for (int k = 0; k < automaton.getK(); k++) {
            if (!visited[matrix[vertex][k]])
                dfs(matrix[vertex][k], visited, automaton);
        }
    }

    private static void dfsReversed(int vertex, boolean[] visited, Automaton automaton,
            InverseAutomaton inverseAutomaton) {
        visited[vertex] = true;
        int[][][] reversedMatrix = inverseAutomaton.getMatrix();
        for (int k = 0; k < automaton.getK(); k++) {
            for (int m = 0; m < reversedMatrix[vertex][k].length; m++) {
                if (!visited[reversedMatrix[vertex][k][m]])
                    dfsReversed(reversedMatrix[vertex][k][m], visited, automaton, inverseAutomaton);
            }
        }
    }

    public static boolean isConnected(Automaton automaton, InverseAutomaton inverseAutomaton) {
        if (automaton.getN() == 0)
            return false;

        boolean[] visited = new boolean[automaton.getN()];

        int vertex = 0; // choose arbitrary vertex
        dfsUndirected(vertex, visited, automaton, inverseAutomaton);
        for (boolean val : visited) {
            if (!val)
                return false;
        }

        return true;
    }

    private static void dfsUndirected(int vertex, boolean[] visited, Automaton automaton,
            InverseAutomaton inverseAutomaton) {
        visited[vertex] = true;
        for (int k = 0; k < automaton.getK(); k++) {
            if (!visited[automaton.getMatrix()[vertex][k]])
                dfsUndirected(automaton.getMatrix()[vertex][k], visited, automaton, inverseAutomaton);
        }
        int[][][] reversedMatrix = inverseAutomaton.getMatrix();
        for (int k = 0; k < automaton.getK(); k++) {
            for (int m = 0; m < reversedMatrix[vertex][k].length; m++) {
                if (!visited[reversedMatrix[vertex][k][m]])
                    dfsUndirected(reversedMatrix[vertex][k][m], visited, automaton, inverseAutomaton);
            }
        }
    }
}
