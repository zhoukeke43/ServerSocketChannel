import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws Exception {

        /*
        //System.out.println("Hello World!");
        //1.通过ServerSocketChannel 的open()方法创建一个ServerSocketChannel对象，open方法的作用：打开套接字通道
        ServerSocketChannel ssc = ServerSocketChannel.open();
        //2.通过ServerSocketChannel绑定ip地址和port(端口号)
        ssc.socket().bind(new InetSocketAddress("127.0.0.1", 3333));
        //通过ServerSocketChannelImpl的accept()方法创建一个SocketChannel对象用户从客户端读/写数据
        SocketChannel socketChannel = ssc.accept();
        //3.创建写数据的缓存区对象
        ByteBuffer writeBuffer = ByteBuffer.allocate(128);
        writeBuffer.put("hello WebClient this is from WebServer".getBytes());
        writeBuffer.flip();
        socketChannel.write(writeBuffer);
        //创建读数据的缓存区对象
        ByteBuffer readBuffer = ByteBuffer.allocate(128);
        //读取缓存区数据
        socketChannel.read(readBuffer);
        StringBuilder stringBuffer=new StringBuilder();
        //4.将Buffer从写模式变为可读模式
        readBuffer.flip();
        while (readBuffer.hasRemaining()) {
            stringBuffer.append((char) readBuffer.get());
        }
        System.out.println("从客户端接收到的数据："+stringBuffer);
        socketChannel.close();
        ssc.close();
        */

        try {
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.socket().bind(new InetSocketAddress("127.0.0.1", 3333));
            ssc.configureBlocking(false);

            Selector selector = Selector.open();
            // 注册 channel，并且指定感兴趣的事件是 Accept
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            ByteBuffer readBuff = ByteBuffer.allocate(1024);
            ByteBuffer writeBuff = ByteBuffer.allocate(128);
            writeBuff.put("received".getBytes());
            writeBuff.flip();

            while (true) {
                int nReady = selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();

                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (key.isAcceptable()) {
                        // 创建新的连接，并且把连接注册到selector上，而且，
                        // 声明这个channel只对读操作感兴趣。
                        SocketChannel socketChannel = ssc.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }
                    else if (key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        readBuff.clear();
                        socketChannel.read(readBuff);

                        readBuff.flip();
                        System.out.println("received : " + new String(readBuff.array()));
                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                    else if (key.isWritable()) {
                        writeBuff.rewind();
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        socketChannel.write(writeBuff);
                        key.interestOps(SelectionKey.OP_READ);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
