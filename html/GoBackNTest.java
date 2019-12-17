/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author leekent
 */
import java.io.*;

class TCPClient implements Runnable {

    private static final String CRLF = "\r\n";
    private String ca;

    public void run() {
        try {
            MySocket socket = new MySocket("127.0.0.1", 1142);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            out.writeBytes("Hi there!" + CRLF);
            ca = "a" + socket.getPort();
            out.flush();
            System.out.println("ltext@ " + ca + " \"Wrote Hi there!\";");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String s = in.readLine();
            System.out.println("ltext@ " + ca + " \"Read data from other side.\";");

            if (s.equals("How are you?")) {
                System.out.println("ltext@ " + ca + " \"Initial success on client side!!!!\";");
            } else {
                System.out.println("Error on client side. Message was:");
                System.out.println(s);
                return;
            }

            out.writeBytes("I am fine!!!!" + CRLF);
            out.flush();
            System.out.println("ltext@ " + ca + " \"Wrote I am fine!!!!\";");

            FileReader file = new FileReader(new File("independence.txt"));

            int b = file.read();

            while (b != -1) {
                out.writeByte(b);
                b = file.read();
            }

            file.close();
            out.flush();

            System.out.println("ltext@ " + ca + " \"The independence.txt file has been read and sent.\";");

            socket.close();
        } catch (IOException ex) {
            System.out.println("Unexpected Exception on Client Side.");
            System.out.println(ex.getMessage());
        }


    }
}

class TCPServer implements Runnable {

    private static final String CRLF = "\r\n";
    private String sa;

    public void run() {
        FileWriter file = null;

        try {
            MyServerSocket serverSocket = new MyServerSocket(1142);

            MySocket socket = serverSocket.accept();
            sa = "a" + socket.getPort();
            System.out.println("component " + sa + " \"Server Application\";");
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String s = in.readLine();
            System.out.println("ctext@ " + sa + " \"read what should be 'Hi there'\";");

            if (s.equals("Hi there!")) {
                System.out.println("ctext@ " + sa + " \"wrote 'How are you?'\";");
                out.writeBytes("How are you?" + CRLF);
                out.flush();
            } else {
                System.out.println("Error on server side with first message. Message was:");
                System.out.println(s);
                return;
            }

            s = in.readLine();

            if (s.equals("I am fine!!!!")) {
                System.out.println("rtext@ " + sa + " \"Initial success on server side!!!!\";");
                //socket.close();
            } else {
                System.out.println("Error on server side with second message. Message was:");
                System.out.println(s);
            }

            file = new FileWriter(new File("copy.txt"));

            String line = in.readLine();
            while (line != null) {
                file.write(line+"\n");
                line = in.readLine();
            }

            file.close();
            System.out.println("rtext@ "+ sa + " \"Finished copying independence.txt to copy.txt\";");


        } catch (IOException ex) {
            System.out.println("Unexpected Exception on Server Side.");
            System.out.println(ex.getMessage());
            try {
               file.close(); 
            } catch (Exception except) {}

        }

    }
}

public class GoBackNTest {

    public static void main(String[] args) throws java.io.IOException {
        //Segment seg = new Segment(100, 200, 9012, 10567, Segment.SYN, 0, 0, null, null, 0);
        //byte[] b = seg.toBytes();

        //b[15] = 127;

        //seg = Segment.fromBytes(b, b.length);

        Thread server = new Thread(new TCPServer());
        server.start();

        Thread client = new Thread(new TCPClient());
        client.start();

    }
}
