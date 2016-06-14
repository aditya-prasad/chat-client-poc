package com.clanout.chatclient_poc;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Client
{
    public static void main(String[] args) throws Exception
    {
        Connection connection = new Connection("0.0.0.0", 7777);

        connection.addMessageListener(System.out::println);

        connection.setConnectionStateListener(() -> {
            System.out.println("Connection closed");
            System.exit(0);
        });

        connection.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true)
        {
            String in = br.readLine();
            if (in.equals("exit()"))
            {
                connection.close();
                System.exit(0);
            }
            else
            {
                connection.write(in + "\r\n");
            }
        }
    }
}
