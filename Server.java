import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server{

    private ServerSocket serverSocket;

    //Construtor
    public Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    public void start(){
        try{
            while(!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("Um usuario se conectou!");
                ClientHandler clienteHandler = new ClientHandler(socket);

                Thread thread = new Thread(clienteHandler);
                thread.start();
            }
        }catch (Exception e){
            closeServerSocket();
        }

    }

    public void closeServerSocket(){
        try {
            if(serverSocket != null){
                serverSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws IOException{
        ServerSocket serverSocket = new ServerSocket(12345);
        Server server = new Server(serverSocket);
        server.start();
    }
}