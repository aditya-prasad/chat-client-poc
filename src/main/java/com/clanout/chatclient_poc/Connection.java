package com.clanout.chatclient_poc;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.util.ArrayList;
import java.util.List;

public class Connection implements AutoCloseable, MessageDelegate
{
    private String host;
    private int port;

    private State state;
    private Channel channel;
    private ConnectionStateListener connectionStateListener;

    private List<MessageListener> messageListeners;

    public Connection(String host, int port)
    {
        this.host = host;
        this.port = port;
        state = State.NOT_CONNECTED;
        messageListeners = new ArrayList<>();
    }

    public void setConnectionStateListener(ConnectionStateListener connectionStateListener)
    {
        this.connectionStateListener = connectionStateListener;
    }

    public void connect() throws Exception
    {
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);

        Bootstrap bootstrap = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress(host, port)
                .handler(new ClientHandler(this))
                .option(ChannelOption.SO_KEEPALIVE, true);

        channel = bootstrap.connect().await().channel();
        if (channel.isActive())
        {
            state = State.CONNECTED;
        }

        channel.closeFuture().addListeners(channelFuture -> {
            state = State.NOT_CONNECTED;
            if (connectionStateListener != null)
            {
                connectionStateListener.onDisconnect();
            }
        });
    }

    public boolean isConnected()
    {
        return state == State.CONNECTED;
    }

    public void write(String msg) throws ConnectionInactiveException
    {
        if (state != State.CONNECTED)
        {
            throw new ConnectionInactiveException();
        }

        ByteBuf byteBuf = channel.alloc().buffer();
        byteBuf.writeBytes(msg.getBytes());
        channel.writeAndFlush(byteBuf);
    }

    public void close() throws Exception
    {
        if (state != State.CONNECTED)
        {
            throw new ConnectionInactiveException();
        }

        channel.close().await();
    }

    public void addMessageListener(MessageListener messageListener)
    {
        messageListeners.add(messageListener);
    }

    public void removeMessageListener(MessageListener messageListener)
    {
        messageListeners.remove(messageListener);
    }

    @Override
    public void onRead(ByteBuf in)
    {
        String input = in.toString(CharsetUtil.UTF_8);

        for (MessageListener listener : messageListeners)
        {
            listener.onMessageReceived(input);
        }
    }

    enum State
    {
        NOT_CONNECTED,
        CONNECTED
    }
}
