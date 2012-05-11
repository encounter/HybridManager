package com.encounterpc.hybridmanager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RootShell {
    public static String execute(String command) {
        String[] str = new String[1];
        str[0] = command;
        return execute(str);
    }

    public static String execute(String[] commands) {
        return execute(commands, true);
    }

    public static String execute(String[] commands, boolean errorStream) {
        DataOutputStream dos;
        DataInputStream dis;
        DataInputStream des = null;
        Process process = null;

        try {
            process = Runtime.getRuntime().exec("su -c sh");
        } catch (IOException ignored) {
        }

        if (process != null) {
            dos = new DataOutputStream(process.getOutputStream());
            dis = new DataInputStream(process.getInputStream());
            if (errorStream)
                des = new DataInputStream(process.getErrorStream());
            try {
                String result = "";
                for (String single : commands) {
                    dos.writeBytes(single + "\n");
                    dos.flush();
                }
                dos.writeBytes("exit\n");
                dos.flush();
                process.waitFor();
                while (dis.available() > 0)
                    result += dis.readLine() + "\n";
                if (errorStream) {
                    while (des.available() > 0)
                        result += des.readLine() + "\n";
                }
                dis.close();
                dos.close();
                if (errorStream)
                    des.close();
                return result;
            } catch (Exception e) {
                return e.getMessage();
            }
        }
        return null;
    }
}