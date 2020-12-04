package ThreadImplementation;

public class Main {
    public static void main(String[] args) {
        int n = 7;
        int m = 6;
        int r = 3;
        int s = 2;
        int k = 10;
        int t = 1;
        boolean useFirstRule = false;
        boolean useSecondRule = false;
        ThreadImplementation.Controller controller = new Controller(n, m , r, s, k, t, useFirstRule, useSecondRule);
        Thread thread = new Thread(controller);
        thread.start();
    }
}
