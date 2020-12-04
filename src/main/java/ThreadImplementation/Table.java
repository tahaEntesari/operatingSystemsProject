package ThreadImplementation;

import ThreadImplementation.Controller;
import ThreadImplementation.SingleSpeciesThread;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class Table {
    private int numberOfRows, numberOfColumns;
    private int[][][] table;
    private ArrayList[][] tableInhabitants;
    private int cellCapacity;
    private int lastRowNumber, lastColumnNumber;
    private Semaphore tableIsBeingUsed = new Semaphore(1);
    private Semaphore mutex = new Semaphore(1);
    private Controller controller;
    private boolean killSmallestFirst, dontKillDiagonal;
    private ArrayList[] locations;
    private int numberOfThreadsInLineForTable;
    private int r;

    public Table(int numberOfRows, int numberOfColumns, int cellCapacity, int r, int s, Controller controller,
                 boolean killSmallestFirst, boolean dontKillDiagonal) {
        this.numberOfRows = numberOfRows;
        this.numberOfColumns = numberOfColumns;
        this.cellCapacity = cellCapacity;
        lastRowNumber = numberOfRows - 1;
        lastColumnNumber = numberOfColumns - 1;
        tableInhabitants = new ArrayList[numberOfRows][numberOfColumns];
        this.controller = controller;
        this.killSmallestFirst = killSmallestFirst;
        this.dontKillDiagonal = dontKillDiagonal;
        locations = new ArrayList[r + 1];
        numberOfThreadsInLineForTable = 0;
        this.r = r;

        table = new int[numberOfRows][numberOfColumns][2]; // First value: species type. Second value: number of species in cell
        for (int i = 0; i < numberOfRows; i++) {
            for (int j = 0; j < numberOfColumns; j++) {
                table[i][j][0] = -1;
                table[i][j][1] = 0;
                tableInhabitants[i][j] = new ArrayList();
            }
        }
        int nOverR = numberOfRows / r;
        int mOverS = numberOfColumns / s;
        int rowIndex, columnIndex;
        for (int i = 1; i <= r; i++) {
            locations[i] = new ArrayList();
            for (int j = 1; j <= s; j++) {
                rowIndex = i * nOverR - 1;
                columnIndex = j * mOverS - 1;
                locations[i].add(new Point(rowIndex, columnIndex));
                table[rowIndex][columnIndex][0] = i;
                table[rowIndex][columnIndex][1] = 1;
                SingleSpeciesThread specimen =
                        new SingleSpeciesThread(i, rowIndex, columnIndex, this, controller);
                tableInhabitants[rowIndex][columnIndex].add(specimen);
                specimen.start();
            }
        }
    }


    public void acquireTable() {
        try {
            mutex.acquire();
            numberOfThreadsInLineForTable++;
            mutex.release();
            tableIsBeingUsed.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void releaseTable() {
        try {
            mutex.acquire();
            numberOfThreadsInLineForTable--;
            tableIsBeingUsed.release();
            mutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public int getNumberOfThreadsInLineForTable() {
        return numberOfThreadsInLineForTable;
    }

    public synchronized void removeSpeciesFromCell(SingleSpeciesThread specimen) {
        Point cell = specimen.getCurrentLocation();
        table[cell.x][cell.y][1]--;
        tableInhabitants[cell.x][cell.y].remove(specimen);
        if (table[cell.x][cell.y][1] == 0) {
            table[cell.x][cell.y][0] = -1;
            locations[specimen.getSpeciesType()].remove(cell);
        }
    }

    public synchronized void moveSpecies(Point newLocation, SingleSpeciesThread specimen) throws Exception {
//        System.out.println("Moving species ...");
        int speciesType = specimen.getSpeciesType();
        if (table[newLocation.x][newLocation.y][0] != -1 && table[newLocation.x][newLocation.y][0] != speciesType) {
            throw new Exception("Error. New location invalid");
        }
        removeSpeciesFromCell(specimen);
        if (table[newLocation.x][newLocation.y][0] == -1) {
            table[newLocation.x][newLocation.y][0] = speciesType;
            locations[speciesType].add(newLocation);
        }

        table[newLocation.x][newLocation.y][1]++;
        tableInhabitants[newLocation.x][newLocation.y].add(specimen);
        specimen.setCurrentLocation(newLocation);
//        System.out.println("Moved species");
    }


    public ArrayList<Point> getAdjacentCells(Point mainCell, boolean getDiagonalAsWell) {
        ArrayList<Point> adjacentCells = new ArrayList<>();
        int x = mainCell.x;
        int y = mainCell.y;
        if (y - 1 >= 0) adjacentCells.add(new Point(x, y - 1));
        if (y + 1 <= lastColumnNumber) adjacentCells.add(new Point(x, y + 1));
        if (x - 1 >= 0) adjacentCells.add(new Point(x - 1, y));
        if (x + 1 <= lastRowNumber) adjacentCells.add(new Point(x + 1, y));
        if (getDiagonalAsWell) {
            if (x - 1 >= 0) {
                if (y - 1 >= 0) adjacentCells.add(new Point(x - 1, y - 1));
                if (y + 1 <= lastColumnNumber) adjacentCells.add(new Point(x - 1, y + 1));
            }
            if (x + 1 <= lastRowNumber) {
                if (y + 1 <= lastColumnNumber) adjacentCells.add(new Point(x + 1, y + 1));
                if (y - 1 >= 0) adjacentCells.add(new Point(x + 1, y - 1));
            }
        }
        return adjacentCells;
    }

    private ArrayList<Point> getNonEnemyAdjacentCells(ArrayList<Point> adjacentCells, int currentSpeciesIndex) {
        ArrayList<Point> nonEnemy = new ArrayList<>(adjacentCells);
        for (Point adjacentCell : adjacentCells) {
            if (table[adjacentCell.x][adjacentCell.y][0] != -1
                    && table[adjacentCell.x][adjacentCell.y][0] != currentSpeciesIndex) {
                nonEnemy.remove(adjacentCell);
            }
        }
        return nonEnemy;
    }

    public ArrayList<Point> getNonEnemyAdjacentCells(Point currentCell, boolean getDiagonalAsWell) {
        int currentSpeciesIndex = table[currentCell.x][currentCell.y][0];
        ArrayList<Point> adjacentCells = this.getAdjacentCells(currentCell, getDiagonalAsWell);
        return getNonEnemyAdjacentCells(adjacentCells, currentSpeciesIndex);
    }

    public ArrayList<Point> getNonEnemyAdjacentCellsWithCapacity(Point currentCell, boolean getDiagonalAsWell, int speciesType) {
        ArrayList<Point> possibleCells = getNonEnemyAdjacentCells(currentCell, getDiagonalAsWell);
        ArrayList<Point> finalCells = new ArrayList<>(possibleCells);
        for (Point possibleCell : possibleCells) {
            if (!cellHasCapacityForSpecies(possibleCell, speciesType)) finalCells.remove(possibleCell);
        }
        return finalCells;
    }

    public ArrayList<Point> getEnemyAdjacentCells(Point currentCell, boolean getDiagonalAsWell) {
        int currentSpeciesIndex = table[currentCell.x][currentCell.y][0];
        ArrayList<Point> adjacentCells = this.getAdjacentCells(currentCell, getDiagonalAsWell);
        ArrayList<Point> nonEnemy = getNonEnemyAdjacentCells(adjacentCells, currentSpeciesIndex);
        adjacentCells.removeAll(nonEnemy);
        return adjacentCells;
    }

    private boolean cellHasCapacityForSpecies(Point cell, int speciesType) {
        int existingSpeciesType = table[cell.x][cell.y][0];
        if (existingSpeciesType != speciesType && existingSpeciesType != -1) {
            try {
                throw new Exception("Error. This cell cannot be given to the mentioned species type");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-4);
            }
        }
        int currentNumberOfSpeciesInCell = table[cell.x][cell.y][1];
        return (currentNumberOfSpeciesInCell + 1) * speciesType <= cellCapacity;
    }

    public void handleOverPopulationDeath() {
        for (int i = 0; i < numberOfRows; i++) {
            for (int j = 0; j < numberOfColumns; j++) {
                while (table[i][j][0] * table[i][j][1] > cellCapacity) {
                    SingleSpeciesThread specimen = ((SingleSpeciesThread) tableInhabitants[i][j].remove(0));
                    killSpecimen(specimen);
                }
            }
        }
    }

    public boolean willBeKilledInBattle(ArrayList<Point> validEnemyAdjacentCells, int currentCellScore) {
        int[] status = new int[r + 1];
        Arrays.fill(status, 0);
        for (Point enemyAdjacentCell : validEnemyAdjacentCells) {
            status[table[enemyAdjacentCell.x][enemyAdjacentCell.y][0]] +=
                    table[enemyAdjacentCell.x][enemyAdjacentCell.y][1];
        }
        boolean flag = false;
        for (int i = 1; i <= r; i++) {
            if (currentCellScore < i * status[i]) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public boolean handleWhenDontKillDiagonalIsTrue(Point cell) {
        int currentCellScore = getCellScore(cell);
        ArrayList<Point> allEnemyAdjacentCells = getEnemyAdjacentCells(cell, true);
        if (table[cell.x][cell.y][0] <= r / 2) {
            // Do nothin
        } else {
            int x = cell.x;
            int y = cell.y;
            int currentSpeciesType = table[x][y][0];
            ArrayList<Point> nonDiagonalAdjacentCells = getEnemyAdjacentCells(cell, false);
            ArrayList<Point> diagonalAdjacentCells = new ArrayList<>(allEnemyAdjacentCells);
            diagonalAdjacentCells.removeAll(nonDiagonalAdjacentCells);
            for (Point diagonalAdjacentCell : diagonalAdjacentCells) {
                int enemyType = table[diagonalAdjacentCell.x][diagonalAdjacentCell.y][0];
                if (enemyType < currentSpeciesType) {
                    allEnemyAdjacentCells.remove(diagonalAdjacentCell);
                }
            }
        }
        return willBeKilledInBattle(allEnemyAdjacentCells, currentCellScore);
    }

    public void handleAttackDeath() {
        if (killSmallestFirst) {
            for (int i = 1; i < r + 1; i++) {
                ArrayList<Point> toKill = new ArrayList<>();
                for (Object o : locations[i]) {
                    Point cell = (Point) o;
                    int currentCellScore = getCellScore(cell);
                    if (currentCellScore <= 0) {
                        System.out.println("This shouldn't have happened. exiting");
                        System.exit(-6);
                    }

                    boolean willThereBeGenocide;
                    if (dontKillDiagonal) {
                        willThereBeGenocide = handleWhenDontKillDiagonalIsTrue(cell);
                    } else {
                        willThereBeGenocide = willBeKilledInBattle(getEnemyAdjacentCells(cell, true)
                                , currentCellScore);
                    }
                    if (willThereBeGenocide) {
                        toKill.add(cell);
                    }
                }
                for (Point cell : toKill) {
                    killAllInCell(cell);
                }
            }
        } else {
            while (true) {
                int i = 0, j = 0;
                outer:
                for (; i < numberOfRows; i++) {
                    j = 0;
                    for (; j < numberOfColumns; j++) {
                        Point currentCell = new Point(i, j);
                        int currentCellScore = getCellScore(currentCell);
                        if (currentCellScore <= 0) continue;
                        boolean willThereBeGenocide;
                        if (dontKillDiagonal) {
                            willThereBeGenocide = handleWhenDontKillDiagonalIsTrue(currentCell);
                        } else {
                            willThereBeGenocide = willBeKilledInBattle(
                                    getEnemyAdjacentCells(currentCell, true), currentCellScore);
                        }
                        if (willThereBeGenocide) {
                            killAllInCell(currentCell);
                            break outer;
                        }
                    }
                }
                if (i == numberOfRows && j == numberOfColumns) break;
            }
        }
    }

    private void killAllInCell(Point cell) {
        ArrayList copied = new ArrayList(tableInhabitants[cell.x][cell.y]);
        for (Object o : copied) {
            killSpecimen((SingleSpeciesThread) o);
        }
    }

    private void killSpecimen(SingleSpeciesThread specimen) {
        removeSpeciesFromCell(specimen);
        specimen.setAlive(false);
    }

    private int getCellScore(Point cell) {
        return table[cell.x][cell.y][0] * table[cell.x][cell.y][1];
    }

    public void reproduce(Point cell) {
        if (table[cell.x][cell.y][0] == -1) {
            try {
                throw new Exception("Error. This cell is not inhabited by any species");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-3);
            }
        }
        table[cell.x][cell.y][1]++;
        SingleSpeciesThread newSpecimen = new SingleSpeciesThread(
                table[cell.x][cell.y][0], cell.x, cell.y, this, controller);
        tableInhabitants[cell.x][cell.y].add(newSpecimen);
        newSpecimen.start();
    }

    public boolean allHaveReproduced() {
        for (int i = 0; i < numberOfRows; i++) {
            for (int j = 0; j < numberOfColumns; j++) {
                for (Object o : tableInhabitants[i][j]) {
                    if (!((SingleSpeciesThread) o).reproductionDoneIfNecessary()) return false;
                }
            }
        }
        return true;
    }

    public boolean allHaveDoneTurnEnd() {
        for (int i = 0; i < numberOfRows; i++) {
            for (int j = 0; j < numberOfColumns; j++) {
                for (Object o : tableInhabitants[i][j]) {
                    if (!((SingleSpeciesThread) o).didTheTurnEndStuff()) return false;
                }
            }
        }
        return true;
    }

    public boolean allHaveDoneIntraTurnEnd() {
        for (int i = 0; i < numberOfRows; i++) {
            for (int j = 0; j < numberOfColumns; j++) {
                for (Object o : tableInhabitants[i][j]) {
                    if (!((SingleSpeciesThread) o).didIntraTurnEndStuff()) return false;
                }
            }
        }
        return true;
    }

    public void printLocations() {
        acquireTable();
        for (int i = 1; i < locations.length; i++) {
            System.out.println(locations[i]);
        }
        System.out.println("************************************************************\n");
        int speciesType;
        for (int i = 0; i < numberOfRows; i++) {
            for (int j = 0; j < numberOfColumns; j++) {
                speciesType = table[i][j][0];
                if (speciesType == -1) speciesType = 0;
                System.out.print(speciesType + "-" + table[i][j][1] + " | ");
            }
            System.out.println("\n-----------------------------------------------------------------");
        }
        releaseTable();
    }
}
