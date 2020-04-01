import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

public class Server {

        static final int MAXPACKETSIZE = 512;
    public static void main(String[] args) throws IOException {

        Scanner stdin = new Scanner(System.in);
        InetAddress ip;
        int port;
        boolean dropPackets;
        DatagramSocket socket;

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

        //Set drop preference
        /*
        System.out.println("Drop 1% of packets (Y/N):");
        String drop = stdin.nextLine();

        if(drop.equalsIgnoreCase("y")){
            dropPackets = true;
        }
        else if(drop.equalsIgnoreCase("n")){
            dropPackets = false;
        }
        */

        socket = new DatagramSocket(port);

        while (!stdin.nextLine().equalsIgnoreCase("quit")){

            byte[] buffer = new byte[MAXPACKETSIZE];
            DatagramPacket packet = new DatagramPacket(buffer, MAXPACKETSIZE);
            socket.receive(packet);
        }


    }

    private static void handlePacket(DatagramPacket packet, boolean drop, InetAddress ip){

    }
}
