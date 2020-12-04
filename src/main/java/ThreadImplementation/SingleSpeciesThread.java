package ThreadImplementation;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class SingleSpeciesThread extends Thread {
    private boolean alive = true;
    private final int speciesType;
    private Point currentLocation;
    private Table table;
    private Random random = new Random(System.nanoTime());
    private Controller controller;
    private int turnCounter = 0;
    private boolean didReproduce, didDoTurnEnd;
    private boolean canReproduce;
    private boolean didIntraTurnEnd;

    public SingleSpeciesThread(int speciesType, int currentRow, int currentColumn, Table table, Controller controller) {
        this.speciesType = speciesType;
        currentLocation = new Point(currentRow, currentColumn);
        this.table = table;
        this.controller = controller;
        didDoTurnEnd = didReproduce = true;
        canReproduce = false;
        didIntraTurnEnd = false;
    }

    private boolean allowedToMove() {
        return random.nextBoolean();
    }

    public void setCurrentLocation(Point currentLocation) {
        this.currentLocation = currentLocation;
    }

    private void move() {
//        System.out.println("Thread " + getId() + " in move(). Awaiting table ...");
        table.acquireTable();
//        System.out.println("Thread " + getId() + " in move(). ThreadImplementation.Table acquired.");
        ArrayList<Point> possibleAdjacentCells =
                table.getNonEnemyAdjacentCellsWithCapacity(currentLocation, true, speciesType);
//        System.out.println("Thread " + getId() + " in move(). Possible adjacent cells acquired.");
        if (possibleAdjacentCells.size() > 0) {
//            System.out.println("Thread " + getId() + " in move(). New location Chosen.");
            Point newLocation = possibleAdjacentCells.get(random.nextInt(possibleAdjacentCells.size()));
            try {
//                System.out.println("Thread " + getId() + " in move(). Trying to move");
                table.moveSpecies(newLocation, this);
            } catch (Exception e) {
//                System.out.println("Thread " + getId() + " in move(). Exception ...");
                e.printStackTrace();
                System.exit(-2);
            }
        } else {
//            System.out.println("Thread " + getId() + " in move(). No empty cell in neighbours");
        }
        table.releaseTable();
    }

    private void reproduce() {
//        System.out.println("Thread " + getId() + " in reproduce. Acquiring table ...");
        table.acquireTable();
        table.reproduce(currentLocation);
        table.releaseTable();
//        System.out.println("Thread " + getId() + " in reproduce. Finished.");
    }

    public boolean reproductionDoneIfNecessary(){
        return (didReproduce && turnCounter == 0) || (!didReproduce && turnCounter != 0);
    }

    public boolean didTheTurnEndStuff(){
        return didDoTurnEnd;
    }

    public boolean didIntraTurnEndStuff(){
        return didIntraTurnEnd;
    }

    @Override
    public void run() {
        while (alive) {
            if (controller.getCurrentTurnStatus() == TurnStatus.intraTurnEnd && !didIntraTurnEnd) {
//                System.out.println("Thread " + getId() + " in IntraTurnEnd");
                didIntraTurnEnd = true;
                didReproduce = false;
                didDoTurnEnd = false;
                canReproduce = true;
            } else if (controller.getCurrentTurnStatus() == TurnStatus.turnStart && !didReproduce && canReproduce) {
//                System.out.println("Thread " + getId() + " in reproduction");
                if (turnCounter == speciesType) {
                    turnCounter = 0;
                    reproduce();
                }
                didReproduce = true;
            } else if (controller.getCurrentTurnStatus() == TurnStatus.turnEnd && !didDoTurnEnd) {
//                System.out.println("Thread " + getId() + " in turnEnd");
                turnCounter++;
                didDoTurnEnd = true;
            } else if (controller.getCurrentTurnStatus() == TurnStatus.intraTurn) {
//                System.out.println("Thread " + getId() + " in intraTurn");
                didIntraTurnEnd = false;
                if (allowedToMove()) {
                    move();
                }
            } else {
//                System.out.println("Thread " + getId() + " sleeping for 50ms");
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Point getCurrentLocation() {
        return currentLocation;
    }

    public int getSpeciesType() {
        return speciesType;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
