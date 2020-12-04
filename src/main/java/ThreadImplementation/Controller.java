package ThreadImplementation;

import java.util.Scanner;

enum TurnStatus {
    preStart, turnStart, turnEnd, intraTurn, intraTurnEnd
}

public class Controller implements Runnable {
    int n, m, r, s, k, t;
    private Table table;
    private TurnStatus turnStatus;

    public Controller(int n, int m, int r, int s, int k, int t, boolean killSmallestFirst, boolean dontKillDiagonal) {
        this.n = n;
        this.m = m;
        this.r = r;
        this.s = s;
        this.k = k;
        this.t = t;
        table = new Table(n, m, k, r, s, this, killSmallestFirst, dontKillDiagonal);
        turnStatus = TurnStatus.preStart;
    }

    public TurnStatus getCurrentTurnStatus() {
        return turnStatus;
    }

    private void handleTurnEnd() {
        table.acquireTable();
        table.handleOverPopulationDeath();
        table.handleAttackDeath();
        table.releaseTable();
    }

    @Override
    public void run() {
        int round = 1;
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("To continue, enter yes");
            String input = scanner.next();
            if (!input.equals("yes")) continue;
            for (int i = 0; i < t; i++) {
                try {
                    System.out.println("Initiating round " + round);
                    System.out.println("Initial table status:");
                    table.printLocations();
                    turnStatus = TurnStatus.intraTurn;
                    System.out.println("Sleeping for 1 second(s) ...");
                    Thread.sleep(1000);
                    System.out.println("Sleeping done. Initiating intraTurnEnd");
                    turnStatus = TurnStatus.intraTurnEnd;
                    while (table.getNumberOfThreadsInLineForTable() != 0){
                        System.out.println("Number of threads in queue: " + table.getNumberOfThreadsInLineForTable());
                        System.out.println("Waiting for table queue to be empty. Let's sleep for 500 ms");
                        Thread.sleep(500);
                    }
                    System.out.println("Ensuring that intraTurnEnd actions have been done ...");
                    while(!table.allHaveDoneIntraTurnEnd()){
                        System.out.println("Waiting for all threads to complete their intraTurnEnd actions." +
                                " Let's sleep for 500 ms");
                        Thread.sleep(500);
                    }
                    turnStatus = TurnStatus.turnEnd;
                    while (!table.allHaveDoneTurnEnd()) {
                        System.out.println("Waiting for all threads to complete their turnEnd actions." +
                                " Let's sleep for 500 ms");
                        Thread.sleep(500);
                    }
                    System.out.println("Status prior to deaths");
                    table.printLocations();
                    System.out.println("Executing turn end actions by the controller ...");
                    handleTurnEnd();
                    System.out.println("TurnEnd done. Table status prior to reproduction");
                    table.printLocations();
                    System.out.println("Initiating reproduction by each thread ...");
                    turnStatus = TurnStatus.turnStart;
                    while (!table.allHaveReproduced()){
                        System.out.println("Waiting for all threads to complete their reproduction." +
                                " Let's sleep for 500 ms");
                        Thread.sleep(500);
                    }
                    Thread.sleep(1000);
                    System.out.println("Reproduction done. Turn finished. ");
                    table.printLocations();
                    round++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
