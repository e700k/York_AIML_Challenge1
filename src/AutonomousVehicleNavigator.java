import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class AutonomousVehicleNavigator {

    private final int[][] _grid;
    private final int _gridId;
    private ArrayList<SearchNode> _frontier;
    private ArrayList<SearchNode> _exploredSet;
    private ArrayList<SearchNode> _solution;
    private final GridState _startingState;

    public AutonomousVehicleNavigator(int gridId) {
        _gridId = gridId;
        _grid = GridFactory.getGrid(gridId);
        _frontier = new ArrayList<>();
        _exploredSet = new ArrayList<>();
        _startingState = GridFactory.getStartingState(gridId);
    }

    public ArrayList<SearchNode> Solve() {
        for (int depthLimit = 0; depthLimit < 100; depthLimit++) {
            _solution = new ArrayList<>();
            _frontier = new ArrayList<>();
            _exploredSet = new ArrayList<>();
            SearchStatus result = PerformDepthLimitedSearch(new SearchNode(_startingState, 0), depthLimit);
            if (result == SearchStatus.Solution)
                return _solution;
        }
        return null;
    }

    private SearchStatus PerformDepthLimitedSearch(SearchNode startNode, int depthLimit) {
        String indent = " ".repeat(startNode.depth);
        boolean hasCutOff = false;
        _solution.add(startNode);
        System.out.println(indent + "Starting expanding from " + startNode.toString());

        if (isGoalState(startNode.gridState)) {
            return SearchStatus.Solution;
        }

        if (depthLimit == 0) {
            System.out.println(indent + "CutOff: " + startNode.toString());
            return SearchStatus.CutOff;
        }

        _exploredSet.add(startNode);

        for (MapDirection direction : startNode.gridState.getActions(GridFactory.getGridSize(_gridId))) {
            SearchNode child = new SearchNode(GridFactory.getNewGridState(_gridId, startNode.gridState, direction),
                    startNode.depth + 1);
            System.out.println(indent + "Direction: " + direction.toString() + " - Checking " + child.toString());

            if (isGoalState(child.gridState)) {
                _solution.add(child);
                return SearchStatus.Solution;
            }

            if (isJammed(child.gridState)) {
                System.out.println(indent + "This node is jammed.");
                continue;
            }

            SearchNode inExplored = _exploredSet.stream().filter(node -> node.gridState.posX == child.gridState.posX
                    && node.gridState.posY == child.gridState.posY).findAny().orElse(null);
            if (inExplored != null) {
                if (inExplored.depth > child.depth) {
                    _exploredSet.remove(inExplored);
                    System.out.println(indent + "This node was explored at a smaller depth; revisiting.");
                }
                else {
                    System.out.println(indent + "This node is already explored");
                    continue;
                }

            }

            System.out.println(indent + "This node is clear.");

            System.out.println(indent + "Moving to " + child.toString());
            SearchStatus childStatus = PerformDepthLimitedSearch(child, depthLimit - 1);

            if (childStatus == SearchStatus.CutOff) {
                hasCutOff = true;
                _solution.remove(child);
            }

            if (childStatus == SearchStatus.Solution)
                return childStatus;

            _solution.remove(child);
        }

        if (hasCutOff)
            return SearchStatus.CutOff;

        return SearchStatus.Failure;
    }

    private boolean isClear(GridState state) {
        return _grid[state.posY][state.posX] == 0;
    }

    private boolean isJammed(GridState state) {
        return _grid[state.posY][state.posX] == 1;
    }

    private boolean isGoalState(GridState state) {
        return _grid[state.posY][state.posX] == 3;
    }

    static class GridFactory {
        public static int[][] getGrid(int gridId) {
            switch (gridId) {
                case 1: return grid1;
                case 2: return grid2;
                case 3: return grid3;
                case 4: return grid4;
                case 5: return grid5;
                case 6: return grid6;
            }
            return null;
        }

        static GridState getStartingState(int gridId) {
            int[][] grid = getGrid(gridId);
            assert grid != null;

            for (int y = 0; y < grid.length; y++)
                for (int x = 0; x < grid[y].length; x++)
                    if (grid[y][x] == 2)
                        return new GridState(y, x, grid[y][x]);

            return null;
        }

        static int[] getGridSize(int gridId) {
            int[][] grid = getGrid(gridId);
            assert grid != null;
            return new int[] {grid.length - 1, grid[0].length - 1};
        }

        static GridState getNewGridState(int gridId, GridState gridState, MapDirection direction) {
            int[][] grid = getGrid(gridId);
            assert grid != null;

            try {
                switch(direction) {
                    case North:
                        return new GridState(gridState.posY - 1, gridState.posX, grid[gridState.posY - 1][gridState.posX]);
                    case East:
                        return new GridState(gridState.posY, gridState.posX + 1, grid[gridState.posY][gridState.posX + 1]);
                    case South:
                        return new GridState(gridState.posY + 1, gridState.posX, grid[gridState.posY + 1][gridState.posX]);
                    case West:
                        return new GridState(gridState.posY, gridState.posX - 1, grid[gridState.posY][gridState.posX - 1]);
                }
            }
            catch (Exception ex) {
                System.out.println("error");
            };

            return null;
        }

        static String GetMapAtState(SearchNode[] nodes, int gridId) {
            int[][] grid = getGrid(gridId);
            String[][] strGrid = new String[grid.length][grid[0].length];

            for (int y = 0; y < grid.length; y++)
                for (int x = 0; x < grid[y].length; x++)
                    strGrid[y][x] = String.valueOf(grid[y][x]);

            for (int i = 0; i < nodes.length; i++) {
                if (i == 0)
                    strGrid[nodes[i].gridState.posY][nodes[i].gridState.posX] = "*";
                else if (i == nodes.length - 1)
                    strGrid[nodes[i].gridState.posY][nodes[i].gridState.posX] = "x";
                else
                    strGrid[nodes[i].gridState.posY][nodes[i].gridState.posX] = ".";
            }

            ArrayList<String> rows = new ArrayList<>();
            for (int y = 0; y < strGrid.length; y++)
                rows.add(String.join(" ", strGrid[y]));
            return String.join("\n", rows);
        }

        static int[][] grid1 = new int [][] {
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        };
        static int[][] grid2 = new int [][] {
                {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1},
                {0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 3, 0, 1, 0},
                {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        };
        static int[][] grid3 = new int [][] {
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
                {0, 2, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 3, 0, 0},
                {0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        };
        static int[][] grid4 = new int [][] {
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
                {0, 2, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 3, 0, 0},
                {0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        };
        static int[][] grid5 = new int [][] {
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 3, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 2, 0, 1, 1, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0}
        };
        static int[][] grid6 = new int [][] {
                {0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 3},
                {0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 0, 0},
                {0, 0, 1, 0, 0, 1, 0, 1, 1, 0, 0, 2, 1, 0, 0},
                {0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
                {0, 1, 1, 1, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0}
        };
    }
}

class SearchNode {
    public GridState gridState;
    int depth;

    public SearchNode(GridState gridStateIn, int depthIn) {
        gridState = gridStateIn;
        depth = depthIn;
    }

    public String toString() {
        return "(" + gridState.posY + "," + gridState.posX + "," + depth + ")";
    }
}

class GridState {
    int posX;
    int posY;
    int stateStatus;

    public GridState(int posYIn, int posXIn, int stateStatusIn) {
        posX = posXIn;
        posY = posYIn;
        stateStatus = stateStatusIn;
    }

    public ArrayList<MapDirection> getActions(int[] gridSize) {
        ArrayList<MapDirection> actions = new ArrayList<>();
        if (posY > 0) actions.add(MapDirection.North);
        if (posX > 0) actions.add(MapDirection.West);
        if (posY < gridSize[0]) actions.add(MapDirection.South);
        if (posX < gridSize[1]) actions.add(MapDirection.East);
        return actions;
    }
}

enum SearchStatus {
    CutOff,
    Solution,
    Failure
}

enum MapDirection {
    North,
    East,
    South,
    West
}

