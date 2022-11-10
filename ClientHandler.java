import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    // Construtor
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            listUsers();
            broadcastMessage("All;SERVER: " + clientUsername + " entrou no chat!");
        } catch (Exception e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                String[] check = messageFromClient.split(";",2);
                if(check[0].equals("All") || check[0].equals(clientUsername)){
                    broadcastMessage(messageFromClient);
                }
                else{
                    privateMessage(messageFromClient);
                }
                
                listUsers();
            } catch (Exception e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                String[] splitIt = messageToSend.split(";", 2);
                if (splitIt[0].equals("All") || splitIt[0].equals(clientUsername)) {
                    if (!clientHandler.clientUsername.equals(clientUsername)) {
                        clientHandler.bufferedWriter.write("All;" + splitIt[1]);
                        clientHandler.bufferedWriter.newLine();
                        clientHandler.bufferedWriter.flush();
                    }
                } else if (splitIt[0].equals("3")) {
                    clientHandler.bufferedWriter.write("3;" + splitIt[1]);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }

            } catch (Exception e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void privateMessage(String message){
        try {
            String[] splitIt = message.split(";",2);
            for (ClientHandler clientHandler : clientHandlers) {
                if (clientHandler.clientUsername.equals(splitIt[0])) {
                    clientHandler.bufferedWriter.write("1;"+ splitIt[1]);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        
    }

    public void listUsers() {        
            try {
                StringBuilder users = new StringBuilder();
                for (ClientHandler clientHandler : clientHandlers) {
                    users.append(clientHandler.clientUsername);
                    users.append(";");
                }                
                broadcastMessage("3;" + users);

            } catch (Exception e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        listUsers();
        broadcastMessage("All;SERVER: " + clientUsername + " saiu do chat!\n");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
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

}