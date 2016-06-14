package com.clanout.chatclient_poc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class ClientHandler extends ChannelInboundHandlerAdapter
{
    private MessageDelegate messageDelegate;

    public ClientHandler(MessageDelegate messageDelegate)
    {
        this.messageDelegate = messageDelegate;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        ByteBuf in = (ByteBuf) msg;
        messageDelegate.onRead(in);
    }
}
