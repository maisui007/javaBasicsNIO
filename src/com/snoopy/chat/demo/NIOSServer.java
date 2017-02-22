package com.snoopy.chat.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by hnair20160706 on 2017/2/22.
 */
public class NIOSServer {
    private int port;
    /**
     * 解码buffer
     **/
    private Charset charset = Charset.forName("UTF-8");
    /**
     * 接收数据缓冲区
     **/

    private static ByteBuffer rbyteBuffer = ByteBuffer.allocate(1024);
    /**
     * 发送数据缓冲区
     **/
    private static ByteBuffer sbyteBuffer = ByteBuffer.allocate(1024);
    /**
     * 映射客户端channel
     **/

    private Map<String, SocketChannel> clientMap = new HashMap<>();
    /**
     * 选择器
     **/
    private static Selector selector;

    public NIOSServer(int port) {
        this.port = port;
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() throws IOException {
        /**1.启动服务端**/
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        /**2.服务端配置为非阻塞**/
        serverSocketChannel.configureBlocking(false);
        /**3.服务端绑定端口**/
        serverSocketChannel.bind(new InetSocketAddress(port));
        /**4.实例化选择器**/
        selector = Selector.open();
        /**5.服务端注册accept事件；ACCEPT事件：当服务器收到客户端连接请求时，触发该事件**/
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务端初始化完成");
    }

    public void listen() {
        /**服务端轮询监听，select 方法会一直堵塞，直到有相关事件发生**/
        while (true) {
            try {
                int evenCount = selector.select();
                System.out.println("服务端轮询监听到事件 eventCount :" + evenCount + "个");
                /**1.获取事件的集合**/
                Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                /**2.迭代集合**/
                for (SelectionKey key : selectionKeySet) {
                        handle(key);
                }
                /**3.清理处理过的事件**/
                selectionKeySet.clear();


            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

        }
    }

    private void handle(SelectionKey selectionKey) throws IOException {
        /**根据事件不同，进行不同的处理**/
        ServerSocketChannel server = null;
        SocketChannel client = null;
        String receiveText = null;
        if (selectionKey.isAcceptable()) {
            /**1.客户端请求连接事件**/
            /**1.1ServertSocket为客户端建立socket连接，将为此socket注册READ事件，监听客户端的输入**/
            server = (ServerSocketChannel) selectionKey.channel();
            client = server.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
        } else if (selectionKey.isReadable()) {
            /**2.收到客户端发送的数据，读取数据后继续注册监听客户端**/
            client = (SocketChannel) selectionKey.channel();
            rbyteBuffer.clear();
            int count;
            while ((count = client.read(rbyteBuffer)) >0) {
                rbyteBuffer.flip();
                receiveText = String.valueOf(charset.decode(rbyteBuffer).array());
                System.out.println(client.toString() + ":" + receiveText);
                /**业务处理**/
                /**读取数据后继续监听客户端**/
                client = (SocketChannel) selectionKey.channel();
                client.register(selector, SelectionKey.OP_READ);
            }

        }
//        else if (selectionKey.isWritable()){
//            SocketChannel sc = (SocketChannel) selectionKey.channel();
//          try {
//              sc.write(ByteBuffer.wrap(new String("0000").getBytes()));
//              sc.register(selector,SelectionKey.OP_WRITE);
//          } catch (IOException e1) {
//              e1.printStackTrace();
//          }
//        }


    }

    /**
     * 把当前客户端信息 推送到其他客户端
     */
    private void dispatch(SocketChannel client, String info) throws IOException {
        Socket s = client.socket();
        String name = "[" + s.getInetAddress().toString().substring(1) + ":" + Integer.toHexString(client.hashCode()) + "]";
        if (!clientMap.isEmpty()) {
            for (Map.Entry<String, SocketChannel> entry : clientMap.entrySet()) {
                SocketChannel temp = entry.getValue();
                if (!client.equals(temp)) {
                    sbyteBuffer.clear();
                    sbyteBuffer.put((name + ":" + info).getBytes());
                    sbyteBuffer.flip();
                    //输出到通道
                    temp.write(sbyteBuffer);
                }
            }
        }
        clientMap.put(name, client);
    }
}
