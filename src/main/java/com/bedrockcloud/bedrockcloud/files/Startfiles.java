package com.bedrockcloud.bedrockcloud.files;

import com.bedrockcloud.bedrockcloud.config.Config;
import com.bedrockcloud.bedrockcloud.console.Loggable;
import com.bedrockcloud.bedrockcloud.api.GroupAPI;
import com.bedrockcloud.bedrockcloud.SoftwareManager;

import java.io.File;
import java.util.ArrayList;

public class Startfiles implements Loggable
{
    private ArrayList<String> directorys;
    
    public Startfiles() {
        this.directorys = new ArrayList<String>();
        try {
            this.delete(new File("./temp"));
            this.directorys.add("./templates");
            this.directorys.add("./temp");
            this.directorys.add("./local");
            this.directorys.add("./archive");
            this.directorys.add(this.directorys.get(2) + "/plugins");
            this.directorys.add(this.directorys.get(2) + "/plugins/pocketmine");
            this.directorys.add(this.directorys.get(2) + "/plugins/waterdogpe");
            this.directorys.add(this.directorys.get(2) + "/versions");
            this.directorys.add(this.directorys.get(2) + "/versions/pocketmine");
            this.directorys.add(this.directorys.get(2) + "/versions/waterdogpe");
            this.directorys.add(this.directorys.get(2) + "/notify");
            this.directorys.add(this.directorys.get(3) + "/crashdumps");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        this.checkFolder();
    }
    
    public void deleteFolder(final File file) {
        if (file.isDirectory()) {
            final String[] fileList = file.list();
            if (fileList.length == 0) {
                file.delete();
            }
            else {
                for (final String fileName : fileList) {
                    final String fullPath = file.getPath() + "/" + fileName;
                    final File fileOrFolder = new File(fullPath);
                    this.delete(fileOrFolder);
                }
                this.delete(file);
            }
        }
        else {
            file.delete();
        }
    }
    
    public void delete(final File file) {
        if (file.isDirectory()) {
            final String[] fileList = file.list();
            if (fileList.length == 0) {
                file.delete();
            }
            else {
                for (final String fileName : fileList) {
                    final String fullPath = file.getPath() + "/" + fileName;
                    final File fileOrFolder = new File(fullPath);
                    this.delete(fileOrFolder);
                }
                this.delete(file);
            }
        } else {
            file.delete();
        }
    }
    
    private void checkFolder() {
        try {
            for (final String direc : this.directorys) {
                final File theDir = new File(direc);
                if (!theDir.exists()) {
                    this.getLogger().debug("Creating Folder in the Directory " + direc + "!");
                    theDir.mkdirs();
                }
            }
            final File templatesc = new File("./templates/config.json");
            if (!templatesc.exists()) {
                templatesc.createNewFile();
            }

            final File file = new File("./local/config.json");
            if (!file.exists()) {
                final Config config = new Config(file, 1);
                config.set("port", 32323.0);
                config.set("debug-mode", false);
                config.set("motd", "Default BedrockCloud service");
                config.set("auto-update-on-start", false);
                config.set("wdpe-login-extras", false);
                config.set("enable-cloudlog-file", false);
                config.set("use-proxy", false);
                config.set("version", "2.0.0");
                config.save(file);
                this.getLogger().debug("Check the server versions...");
                if (SoftwareManager.download("https://github.com/pmmp/PocketMine-MP/releases/latest/download/PocketMine-MP.phar", "./local/versions/pocketmine/PocketMine-MP.phar")) {
                    this.getLogger().debug("Pocketmine Downloaded");
                } else {
                    this.getLogger().debug("Error");
                }
                if (SoftwareManager.download("https://jenkins.waterdog.dev/job/Waterdog/job/WaterdogPE/job/master/lastSuccessfulBuild/artifact/target/Waterdog.jar", "./local/versions/waterdogpe/WaterdogPE.jar")) {
                    this.getLogger().debug("WaterdogPE Downloaded");
                }
            }
            GroupAPI.createGroup("Proxy-Master", 0);
            GroupAPI.createGroup("Lobby", 1);
        }
        catch (Exception e) {
            e.printStackTrace();
            this.getLogger().debug("An Error occured whilst trying to create the Files and Folder");
        }
    }
}
