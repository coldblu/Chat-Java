import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextArea;

public class Cliente extends JFrame implements ActionListener{

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    //Variáveis GUI
    private JPanel painel = new JPanel(), msg = new JPanel();
    private JLabel lblHistorico = new JLabel("Historico de Mensagens"), lbMensagem = new JLabel("Mensagem");
    private JButton btSend = new JButton("Enviar");
    private JTextField tfMensagem  = new JTextField(20);
    private JTextArea taHistorico = new JTextArea(10, 20);
    private JScrollPane scroll = new JScrollPane(this.taHistorico);

    //Construtor
    public Cliente(Socket socket, String username){
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

    public void tela(){
        
        //Tela
            this.painel.setLayout( new BoxLayout(painel, BoxLayout.Y_AXIS));
	    	this.painel.setBackground(Color.LIGHT_GRAY);

            this.taHistorico.setEditable(false);
            this.taHistorico.setBackground(new Color(240, 240, 240));
            
            this.btSend.addActionListener(this);
            
            this.msg.add(this.tfMensagem);
            this.msg.add(this.btSend);

            
            this.painel.add(lblHistorico);
            this.painel.add(scroll);
            this.painel.add(lbMensagem);
            this.painel.add(msg);

            this.tfMensagem.setText("");
            
            this.setTitle("Dolphins Chat");
            this.setContentPane(painel);
            this.setLocationRelativeTo(null);
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
            this.setResizable(false);
            //this.pack();
            this.setSize(600, 400);
            this.setVisible(true);
            
			
    }
    //Mandar mensagens
    public void sendMessage(String mesage){
        try {
                bufferedWriter.write(username + " diz -> " + mesage);
                taHistorico.append(username + " diz -> " + tfMensagem.getText() + "\r\n");                
                bufferedWriter.newLine();
                bufferedWriter.flush();
        } catch (Exception e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMessage(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                String msgFromGroupChat;
                try {
                    bufferedWriter.write(username);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                } catch (Exception e) {
                    // TODO: handle exception
                }
                
                while(socket.isConnected()){
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        taHistorico.append(msgFromGroupChat+"\n");
                        //System.out.println(msgFromGroupChat);
                    } catch (Exception e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    
    public void actionPerformed(ActionEvent evento){
                //System.out.println("Botao pressionado");
                String mensagem = tfMensagem.getText();
                sendMessage(mensagem);
                tfMensagem.setText("");
  
    }
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        try {
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            if(socket != null){
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Main
    public static void main(String args[]) throws IOException{
        Scanner ler = new Scanner(System.in);
        System.out.print("Informe o nome de usuario: ");
        String username = ler.nextLine();
        Socket socket = new Socket("localhost",12345);
        Cliente cliente = new Cliente(socket,username);
        cliente.listenForMessage();
    }
}