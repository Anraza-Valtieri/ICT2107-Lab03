package BroadcastListenerThread;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.*;
import java.util.*;

public class BroadcastListenerThread {

    private long FIVE_SECONDS = 5000;

    public void run(){
        MulticastSocket multicastSocket = null;
        try {
            InetAddress multicastGroup = InetAddress.getByName("228.1.1.1");
            multicastSocket = new MulticastSocket(4446);
            multicastSocket.setReuseAddress(true);
            multicastSocket.joinGroup(multicastGroup);
            System.out.print("[RESOLVER - "+ ManagementFactory.getRuntimeMXBean().getName()+"]: Listener Online - " +
                    multicastGroup.getHostName()+ " " + multicastGroup.getHostAddress());

            try {
                DatagramSocket socket = new DatagramSocket(6789);
                byte[] buffer = new byte[socket.getReceiveBufferSize()];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                while (true) {
                    // Blocks until it gets a packet
                    socket.receive(packet);
                    System.out.print("[RESOLVER - "+ ManagementFactory.getRuntimeMXBean().getName()+"]: Listener Received - " +
                            packet.getData().toString());
                }
                // socket.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
