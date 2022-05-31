/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.poli.servidorchat.model;

import com.poli.servidorchat.model.mapper.MessageMapper;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 * Esta clase controla todas y cada una de las conexiones cliente al servidor de chat. Implementa la interface Runnable debido a que cada conexión
 * de cliente será gestionada por un hilo.
 *
 */
public class ClientHandler implements Runnable {

    private String username;
    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private JTextArea jtextArea;
    public static final String CLOSING_ORDER = "Chao";
    /**
     * Inicializa los objetos más relevantes para cada conexión de cliente.
     * @param socket el socket de conexión asignado al cliente.
     * @param area el JTextArea de la interfaz de usuario para la visualización de logs.
     */
    public ClientHandler(Socket socket, JTextArea area) {
        this.jtextArea = area;
        try {
            this.socket = socket;
            this.dataInputStream = new DataInputStream(this.socket.getInputStream());
            this.dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
            this.setUsername(this.dataInputStream.readUTF().split("\\|")[1]);
            jtextArea.append(String.format("Usuario %s conectado", this.username) + "\n");
            clientHandlers.add(this);
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public void setDataOutputStream(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public void setDataInputStream(DataInputStream dataInputStream) {
        this.dataInputStream = dataInputStream;
    }
    /**
     * Implementación del método run() dedicada a "escuchar" los mensajes que reciba el servidor de chat y a reenviarlos haciaa su destinatario.
     */
    @Override
    public void run() {
        MessageMapper messageMapper = new MessageMapper();
        try {
            String incomingMessage = "";
            Message message = null;
            do {
                incomingMessage = dataInputStream.readUTF();
                System.out.println(incomingMessage);
                message = messageMapper.mapStringToMessage(incomingMessage);
                switch (message.getMessageType()) {                    
                    case 2:
                        jtextArea.append(String.format("De %s para %s: %s", message.getFrom(), message.getTo(), message.getContent()) + "\n");
                        if (!message.getTo().equals("Servidor")) {
                            String messageString = messageMapper.mapMessageToString(message);
                            sendMessage(messageString, message.getTo());
                        }
                        break;
                    case 3:
                        jtextArea.append(String.format("El usuario %s ha abandonado", message.getFrom()) + "\n");
                        closeEverything(socket, dataInputStream, dataOutputStream);
                        break;
                }
            } while (!message.getContent().equalsIgnoreCase(CLOSING_ORDER) && this.socket.isConnected());
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            closeEverything(socket, dataInputStream, dataOutputStream);
        }
    }
    /**
     * Este método busca en el listado de conexiones de clientes al destinatario de un mensaje para su posterior envío a través del socket indicado.
     * @param messageString representación en cadena de texto del mensaje a enviar.
     * @param to el cliente destinatario.
     * @throws IOException 
     */
    public void sendMessage(String messageString, String to) throws IOException {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.getUsername().equals(to)) {
                System.out.println(clientHandler.getUsername());
                DataOutputStream ds = new DataOutputStream(clientHandler.getSocket().getOutputStream());
                ds.writeUTF(messageString);
                ds.flush();
            }
        }
    }
    /**
     * Este método elimina esta conexión de cliente del listado principal.
     */
    public void removeClientHandler() {
        clientHandlers.remove(this);        
    }

    /**
     * Cierra de forma adecuada los flujos de entrada/salida de datos y el socket de conexión del cliente.
     * @param socket el socket TCP de conexión del cliente.
     * @param dataInputStream el flujo de entrada de datos del cliente.
     * @param dataOutputStream el flujo de salida de datos del cliente.
     */
    public void closeEverything(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        removeClientHandler();
        try {
            if (dataInputStream != null) {
                dataInputStream.close();
            }
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
