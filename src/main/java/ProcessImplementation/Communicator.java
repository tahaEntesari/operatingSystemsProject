package ProcessImplementation;

import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Scanner;

public class Communicator extends Thread {
    private PrintWriter printWriter;
    private Scanner scanner;
    private InputStream errorStream;
    private Table table;
    private boolean alive;
    private JSONObject writerScanner;

    public Communicator(JSONObject writerScanner, Table table) {
        this.printWriter = (PrintWriter) writerScanner.get("writer");
        this.scanner = (Scanner) writerScanner.get("scanner");
        this.errorStream = (InputStream) writerScanner.get("errorStream");
        this.writerScanner = writerScanner;
        this.table = table;
        alive = true;
    }

    public void kill() {
        alive = false;
    }

    @Override
    public void run() {
        String scanResult;

        while (alive) {
//            System.out.println("Thread " + getId() + "alive");
            try {
                for (int i = 0; i < errorStream.available(); i++) {
                    System.out.println("" + errorStream.read());

                }
            } catch (IOException e){
                e.printStackTrace();
            }

            if (!scanner.hasNext()) {
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            JSONObject requestData = new JSONObject();
            scanResult = scanner.nextLine();
//            System.out.println(scanResult);
            requestData.put("requestCommand", scanResult);
            switch (scanResult) {
                case "getNonEnemyAdjacentCellsWithCapacity":
                    int row = Integer.parseInt(scanner.nextLine());
                    int column = Integer.parseInt(scanner.nextLine());
                    Point currentLocation = new Point(row, column);
                    requestData.put("currentLocation", currentLocation);
                    break;
                case "move":
                    int currentRow = Integer.parseInt(scanner.nextLine());
                    int currentColumn = Integer.parseInt(scanner.nextLine());
                    Point currentLocation1 = new Point(currentRow, currentColumn);
                    requestData.put("currentLocation", currentLocation1);
                    int newRow = Integer.parseInt(scanner.nextLine());
                    int newColumn = Integer.parseInt(scanner.nextLine());
                    Point newLocation = new Point(newRow, newColumn);
                    requestData.put("newLocation", newLocation);
                    break;
                case "reproduce":
                    int currentRow1 = Integer.parseInt(scanner.nextLine());
                    int currentColumn1 = Integer.parseInt(scanner.nextLine());
                    Point currentLocation2 = new Point(currentRow1, currentColumn1);
                    requestData.put("currentLocation", currentLocation2);
                    break;
            }
            table.handleRequest(requestData, writerScanner);

        }
    }
}
