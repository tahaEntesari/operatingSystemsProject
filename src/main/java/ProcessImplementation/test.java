package ProcessImplementation;

import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.util.Scanner;

public class test extends Thread {
    int wait;

    public test(int waitTime) {
        wait = waitTime;
    }

    @Override
    public void run() {
        try {
            System.out.println("Enter input ...");
            Scanner scanner = new Scanner(System.in);
            String result = scanner.next();
            System.out.println(result + " Thread " + getId());
            try {
                sleep(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(scanner.next() + " Thread " + getId());
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public static void main(String[] args) {
//        String runCommand = "java  -cp C:\\Users\\tahae\\Dropbox\\8th term\\OS\\project\\src\\main\\java;" +
//                " ProcessImplementation.SingleSpeciesProcess";
//        String runCommand = "javac -cp src src\\main\\java\\ProcessImplementation\\SingleSpeciesProcess.java";
//        try {
//            String id = "ahoyYou";
//            String inLocation = "./processFiles/" + id + "IN.txt";
//            String outLocation = "./processFiles/" + id + "OUT.txt";
//            (new File(inLocation)).createNewFile();
//            (new File(outLocation)).createNewFile();
//            Process process;
//            Runtime.getRuntime().exec(runCommand);
//            Thread.sleep(5000);
//            process = Runtime.getRuntime()
//                    .exec("java -cp src/main/java ProcessImplementation.SingleSpeciesProcess");
//            System.out.println(2);
//            PrintWriter pw = new PrintWriter(process.getOutputStream());
//            pw.println(1);
//            pw.println(1);
//            pw.println(1);
//            pw.println(id);
//            pw.flush();
//            System.out.println(4);
//            Thread thread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    InputStream errorStream = process.getErrorStream();
//                    try {
//                        while (true) {
//                            for (int i = 0; i < errorStream.available(); i++) {
//                                System.out.println("" + errorStream.read());
//                            }
//                        }
//                    } catch (IOException e){
//                        e.printStackTrace();
//                    }
//                }
//            });
//            thread.start();
//            System.out.println(5);
//            Scanner scanner = new Scanner(process.getInputStream());
//            System.out.println(6);
//            if (scanner.hasNext()) {
//                System.out.println(scanner.nextLine());
//            }
//            System.out.println(7);
//            System.out.println(scanner.hasNext());
//            if (scanner.hasNext()) {
//                System.out.println(scanner.nextLine());
//            }
//            System.out.println(1);
//            while (true){
//                try {
////                    System.out.println(process.isAlive() + "" + process.exitValue());
//                    if (scanner.hasNext()) {
//                        System.out.println(scanner.nextLine());
//                    }
//                } catch (Exception e){
////                    System.out.println(process.isAlive());
//                }
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        String location =
                "C:\\Users\\tahae\\Dropbox\\8th term\\OS\\project\\processFiles\\16605ctdmhbAHLs1Deah7nUOtbmpiiOUT.txt";
        try {
            FileReader fileReader = new FileReader(location);
            System.out.println(fileReader.read());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
