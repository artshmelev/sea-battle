package model;
import java.io.*;
import java.net.*;

import model.IpAddress;
import model.Model;
import model.GameState;
import static model.Constants.*;

public class TcpThread extends Thread {

    private Model model;
    private int port;

    public TcpThread(Model model, int port) {
        super();
        this.model = model;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);

            while (true) {
                Socket socket = serverSocket.accept();
                BufferedReader in =
                    new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String data = in.readLine();

                Message msg = new Message(data);
                if (msg.getPrefix().equals(REQUEST_PREFIX)) {
                    GameState state = model.getGameState();
                    if (state == GameState.WAIT) {
                        model.setOppAddress(new IpAddress(
                            socket.getInetAddress().getHostAddress(),
                            msg.getData()[0])
                        );
                    } else if (state == GameState.WAIT_READY ||
                               state == GameState.PREPARE) {
                        model.setOppReady(true);
                    } else
                        model.setRequest(msg);
                }
                else if (msg.getPrefix().equals(RESPONSE_PREFIX))
                    model.setResponse(msg);
            }
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
