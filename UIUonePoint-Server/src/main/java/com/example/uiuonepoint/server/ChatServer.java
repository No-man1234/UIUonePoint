package com.example.uiuonepoint.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 5000;


    private static final Map<String, Set<PrintWriter>> chatWriters = new HashMap<>();

    private static final Map<String, List<String>> chatHistories = new HashMap<>();

    private static final Map<Socket, String> socketChatIdMap = new HashMap<>();

    private static final Map<Socket, PrintWriter> socketWriterMap = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("UIUonePOINT Chat Server is running...");
        try (ServerSocket listener = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(listener.accept()).start();
            }
        } catch (IOException e) {
            System.out.println("Error in the server: " + e.getMessage());
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String name;
        private String chatId;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);


                String nameAndChatId = in.readLine();
                if (nameAndChatId == null || !nameAndChatId.contains("::")) {
                    out.println("NAME_TAKEN");
                    return;
                }
                String[] parts = nameAndChatId.split("::", 2);
                name = parts[0];
                chatId = parts[1];


                synchronized (chatWriters) {
                    chatWriters.putIfAbsent(chatId, new HashSet<>());
                    chatWriters.get(chatId).add(out);
                }
                synchronized (chatHistories) {
                    chatHistories.putIfAbsent(chatId, new ArrayList<>());
                }
                socketChatIdMap.put(socket, chatId);
                socketWriterMap.put(socket, out);

                out.println("NAME_ACCEPTED");


                synchronized (chatHistories) {
                    for (String msg : chatHistories.get(chatId)) {
                        out.println("MESSAGE " + msg);
                    }
                }

                broadcastMessage(chatId, "System: " + name + " has joined the chat");

                String input;
                while ((input = in.readLine()) != null) {
                    broadcastMessage(chatId, name + ": " + input);
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            } finally {

                if (out != null && chatId != null) {
                    synchronized (chatWriters) {
                        Set<PrintWriter> writers = chatWriters.get(chatId);
                        if (writers != null) {
                            writers.remove(out);
                        }
                    }
                }
                socketChatIdMap.remove(socket);
                socketWriterMap.remove(socket);
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        private void broadcastMessage(String chatId, String message) {

            synchronized (chatHistories) {
                chatHistories.get(chatId).add(message);
            }

            synchronized (chatWriters) {
                Set<PrintWriter> writers = chatWriters.get(chatId);
                if (writers != null) {
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + message);
                    }
                }
            }
        }
    }
}