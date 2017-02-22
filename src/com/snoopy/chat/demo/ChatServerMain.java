package com.snoopy.chat.demo;

/**
 * Created by hnair20160706 on 2017/2/22.
 */
public class ChatServerMain {
    public static void main(String[] args) {
        NIOSServer server = new NIOSServer(60000);
        server.listen();
    }
}
