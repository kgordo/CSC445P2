import java.net.InetAddress;
import java.util.Scanner;

public class Server {
    public static void main(String[] args){

        Scanner stdin = new Scanner(System.in);
        InetAddress ip;
        int port;

        System.out.println("Enter port:");
        port = Integer.parseInt(stdin.nextLine());


        //Set address preference
        /*
        System.out.println("IPv6? (Y/N):");
        String format = stdin.nextLine();

        if(format.equalsIgnoreCase("y")){
            System.setProperty("java.net.preferIPv4Stack", "false");
        }
        else if(format.equalsIgnoreCase("n")){
            System.setProperty("java.net.preferIPv4Stack", "true");
        }
        */


    }
}
