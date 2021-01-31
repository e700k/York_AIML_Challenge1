
import javax.swing.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class AISearch {

    public static void main(String[] args) {
        //Build an array.
        int gridId = 5;
        AutonomousVehicleNavigator navigator = new AutonomousVehicleNavigator(gridId);
        ArrayList<SearchNode> result = navigator.Solve();

        if (navigator != null) {
            System.out.println("Solution found:");
            for (int j = 0; j < result.size(); j++) {
                System.out.println(result.get(j).toString() + " -> ");
                System.out.println(AutonomousVehicleNavigator.GridFactory.GetMapAtState(
                        result.stream().limit(j + 1).collect(Collectors.toList()).toArray(new SearchNode[j + 1]),
                        gridId));
            }
        }
    }
}