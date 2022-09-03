package com.bedrockcloud.bedrockcloud;

import com.bedrockcloud.bedrockcloud.files.Startfiles;

public class ClassNotFound {
    public static void main(String args[]) {
        try {
            Class.forName("com.bedrockcloud.bedrockcloud.BedrockCloud");

            new Startfiles();
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                BedrockCloud.getLogger().exception(e);
            }
            new BedrockCloud();

        }catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}
