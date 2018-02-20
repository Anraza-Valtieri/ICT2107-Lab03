import BroadcastListenerThread.BroadcastListenerThread;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.*;
import java.util.*;


import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class GroupChatApp extends JFrame {

    // SERVER
    MulticastSocket multicastSocket = null;
    InetAddress multicastGroup = null;
    // BUTTONS
    MulticastSocket multicastSocket2 = null;
    InetAddress multicastGroup2 = null;
    // CLIENT LISTENER
    MulticastSocket multicastSocket3 = null;
    InetAddress multicastGroup3 = null;
    // CLIENT CHAT
    MulticastSocket multicastSocket4 = null;
    InetAddress multicastGroup4 = null;

    private JPanel contentPane;
    private JTextField txtGroupIp;
    private JTextField textField;
    private JTextField textField_1;
    private JTextField textField_2;
    private JTextField textField_3;
    JTextArea textArea;

    private JButton btnSend;
    private JButton btnJoin;
    private JButton btnCreate;
    private JButton btnLeave;

    private String name = "";
    private String grp = "228.1.1.1";
    private int num = 0;


    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                try {
                    GroupChatApp frame = new GroupChatApp();
                    frame.setVisible(true);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void ClientListener(){
        //Create a new thread to keep client chat listening
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    multicastGroup3 = InetAddress.getByName("228.1.1.1");
                    multicastSocket3 = new MulticastSocket(6789);
                    multicastSocket3.joinGroup(multicastGroup3);
                    System.out.println("[CLIENT - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: CMD Listener Online - " +
                            multicastGroup3.getHostName() + " " + multicastGroup3.getHostAddress());

                    while(true){
                        System.out.println("[CLIENTCHAT - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: READY");
                        byte buf1[] = new byte[1000];
                        DatagramPacket dgpRecieved = new DatagramPacket(buf1, buf1.length);
                        multicastSocket3.receive(dgpRecieved);
                        byte[] receivedData = dgpRecieved.getData();
                        int length = dgpRecieved.getLength();
                        //Assumed we recieved string
                        String msg = new String(receivedData, 0, length);
                        System.out.println("[CLIENT - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: Received - " +
                                msg);
                        // We only care for CMSG messages
                        String[] sep = msg.split("\\:");
                        if (msg.substring(0, 4).contentEquals("SMSG")) {
                            if(msg.contains(textField_2.getText().toString())) {
                                // CMSGA is Create!
                                if (msg.substring(0, 5).contentEquals("SMSGA")) {
                                    String grpname = sep[2];
                                    System.out.println("[CLIENT - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: Client redirected to - " + grpname);
                                    grp = grpname;
                                    if (!sep[2].matches("NULL")) {
                                        multicastGroup3 = InetAddress.getByName(grp.toString());
                                        multicastSocket3 = new MulticastSocket(6789);
                                        multicastSocket3.joinGroup(multicastGroup3);
                                        System.out.println("[CLIENTCHAT - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: listening to - " +
                                                multicastGroup3.getHostName() + " " + multicastGroup3.getHostAddress());
                                    } else {
                                        textArea.append("Group " + textField_1.getText().toString() + " does not exist." + "\n");
                                        textArea.setCaretPosition(textArea.getDocument().getLength());
                                        btnSend.setEnabled(false);
                                        btnJoin.setEnabled(true);
                                        btnCreate.setEnabled(true);
                                        btnLeave.setEnabled(false);
                                    }
                                }
                            }
                        }
                        // CMGAS are messages!
                        if (msg.substring(0, 5).contentEquals("CMSGS")) {
                            // We are still relevant?
                            if(!grp.toString().contentEquals(multicastGroup3.getHostName().toString())) {
                                System.out.println("[CLIENTCHAT - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: THREAD STOPPED DUE MISMATCH 1");
                                break;
                            }
                            //String text = sep[2];
                            textArea.append(sep[1]+":"+sep[2] + "\n");
                            textArea.setCaretPosition(textArea.getDocument().getLength());
                            System.out.println("[CLIENTCHAT - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: recv - " +
                                    multicastGroup3.getHostName() + " " + multicastGroup3.getHostAddress());

                        }

                        // CMGAL are leaving!
                        if (msg.substring(0, 5).contentEquals("CMSGL")) {
                            String text = sep[1];
                            textArea.append(text + " has left the group.\n");
                            textArea.setCaretPosition(textArea.getDocument().getLength());
                            System.out.println("[CLIENTCHAT - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: recv - " +
                                    multicastGroup3.getHostName() + " " + multicastGroup3.getHostAddress());

                            if(textField_2.getText().toString().contentEquals(text)) {
                                System.out.println("[CLIENTCHAT - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: THREAD STOPPED DUE LEAVING");
                                break;
                            }

                        }
                        // We are still relevant?
                        if(!grp.toString().contentEquals(multicastGroup3.getHostName().toString())) {
                            System.out.println("[CLIENTCHAT - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: THREAD STOPPED DUE MISMATCH 2");
                            break;
                        }
                        msg = "";

                        }
                }catch(IOException e) {
                    e.printStackTrace();
                }
                System.out.println("[CLIENT - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: END");
                System.out.println("[CLIENTCHAT - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: END");
            }
        }).start();
    }

    public void ServerListener(){
        //Create a new thread to keep resolver
        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] startParts;
                String[] endParts;
                String start = "228.1.1.2";
                String end = "228.1.1.254";

                startParts = start.split("(?<=\\.)(?!.*\\.)");
                endParts = end.split("(?<=\\.)(?!.*\\.)");

                int first = Integer.parseInt(startParts[1]);
                int last = Integer.parseInt(endParts[1]);
                int i = first;
                Dictionary d = new Hashtable();

                try {
                    InetAddress multicastGroup = InetAddress.getByName("228.1.1.1");
                    multicastSocket = new MulticastSocket(6789);
                    //multicastSocket.setReuseAddress(true);
                    multicastSocket.joinGroup(multicastGroup);
                    System.out.println("[SERVER - "+ ManagementFactory.getRuntimeMXBean().getName()+ " - "+Thread.currentThread().getName()+"]: Online - " +
                            multicastGroup.getHostName()+ " " + multicastGroup.getHostAddress());

                    while(true) {
                        byte buf1[] = new byte[1000];
                        DatagramPacket dgpRecieved = new DatagramPacket(buf1, buf1.length);
                        multicastSocket.receive(dgpRecieved);
                        byte[] receivedData = dgpRecieved.getData();
                        int length = dgpRecieved.getLength();
                        //Assumed we recieved string
                        String msg = new String(receivedData, 0, length);
                        // We only care for CMSG messages
                        if (msg.substring(0, 4).contentEquals("CMSG")) {
                            System.out.println("[SERVER - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: Received - " +
                                    msg);
                            // CMSGA:testgrp:Tester
                            String[] sep = msg.split("\\:");
                                // CMSGA is Create!
                                if (msg.substring(0, 5).contentEquals("CMSGA")) {
                                    String grpname = sep[1];
                                    if (d.get(grpname) == null) {
                                        String newIP = startParts[0] + i;
                                        System.out.println("[SERVER - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: Creating new entry - "
                                                + grpname + ":" + newIP);
                                        d.put(grpname, newIP);
                                        i++;
                                        System.out.println("[SERVER - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: Dictionary size:" + d.size());

                                        String smsg = "SMSGA:" + sep[2] + ":" + newIP;
                                        byte[] buf = smsg.getBytes();
                                        DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, multicastGroup, 6789);
                                        System.out.println("[SERVER - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: SEND - " +
                                                smsg);
                                        multicastSocket.send(dgpSend);
                                    } else {
                                        String newIP = d.get(grpname).toString();
                                        System.out.println("[SERVER - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: entry exist - "
                                                + grpname + ":" + newIP);
                                        String smsg = "SMSGA:" + sep[2] + ":" + newIP;
                                        byte[] buf = smsg.getBytes();
                                        DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, multicastGroup, 6789);
                                        System.out.println("[SERVER - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: SEND - " +
                                                smsg);
                                        multicastSocket.send(dgpSend);
                                    }

                                }

                            if(!sep[2].contentEquals(name.toString())) {
                                // CMSGJ is Join!
                                if (msg.substring(0, 5).contentEquals("CMSGJ")) {
                                    //String grpname = msg.substring(5);
                                    String grpname = sep[1];
                                    if (d.get(grpname) == null) {
                                        String smsg = "SMSGA:" + sep[2] + ":" + "NULL";
                                        byte[] buf = smsg.getBytes();
                                        DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, multicastGroup, 6789);
                                        System.out.println("[SERVER - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: SEND - " +
                                                smsg);
                                        multicastSocket.send(dgpSend);
                                    } else {
                                        String newIP = d.get(grpname).toString();
                                        System.out.println("[SERVER - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: entry exist - "
                                                + grpname + ":" + newIP);
                                        String smsg = "SMSGA:" + sep[2] + ":" + newIP;
                                        byte[] buf = smsg.getBytes();
                                        DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, multicastGroup, 6789);
                                        System.out.println("[SERVER - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: SEND - " +
                                                smsg);
                                        multicastSocket.send(dgpSend);
                                    }

                                }
                            }
                            msg = "";
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("[SERVER - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: END");
            }
        }).start();


    }



    /**
     * Create the frame.
     */
    public GroupChatApp() {


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 422);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblProcessid = new JLabel("User Name");
        lblProcessid.setBounds(10, 24, 63, 14);
        contentPane.add(lblProcessid);

        JLabel lblGroupIp = new JLabel("Create Group");
        lblGroupIp.setBounds(10, 52, 78, 14);
        contentPane.add(lblGroupIp);

        txtGroupIp = new JTextField();
        txtGroupIp.setText("testgrp");
        txtGroupIp.setBounds(83, 49, 139, 20);
        contentPane.add(txtGroupIp);
        txtGroupIp.setColumns(10);

        btnJoin = new JButton("Join");
        btnJoin.setBounds(232, 82, 89, 23);
        contentPane.add(btnJoin);

        btnLeave = new JButton("Leave");
        btnLeave.setBounds(322, 82, 89, 23);
        contentPane.add(btnLeave);
        btnLeave.setEnabled(false);

        btnCreate = new JButton("Create");
        btnCreate.setBounds(232, 48, 89, 23);
        contentPane.add(btnCreate);

        textArea = new JTextArea();
        textArea.setBounds(10, 163, 414, 164);
        //contentPane.add(textArea);
        //textArea.setAutoscrolls(true);
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBounds(10, 163, 414, 164);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        contentPane.add(scroll);



        JLabel lblMessage = new JLabel("Message");
        lblMessage.setBounds(10, 353, 46, 14);
        contentPane.add(lblMessage);

        textField = new JTextField();
        textField.setBounds(66, 350, 263, 20);
        contentPane.add(textField);
        textField.setText("textField");
        textField.setColumns(10);

        btnSend = new JButton("Send");
        btnSend.setEnabled(false);
        btnSend.setBounds(335, 349, 89, 23);
        contentPane.add(btnSend);

        JLabel lblJoinGroup = new JLabel("Join Group");
        lblJoinGroup.setBounds(10, 86, 78, 14);
        contentPane.add(lblJoinGroup);

        textField_1 = new JTextField();
        textField_1.setBounds(83, 83, 139, 20);
        contentPane.add(textField_1);
        //textField_1.setText("textField_1");
        textField_1.setColumns(10);

        textField_2 = new JTextField();
        textField_2.setBounds(82, 21, 140, 20);
        contentPane.add(textField_2);
        textField_2.setText("Tester");
        textField_2.setColumns(10);

        JButton btnUpdate = new JButton("Update");
        btnUpdate.setBounds(232, 20, 89, 23);
        contentPane.add(btnUpdate);

		btnJoin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                try {
                    // Tune client listener to listen to broadcast for instructions
                    System.out.println("JOIN KEYPRESS");
                    multicastGroup2 = InetAddress.getByName("228.1.1.1");
                    multicastSocket2 = new MulticastSocket(6789);
                    multicastSocket2.joinGroup(multicastGroup2);

                    String msg = "CMSGJ:"+textField_1.getText()+":"+textField_2.getText();
                    byte[] buf = msg.getBytes();
                    DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, multicastGroup2, 6789);
                    System.out.println("[CLIENT - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: SEND - " +
                            msg);
                    multicastSocket2.send(dgpSend);
                    textArea.append("Joining "+ textField_1.getText() + "\n");
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                    ClientListener();
                    btnSend.setEnabled(true);
                    btnJoin.setEnabled(false);
                    btnCreate.setEnabled(false);
                    btnLeave.setEnabled(true);
                }catch (IOException ex){
                    ex.printStackTrace();
                }finally {
                    //multicastSocket2.close();
                }
            }
		});

        btnLeave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // Tune client listener to listen to broadcast for instructions
                    System.out.println("LEAVE KEYPRESS");

                    String msg = "CMSGL:"+textField_2.getText();
                    byte[] buf = msg.getBytes();
                    DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, multicastGroup3, 6789);
                    System.out.println("[CLIENT - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: SEND - " +
                            msg);
                    multicastSocket3.send(dgpSend);
                    textArea.append("Leaving "+ textField_1.getText() + "\n");
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                    //ClientListener();
                    btnSend.setEnabled(false);
                    btnJoin.setEnabled(true);
                    btnCreate.setEnabled(true);
                    btnLeave.setEnabled(false);
                    //btnCreate.setEnabled(false);
                }catch (IOException ex){
                    ex.printStackTrace();
                }finally {
                    //multicastSocket2.close();
                }
            }
        });

		btnCreate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    System.out.println("CREATE KEYPRESS");
                    multicastGroup2 = InetAddress.getByName("228.1.1.1");
                    multicastSocket2 = new MulticastSocket(6789);
                    multicastSocket2.joinGroup(multicastGroup2);


                    //ResetClientListener();
                    String msg = "CMSGA:"+txtGroupIp.getText()+":"+textField_2.getText();
                    byte[] buf = msg.getBytes();
                    DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, multicastGroup2, 6789);
                    System.out.println("[CLIENT - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: SEND - " +
                            msg);
                    multicastSocket2.send(dgpSend);
                    textArea.append("Creating "+ txtGroupIp.getText() + "\n");
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                    //grp = txtGroupIp.getText().toString();
                    ClientListener();
                    btnSend.setEnabled(true);
                    btnJoin.setEnabled(false);
                    btnCreate.setEnabled(false);
                    btnLeave.setEnabled(true);
                    //btnCreate.setEnabled(false);
                }catch (IOException ex){
                    ex.printStackTrace();
                }finally {
                    //multicastSocket2.close();
                }
            }
        });

		btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                name = textField_2.getText();
                System.out.println("[CLIENT - " + ManagementFactory.getRuntimeMXBean().getName() + " - " + Thread.currentThread().getName() + "]: Name Updated - " +
                        name);
            }
        });

		btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    System.out.println(grp);
                    multicastGroup2 = InetAddress.getByName(grp);
                    multicastSocket2 = new MulticastSocket(6789);
                    multicastSocket2.joinGroup(multicastGroup2);
                    String msg = textField.getText();
                    msg = "CMSGS:"+name + ": " + msg;
                    byte[] buf = msg.getBytes();
                    DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, multicastGroup2, 6789);
                    multicastSocket2.send(dgpSend);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        //ClientListener();
        ServerListener();

    }
}