import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.io.IOException;

public class AISearch {

    public static void main(String[] args) {
        int gridId = 0;
        AutonomousVehicleNavigator navigator = new AutonomousVehicleNavigator(gridId);
        ArrayList<SearchNode> result = navigator.Solve();
        StaticWaiter waiter = new StaticWaiter();

        if (result != null) {
            System.out.println("Solution found:");
            for (int j = 0; j < result.size(); j++) {
                int finalJ = j;
                waiter.staticWait(300);
                clear();
                System.out.println(result.get(j).toString() + " -> ");
                System.out.println(AutonomousVehicleNavigator.GridFactory.GetMapAtState(
                        result.stream().limit(j + 1).collect(Collectors.toList()).toArray(new SearchNode[j + 1]),
                        gridId, navigator.exploredSet.stream().filter(node -> node.depth <= finalJ).collect(Collectors.toList())));
            }
            System.out.println("Explored nodes: " + navigator.getExploredCount());
        }
        else {
            System.out.println("No solution found:");
            System.out.println(AutonomousVehicleNavigator.GridFactory.GetMapAtState(
                    new SearchNode[0],
                    gridId, navigator.exploredSet));
            System.out.println("Explored nodes: " + navigator.getExploredCount());
        }
    }

    public static void clear()
    {
        try
        {
            if (System.getProperty("os.name").contains("Windows"))
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            else
                Runtime.getRuntime().exec("clear");
        } catch (IOException | InterruptedException ex) {}
    }

}