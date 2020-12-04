package ProcessImplementation;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class SingleSpeciesProcess extends Thread {
    private boolean alive = true;
    private final int speciesType;
    private Point currentLocation;
    private Random random = new Random(System.nanoTime());
    private int turnCounter = 0;
    private boolean didReproduce, didDoTurnEnd;
    private boolean canReproduce;
    private boolean didIntraTurnEnd;
    private Scanner scanner;
    private String id;
    private String inLocation, outLocation;

    public SingleSpeciesProcess(int speciesType, int currentRow, int currentColumn, String id) {
        this.speciesType = speciesType;
        currentLocation = new Point(currentRow, currentColumn);
        didDoTurnEnd = didReproduce = true;
        canReproduce = false;
        didIntraTurnEnd = false;
        scanner = new Scanner(System.in);
        this.id = id;
        inLocation = "./processFiles/" + id + "IN.txt";
        outLocation = "./processFiles/" + id + "OUT.txt";

        try {
            (new File(inLocation.substring(0, inLocation.length() - 6) + "log.txt")).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeToOut(2); // This is necessary for when a new species is born
    }

    public boolean isOver() {
        boolean flag = false;
        try {
            FileReader input = new FileReader(inLocation);
            int isOverStat = input.read() - '0';
            if (isOverStat == 1) flag = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    private void writeToOut(int state) {
        try {
            FileWriter out = new FileWriter(outLocation);
            out.write(state);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean allowedToMove() {
        return random.nextBoolean();
    }

    private void move() {
        writeToLog("Thread " + getId() + " in move(). Awaiting table ...");
        sendAcquireTable();
        writeToLog("Thread " + getId() + " in move(). ThreadImplementation.Table acquired.");
        ArrayList<Point> possibleAdjacentCells =
                sendGetNonEnemyAdjacentCellsWithCapacity(currentLocation);
        writeToLog("Thread " + getId() + " in move(). Possible adjacent cells acquired.");
        if (possibleAdjacentCells.size() > 0) {
            writeToLog("Thread " + getId() + " in move(). New location Chosen.");
            Point newLocation = possibleAdjacentCells.get(random.nextInt(possibleAdjacentCells.size()));
            try {
                writeToLog("Thread " + getId() + " in move(). Trying to move");
                sendMoveSpecies(newLocation, currentLocation);
                currentLocation = newLocation;
            } catch (Exception e) {
                writeToLog("Thread " + getId() + " in move(). Exception ...");
                writeToLog(e.getMessage());
                System.exit(-2);
            }
        } else {
            writeToLog("Thread " + getId() + " in move(). No empty cell in neighbours");
        }
        writeToLog("releasing after move");
        sendReleaseTable();
    }

    private void reproduce() {
        writeToLog("Thread " + getId() + " in reproduce. Acquiring table ...");
        sendAcquireTable();
        sendReproduce(currentLocation);
        sendReleaseTable();
        writeToLog("Thread " + getId() + " in reproduce. Finished.");
    }

    private String getCurrentTurnStatus() {
        System.out.println("getCurrentTurn");
        String turnStatusString = scanner.nextLine();
        String turnStatus = null;
        switch (turnStatusString) {
            case "preStart":
                turnStatus = "preStart";
                break;
            case "turnStart":
                turnStatus = "turnStart";
                break;
            case "turnEnd":
                turnStatus = "turnEnd";
                break;
            case "intraTurn":
                turnStatus = "intraTurn";
                break;
            case "intraTurnEnd":
                turnStatus = "intraTurnEnd";
                break;
        }
        return turnStatus;
    }

    private void sendAcquireTable() {
        System.out.println("acquireTable");
        writeToLog("acquiring ...");
        String requestResult = scanner.nextLine();
        writeToLog("acquired");
        if (!requestResult.equals("acquired")) System.exit(-11);
    }

    private void sendReleaseTable() {
        writeToLog("releasing ...");
        System.out.println("releaseTable");
        String requestResult = scanner.nextLine();
        writeToLog("release result: " + requestResult);
        if (!requestResult.equals("released")) System.exit(-12);
    }

    private ArrayList<Point> sendGetNonEnemyAdjacentCellsWithCapacity(Point currentLocation) {
        System.out.println("getNonEnemyAdjacentCellsWithCapacity");
        System.out.println(currentLocation.x);
        System.out.println(currentLocation.y);
        ArrayList<Point> adjacentCells = new ArrayList<>();
        int x = Integer.parseInt(scanner.nextLine());
        int y;
        while (x != -1) {
            y = Integer.parseInt(scanner.nextLine());
            adjacentCells.add(new Point(x, y));
            x = Integer.parseInt(scanner.nextLine());
        }
        return adjacentCells;
    }

    private void sendMoveSpecies(Point newLocation, Point currentLocation) {
        writeToLog("move");
        System.out.println("move");
        System.out.println(currentLocation.x);
        System.out.println(currentLocation.y);
        System.out.println(newLocation.x);
        System.out.println(newLocation.y);
        writeToLog("sent move.");
        String requestResult = scanner.nextLine();
        writeToLog("move result: " + requestResult);
        if (!requestResult.equals("moved")) System.exit(-13);
    }

    private void sendReproduce(Point currentLocation) {
        System.out.println("reproduce");
        System.out.println(currentLocation.x);
        System.out.println(currentLocation.y);
        String requestResult = scanner.nextLine();
        if (!requestResult.equals("reproduced")) System.exit(-13);
    }

    private void writeToLog(String message) {
        try {
            message += "\n";
            FileOutputStream fileWriter =
                    new FileOutputStream(inLocation.substring(0, inLocation.length() - 6) + "log.txt", true);
            fileWriter.write(message.getBytes());
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        boolean didWrite = false;
        String currentTurn;
        while (!isOver()) {
            currentTurn = getCurrentTurnStatus();
            if (currentTurn.equals("intraTurnEnd") && !didIntraTurnEnd) {
                writeToLog("Thread " + getId() + " in intraTurnEnd");
                writeToOut(0);
                didWrite = false;
                didIntraTurnEnd = true;
                didReproduce = false;
                didDoTurnEnd = false;
                canReproduce = true;
                writeToOut(1);
                writeToLog("Thread " + getId() + " intraTurnEnd finished");
            } else if (currentTurn.equals("turnStart") && !didReproduce && canReproduce) {
                writeToLog("Thread " + getId() + " in reproduction");
                writeToOut(0);
                if (turnCounter == speciesType) {
                    turnCounter = 0;
                    reproduce();
                }
                didReproduce = true;
                writeToOut(2);
                writeToLog("Thread " + getId() + " reproduction finished");
            } else if (currentTurn.equals("turnEnd") && !didDoTurnEnd) {
                writeToLog("Thread " + getId() + " in turnEnd");
                writeToOut(0);
                turnCounter++;
                didDoTurnEnd = true;
                writeToOut(3);
                writeToLog("Thread " + getId() + " turnEnd finished");
            } else if (currentTurn.equals("intraTurn")) {
                writeToLog("Thread " + getId() + " in intraTurn");
                if (!didWrite) {
                    writeToOut(0);
                    didWrite = true;
                }
                didIntraTurnEnd = false;
                if (allowedToMove()) {
                    move();
                }
            } else {
                writeToLog("Thread " + getId() + " sleeping for 200ms");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        int speciesType = Integer.parseInt(s.nextLine());
        int row = Integer.parseInt(s.nextLine());
        int column = Integer.parseInt(s.nextLine());
        String id = s.nextLine();

        SingleSpeciesProcess singleSpeciesProcess = new SingleSpeciesProcess(speciesType, row, column, id);
        singleSpeciesProcess.start();
    }
}
