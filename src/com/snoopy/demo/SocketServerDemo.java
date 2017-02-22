package com.snoopy.demo;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * Created by hnair20160706 on 2017/2/22.
 */
public class SocketServerDemo {
    //Socket协议服务端
    private int port = 9875;
    private ServerSocketChannel serverSocketChannel;
    private Charset charset = Charset.forName("UTF-8");
    private Selector selector = null;

    public SocketServerDemo() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().setReuseAddress(true);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        System.out.println("服务器启动");
    }

    /* 编码过程 */
    public ByteBuffer encode(String str) {
        return charset.encode(str);
    }

    /* 解码过程 */
    public String decode(ByteBuffer bb) {
        return charset.decode(bb).toString();
    }

    /* 服务器服务方法 */
    public void service() throws IOException {
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        /** 外循环，已经发生了SelectionKey数目 */
        while (selector.select() > 0) {
            /* 得到已经被捕获了的SelectionKey的集合 */
            Iterator iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = null;
                try {
                    key = (SelectionKey) iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                        SocketChannel sc = ssc.accept();
                        System.out
                                .println("客户端机子的地址是 "
                                        + sc.socket().getRemoteSocketAddress()
                                        + "  客户端机机子的端口号是 "
                                        + sc.socket().getLocalPort());
                        sc.configureBlocking(false);
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        sc.register(selector, SelectionKey.OP_READ , buffer);//buffer通过附件方式，传递
                    }
                    if (key.isReadable()) {
                        reveice(key);
                    }
                    if (key.isWritable()) {
//                       send(key);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        if (key != null) {
                            key.cancel();
                            key.channel().close();
                        }
                    } catch (ClosedChannelException cex) {
                        e.printStackTrace();
                    }
                }
            }
            /* 内循环完 */
        }
        /* 外循环完 */
    }

    int x = 1;

    /* 接收 */
    public void reveice(SelectionKey key) throws IOException {
        if (key == null)
            return;
        //***用SelectionKey.attachment()获取客户端消息***//
        //：通过附件方式，接收数据
//       ByteBuffer buff = (ByteBuffer) key.attachment();
        // SocketChannel sc = (SocketChannel) key.channel();
//       buff.limit(buff.capacity());
        // buff.position(0);
        // sc.read(buff);
        // buff.flip();
        // String reviceData = decode(buff);
        // System.out.println("接收：" + reviceData);

        //***用channel.read()获取客户端消息***//
        //：接收时需要考虑字节长度
        SocketChannel sc = (SocketChannel) key.channel();
        String content = "";
        //create buffer with capacity of 48 bytes
        ByteBuffer buf = ByteBuffer.allocate(3);//java里一个(utf-8)中文3字节,gbk中文占2个字节
        int bytesRead = sc.read(buf); //read into buffer.

        while (bytesRead >0) {
            buf.flip();  //make buffer ready for read
            while(buf.hasRemaining()){
                buf.get(new byte[buf.limit()]); // read 1 byte at a time
                content += new String(buf.array());
            }
            buf.clear(); //make buffer ready for writing
            bytesRead = sc.read(buf);
        }
        System.out.println("接收：" + content.trim());

        // sc.write(ByteBuffer.wrap(reviceData.getBytes()));
//      try {
//          sc.write(ByteBuffer.wrap(new String(
//                  "测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试" + (++x)).getBytes()));// 将消息回送给客户端
//      } catch (IOException e1) {
//          e1.printStackTrace();
//      }
    }

    int y = 0;
    public void send(SelectionKey key) {
//      if (key == null)
//          return;
//      ByteBuffer buff = (ByteBuffer) key.attachment();
//      SocketChannel sc = (SocketChannel) key.channel();
//      try {
//          sc.write(ByteBuffer.wrap(new String("aaaa").getBytes()));
//      } catch (IOException e1) {
//          e1.printStackTrace();
//      }
        System.out.println("send2() " +(++y));
    }

    /* 发送文件 */
    public void sendFile(SelectionKey key) {
        if (key == null)
            return;
        ByteBuffer buff = (ByteBuffer) key.attachment();
        SocketChannel sc = (SocketChannel) key.channel();
        String data = decode(buff);
        if (data.indexOf("get") == -1)
            return;
        String subStr = data.substring(data.indexOf(" "), data.length());
        System.out.println("截取之后的字符串是 " + subStr);
        FileInputStream fileInput = null;
        try {
            fileInput = new FileInputStream(subStr);
            FileChannel fileChannel = fileInput.getChannel();
            fileChannel.transferTo(0, fileChannel.size(), sc);
            fileChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileInput.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new SocketServerDemo().service();
    }
}
