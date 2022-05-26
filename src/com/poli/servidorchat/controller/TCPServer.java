/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.poli.servidorchat.controller;

import com.poli.servidorchat.model.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author dannymvp
 */
public class TCPServer {
    
    public void sendMessage(OutputStream outputStream, String username, String content) throws IOException{
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.flush();
        Message message = new Message();
        message.setUsername(username);
        message.setContent(content);
        dataOutputStream.writeUTF(username+"|"+content);
    }
    
    public String readMessage(InputStream inputStream) throws IOException{
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        return dataInputStream.readUTF();
    }
}
