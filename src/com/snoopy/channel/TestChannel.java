package com.snoopy.channel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * Created by hnair20160706 on 2017/2/22.
 */
public class TestChannel {
    public static void main(String[] args) {
        Thread sThread = new Thread(new Runnable() {
            private  int port = 60000;
            private ServerSocketChannel serverSocketChannel;
            private Charset charset = Charset.forName("UTF-8");
            private Selector selector = null;
            @Override
            public void run() {
                try {
                    /**服务器启动**/
                    selector = Selector.open();
                    serverSocketChannel = ServerSocketChannel.open();
                    serverSocketChannel.socket().setReuseAddress(true);
                    serverSocketChannel.socket().bind(new InetSocketAddress(port));
                    System.out.println("服务器启动");
                    service();




                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            /**服务器服务方法**/
            public void service(){
                try {
                    serverSocketChannel.configureBlocking(false);
                    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                    while (selector.select()>0){
//                        System.out.println("服务");
                        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                        while (iterator.hasNext()){
                            SelectionKey key = null;
                            key = iterator.next();
                            iterator.remove();
                            if (key.isAcceptable()){
                                System.out.println("isAcceptable");
                                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                                SocketChannel sc = ssc.accept();
                                System.out.println("客户端机子的地址是 "
                                        + sc.socket().getRemoteSocketAddress()
                                        + "  客户端机机子的端口号是 "
                                        + sc.socket().getLocalPort());
                                sc.configureBlocking(false);
                                ByteBuffer buffer = ByteBuffer.allocate(48);
                                sc.register(selector, SelectionKey.OP_READ, buffer);//buffer
                            }
                            if (key.isReadable()){
                                SocketChannel sc = (SocketChannel) key.channel();
                                ByteBuffer bf = ByteBuffer.allocate(48);
                               int bytesRead;
                                while ((bytesRead = sc.read(bf))!=-1){
                                    System.out.println(new String(bf.array()));
                                }
                            }
                            if (key.isWritable()){

                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        });



        Thread threadCilent = new Thread(new Runnable() {
            private Selector selector;
            private SocketChannel socketChannel;
            private String hostIp="127.0.0.1";
            private  int hostListenningPort=60000;


            @Override
            public void run() {
                try {
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(false);
                    selector = Selector.open();
                    socketChannel.connect(new InetSocketAddress(hostIp,hostListenningPort));
                    socketChannel.register(selector,SelectionKey.OP_CONNECT);
                    clientReceive();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            public void  clientReceive(){
                while (true){
                    try {
                        selector.select();//如果队列有新的Channel加入，那么Selector.select()会被唤醒
                        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                        while (iterator.hasNext()){
                            SelectionKey key = iterator.next();
                            iterator.remove();
                            if (key.isConnectable()){
                                SocketChannel channel = (SocketChannel) key.channel();
                                //如果正在连接，则完成连接
                                if (channel.isConnectionPending()){
                                    channel.finishConnect();
                                }
                                //设置非堵塞
                                channel.configureBlocking(false);
                                channel.write(ByteBuffer.wrap(new String("测试").getBytes()));
                                channel.register(selector,SelectionKey.OP_READ);
                            }else if (key.isReadable()) {

                                // 服务器可读取消息:得到事件发生的Socket通道
                                SocketChannel channel = (SocketChannel) key.channel();
                                // 创建读取的缓冲区
                                ByteBuffer buffer = ByteBuffer.allocate(10);
                                channel.read(buffer);
                                byte[] data = buffer.array();
                                String msg = new String(data).trim();
                                System.out.println(msg);
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }



        });
        sThread.start();
        try {
            Thread.currentThread().sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        threadCilent.start();
    }
}
