package com.juleswhite.module3;

import com.juleswhite.module1.RegisterTool;

public class SchedulerTools {

    @RegisterTool
    public String getAvailability() {
        return "Available: 6/1/2025 at 9-10 AM";
    }

    @RegisterTool(terminal = true)
    public static void terminate(String message){
        System.out.println(message);
    }
}
