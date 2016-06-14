package com.clanout.chatclient_poc;

import io.netty.buffer.ByteBuf;

public interface MessageDelegate
{
    void onRead(ByteBuf in);
}
