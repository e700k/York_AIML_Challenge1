import java.util.*;

public class VacuumSolver {
    private final StateSpace _stateSpace;
    private final LinkedList<State> _frontier;
    private final ArrayList<State> _exploredSet;
    private final int _startingState;
    private final int[] _goalStates;

    public VacuumSolver() {
        _stateSpace = new StateSpace();
        _frontier = new LinkedList<>();
        _exploredSet = new ArrayList<>();
        _startingState = 1;
        _goalStates = new int[] {7, 8};
    }

    public boolean Solve() {
        System.out.println("Solving Vacuum problem:");
        State currentState = _stateSpace.States.get(_startingState);
        System.out.println("Starting state: " + currentState.stateId);
        if (IsGoalState(currentState)) {
            System.out.println("Starting state is a goal state!");
            return true;
        }
        _frontier.add(currentState);

        do {
            currentState = _frontier.poll();
            System.out.println("Moving to state: " + currentState.stateId);
            _exploredSet.add(currentState);

            for (Action action : currentState.ActionList()) {
                System.out.println("Expanding through action [" + action.description + "]");
                State child = _stateSpace.States.get(action.result);
                System.out.println("Action would result in state: " + child.stateId);
                if (!_frontier.contains(child) && !_exploredSet.contains(child)) {
                    if (IsGoalState(child)) {
                        System.out.println("Goal state found!");
                        return true;
                    }
                    _frontier.add(child);
                }
            }
        } while (!_frontier.isEmpty());

        System.out.println("No solution was found!");
        return false;
    }

    private boolean IsGoalState(State state) {
        return Arrays.stream(_goalStates).anyMatch(x -> x == state.stateId);
    }
}

class StateSpace{
    public Dictionary<Integer, State> States;

    public StateSpace() {
        States = new Hashtable<>();
        States.put(1, new State(1, true, true, Direction.Left, new Action(1, "Left"), new Action(2, "Right"), new Action(3, "Suck")));
        States.put(2, new State(2, true, true, Direction.Right, new Action(1, "Left"), new Action(2, "Right"), new Action(6, "Suck")));
        States.put(3, new State(3, false, true, Direction.Left, new Action(3, "Left"), new Action(4, "Right"), new Action(3, "Suck")));
        States.put(4, new State(4, false, true, Direction.Right, new Action(3, "Left"), new Action(4, "Right"), new Action(8, "Suck")));
        States.put(5, new State(5, true, false, Direction.Left, new Action(5, "Left"), new Action(6, "Right"), new Action(7, "Suck")));
        States.put(6, new State(6, true, false, Direction.Right, new Action(5, "Left"), new Action(6, "Right"), new Action(6, "Suck")));
        States.put(7, new State(7, false, false, Direction.Left, new Action(7, "Left"), new Action(8, "Right"), new Action(7, "Suck")));
        States.put(8, new State(8, false, false, Direction.Right, new Action(7, "Left"), new Action(8, "Right"), new Action(8, "Suck")));
    }
}

class State {
    public int stateId;
    public boolean hasDirtLeft;
    public boolean hasDirtRight;
    public Direction vacuumPosition;
    public Action actionLeft;
    public Action actionRight;
    public Action actionSuck;

    public State(int stateIdIn, boolean hasDirtLeftIn, boolean hasDirtRightIn, Direction vacuumPositionIn,
                 Action actionLeftIn, Action actionRightIn, Action actionSuckIn) {
        stateId = stateIdIn;
        hasDirtLeft = hasDirtLeftIn;
        hasDirtRight = hasDirtRightIn;
        vacuumPosition = vacuumPositionIn;
        actionLeft = actionLeftIn;
        actionRight = actionRightIn;
        actionSuck = actionSuckIn;
    }

    public Action[] ActionList() {
        return new Action[] {actionLeft, actionRight, actionSuck};
    }
}

class Action {
    public int result;
    public String description;

    public Action(int resultIn, String descriptionIn) {
        result = resultIn;
        description = descriptionIn;
    }
}

enum Direction {
    Left,
    Right
}