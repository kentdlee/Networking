/*
 * This class implements a sender of data to an address and port. It's
 * purpose is to inject checksum errors and sometimes fail to send
 * to inject errors into the protocol for testing reliable data transfer.
 */

import java.net.*;
import java.io.*;
import java.util.*;

public class Injector {
    private Random rand;
    private double LOST_PACKET_RATE;
    private double CHECKSUM_ERROR_RATE;

    public Injector(double checksumErrorRate, double lostPacketErrorRate) {
        rand = new Random();
        LOST_PACKET_RATE = lostPacketErrorRate;
        CHECKSUM_ERROR_RATE = checksumErrorRate;
    }

    public void send(byte[] data, InetAddress addr, int port) throws java.io.IOException {
        double checksum = rand.nextDouble();
        double lost = rand.nextDouble();

        if (checksum < CHECKSUM_ERROR_RATE) {
            int index = rand.nextInt(data.length);
            data[index] = (byte) ((~data[index]) & 0xff);
        }

        if (lost > LOST_PACKET_RATE) {
            DatagramSocket senderSock = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(data, data.length, addr, port);
            senderSock.send(packet);
        }

    }
}
