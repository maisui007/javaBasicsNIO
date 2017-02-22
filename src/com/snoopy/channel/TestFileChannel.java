package com.snoopy.channel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by hnair20160706 on 2017/2/22.
 */
public class TestFileChannel {
    public static void main(String[] args) {
        try {
//            RandomAccessFile accessFile = new RandomAccessFile("E:\\MyStudyCode\\javaBasicsNIO\\src\\com\\snoopy\\resources\\NIO-data.txt","rw");
//            RandomAccessFile accessFile1 = new RandomAccessFile("E:\\MyStudyCode\\javaBasicsNIO\\src\\com\\snoopy\\resources\\TONIO-data.txt","rw");
//            FileChannel fileChannel = accessFile.getChannel();
//            FileChannel fileChannel1 = accessFile1.getChannel();
//            long position = 0;
//            long count = fileChannel1.size();
//            fileChannel1.transferFrom(fileChannel,position,count);
//            fileChannel.transferTo(position,count,fileChannel1);


//
//            ByteBuffer buffer = ByteBuffer.allocate(48);//创建缓冲区48个字节
//            Charset charset = Charset.forName("UTF-8");
////            int bytesRead = fileChannel.read(buffer);//从通道读取数据到缓冲区
//            int bytesRead;
//            while ((bytesRead =fileChannel.read(buffer)) != -1){
//                System.out.println("Read "+ bytesRead);
//            buffer.flip();
//                fileChannel.write(buffer);
////                System.out.println(charset.decode(buffer));
////            while (buffer.hasRemaining()){
////                System.out.println((char)buffer.get());
////            }
//            buffer.clear();
////            bytesRead = fileChannel.read(buffer);
//            }
//            fileChannel.close();
            Thread serverSocketChannelThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("开启服务器start.....");
                    try{
                        Selector selector = Selector.open();
                        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                        serverSocketChannel.configureBlocking(false);
                        serverSocketChannel.socket().bind(new InetSocketAddress(60000));
                        SelectionKey selectionKey =  serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                        while (true){
                            int readyChannels = selector.select();
                            System.out.println("readyChannels = "+readyChannels);
                            if (readyChannels == 0) continue;
                            Set selectKeys = selector.selectedKeys();
                            Iterator<SelectionKey> keyIterator = selectKeys.iterator();
                            while (keyIterator.hasNext()) {
                                System.out.println(selectKeys.size());
                                SelectionKey key = (SelectionKey) keyIterator.next( );
                                if (key.isAcceptable()) {
                                    ServerSocketChannel server = (ServerSocketChannel) key.channel( );
                                    SocketChannel channel = server.accept( );
                                    if (channel == null) {
                                        ;//handle code, could happen
                                        keyIterator.remove();
                                    }
                                    channel.configureBlocking(false);
                                    channel.register(selector, SelectionKey.OP_READ);

                                }
                                if (key.isReadable()) {
                                    readDataFromSocket (key);
                                }
                                keyIterator.remove();
                            }
//                            while (keyIterator.hasNext()){
//                                SelectionKey key = keyIterator.next();
//                                if (key.isAcceptable()){
//                                    ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
//                                    SocketChannel sc = ssc.accept();
//
//
//                                }else if (key.isConnectable()){
//
//                                }else if (key.isReadable()){
//
//                                }else if ((key.isWritable())){
//
//                                }
//                                keyIterator.remove();
//                            }
                        }






                    }catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("开启服务器end.....");

                }
            });
            serverSocketChannelThread.start();

            Selector selector = Selector.open();
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
//            socketChannel.socket().bind(new InetSocketAddress("localhost", 60000));
//            socketChannel.register(selector,SelectionKey.OP_WRITE);













        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readDataFromSocket(SelectionKey key) {
        try {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(48);
        Charset charset = Charset.forName("UTF-8");
        int count;
        while ((count = socketChannel.read(buffer))!=-1){
            buffer.flip();
            System.out.println(charset.decode(buffer));
            socketChannel.write(buffer);
            buffer.clear();
        }
            System.out.println("准备关闭socketChannel");
            socketChannel.close();

    }catch (Exception e){
            e.printStackTrace();
        }
    }


}
