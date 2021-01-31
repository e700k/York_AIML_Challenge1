import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class AutonomousVehicleNavigator {

    private final int[][] _grid;
    private final int _gridId;
    private ArrayList<SearchNode> _exploredSet;
    private ArrayList<SearchNode> _solution;
    private final GridState _startingState;
    static final int _jammedMarker = 1;
    static final int _goalMarker = 3;
    static final int _startMarker = 2;
    static final int _clearMarker = 0;

    public AutonomousVehicleNavigator(int gridId) {
        _gridId = gridId;
        _grid = GridFactory.getGrid(gridId);
        _exploredSet = new ArrayList<>();
        _startingState = GridFactory.getStartingState(gridId);
    }

    public ArrayList<SearchNode> Solve() {
        for (int depthLimit = 0; depthLimit < 100; depthLimit++) {
            _solution = new ArrayList<>();
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

    public int getExploredCount() {
        return _exploredSet.size();
    }

    private boolean isJammed(GridState state) {
        return _grid[state.posY][state.posX] == _jammedMarker;
    }

    private boolean isGoalState(GridState state) {
        return _grid[state.posY][state.posX] == _goalMarker;
    }

    static class GridFactory {
        static final double _randomGridJammedRatio = 0.18;
        static final int _randomGridWidth = 50;
        static final int _randomGridHeight = 50;

        static int[][] _randomGrid;

        public static int[][] getGrid(int gridId) {
            switch (gridId) {
                case 0:
                    if (_randomGrid == null)
                        _randomGrid = randomizeGrid();
                    return _randomGrid;
                case 1: return grid1;
                case 2: return grid2;
                case 3: return grid3;
                case 4: return grid4;
                case 5: return grid5;
                case 6: return grid6;
            }
            return null;
        }

        static int[][] randomizeGrid() {
            int[][] result = new int[_randomGridHeight][_randomGridWidth];
            Random random = new Random(new Random().nextInt());

            for (int y = 0; y < result.length; y++)
                for (int x = 0; x < result[y].length; x++)
                    if (random.nextDouble() < _randomGridJammedRatio)
                        result[y][x] = _jammedMarker;
                    else
                        result[y][x] = _clearMarker;

            result[random.nextInt(_randomGridHeight)][random.nextInt(_randomGridWidth)] = _startMarker;
            result[random.nextInt(_randomGridHeight)][random.nextInt(_randomGridWidth)] = _goalMarker;

            return result;
        }

        static GridState getStartingState(int gridId) {
            int[][] grid = getGrid(gridId);
            assert grid != null;

            for (int y = 0; y < grid.length; y++)
                for (int x = 0; x < grid[y].length; x++)
                    if (grid[y][x] == _startMarker)
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

            return null;
        }

        static String GetMapAtState(SearchNode[] nodes, int gridId) {
            int[][] grid = getGrid(gridId);
            assert grid != null;
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
            for (String[] strings : strGrid)
                rows.add(String.join(" ", strings));
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
                {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 3},
                {0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 0, 0, 1, 0, 0},
                {0, 0, 1, 0, 0, 1, 0, 1, 1, 0, 0, 2, 1, 0, 0},
                {0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
                {0, 1, 1, 1, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0},
                {1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 1, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0},
                {0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                {0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                {0, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
                {0, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
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
        Collections.shuffle(actions);
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

