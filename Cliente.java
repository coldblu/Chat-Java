import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.util.ArrayList;

public class Cliente extends JFrame implements ActionListener,ListSelectionListener {
    
    public String whosWho = "All";
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    // Variáveis GUI
    private JPanel painel = new JPanel(), msg = new JPanel(), users = new JPanel(), geral = new JPanel();
    private JLabel lblHistorico = new JLabel("Historico de Mensagens"), lbMensagem = new JLabel("Mensagem");
    private JLabel lbOnline = new JLabel("Pessoas Online");
    private JButton btSend = new JButton("Enviar");
    private JTextField tfMensagem = new JTextField(20);
    private JTextArea taHistorico = new JTextArea(10, 20);
    private JScrollPane scroll = new JScrollPane(this.taHistorico);
    private DefaultListModel modelList = new DefaultListModel<String>();
    private JList list = new JList();
    private JScrollPane scrollList = new JScrollPane(this.list);

    // Construtor
    public Cliente(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;

            tela();

        } catch (Exception e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void tela() {

        // Tela
        // Painel Configs
        this.painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        this.painel.setBackground(Color.LIGHT_GRAY);

        this.taHistorico.setEditable(false);
        this.taHistorico.setBackground(new Color(240, 240, 240));

        // Botão + caixa de texto
        this.btSend.addActionListener(this);
        this.msg.add(this.tfMensagem);
        this.msg.add(this.btSend);

        this.painel.add(lblHistorico);
        this.painel.add(scroll);
        this.painel.add(lbMensagem);
        this.painel.add(msg);

        this.tfMensagem.setText("");
        // List
        
        this.list.setModel(modelList);
        //this.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.list.addListSelectionListener(this);

        this.users.setLayout(new BoxLayout(users, BoxLayout.Y_AXIS));
        this.users.setBackground(Color.LIGHT_GRAY);
        users.add(lbOnline);
        users.add(scrollList);
        // Lego
        this.geral.setLayout(new BorderLayout());
        geral.add(painel, BorderLayout.CENTER);
        geral.add(users, BorderLayout.EAST);

        // Window configs
        this.setTitle("Dolphins Chat");
        this.setContentPane(geral);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(false);
        // this.pack();
        this.setSize(600, 400);
        this.setVisible(true);

    }

    // Mandar mensagens
    public void sendMessage(String mesage) {
        try {
            bufferedWriter.write(whosWho + ";" + username + " diz -> " + mesage);
            taHistorico.append(username + " diz -> " + tfMensagem.getText() + "\r\n");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            System.out.println(whosWho);
        } catch (Exception e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;
                try {
                    bufferedWriter.write(username);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }

                while (socket.isConnected()) {
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        String[] splitThis = msgFromGroupChat.split(";", 2);
                        if (splitThis[0].equals("All")) {
                            taHistorico.append(splitThis[1] + "\n");
                        } else if (splitThis[0].equals("3")) {
                            refreshList(splitThis[0], splitThis[1]);
                            // taHistorico.append(splitThis[1] + "\n");
                        }else if(splitThis[0].equals("1")){
                            //String[] splitAgain = splitThis[1].split(";",2);
                            taHistorico.append("[Privado]" + splitThis[1] + "\n");
                        }
                        // taHistorico.append(msgFromGroupChat + "\n");
                        // System.out.println(msgFromGroupChat);
                    } catch (Exception e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    public void refreshList(String operation, String nameUser) {
        this.modelList.clear();
        this.modelList.addElement("All");
        String[] names = nameUser.split(";");
        for (String name : names) {
            this.modelList.addElement(name);
        }  
    }


    public void actionPerformed(ActionEvent evento) {
        // System.out.println("Botao pressionado");
        String mensagem = tfMensagem.getText();
        sendMessage(mensagem);
        tfMensagem.setText("");

    }

    public void valueChanged(ListSelectionEvent event){
        if (event.getValueIsAdjusting()) {
            this.whosWho =(String) list.getSelectedValue();
        }        
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Main
    public static void main(String args[]) throws IOException {
        Scanner ler = new Scanner(System.in);
        System.out.print("Informe o nome de usuario: ");
        String username = ler.nextLine();
        Socket socket = new Socket("localhost", 12345);
        Cliente cliente = new Cliente(socket, username);
        cliente.listenForMessage();
    }
}