package com.juleswhite.module3;

import com.juleswhite.module1.RegisterTool;

import java.util.Map;

public class EmailTools {

    @RegisterTool
    public String sendEmail(String message) {
        return "Email Sent: " + message;
    }

    @RegisterTool(terminal = true)
    public static void terminate(String message){
        System.out.println(message);
    }

}
