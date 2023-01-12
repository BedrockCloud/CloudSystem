package com.bedrockcloud.bedrockcloud.command;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import com.bedrockcloud.bedrockcloud.BedrockCloud;
import com.bedrockcloud.bedrockcloud.console.Loggable;

public class CommandManager extends Thread implements Loggable
{
    private final Set<Command> commands;
    private String message;

    public CommandManager() {
        Scanner scanner = new Scanner(System.in);
        this.commands = new HashSet<Command>();
    }
    
    @Override
    public void run() {
        String answer = null;
        while (BedrockCloud.isRunning()) {
            answer = new Scanner(System.in).nextLine();
            final String cmd = answer.split(" ")[0];
            final String[] args = this.dropFirstString(answer.split(" "));
            this.executeCommand(cmd, args);
        }
    }
    
    public String[] dropFirstString(final String[] input) {
        final String[] anstring = new String[input.length - 1];
        System.arraycopy(input, 1, anstring, 0, input.length - 1);
        return anstring;
    }
    
    public void addCommand(final Command cmd) {
        this.commands.add(cmd);
    }
    
    public void executeCommand(final String command, final String[] args) {
        final Command cmd = this.getCommand(command);
        if (cmd != null) {
            cmd.onCommand(args);
        } else if (!command.equals("")) {
            this.getLogger().info("This Command doesn't exist Try help for more!");
        }
    }
    
    public Command getCommand(final String name) {
        for (final Command command : this.commands) {
            if (command.getCommand().equalsIgnoreCase(name)) {
                return command;
            }
        }
        return null;
    }
    
    public Command getCommand(final Class<? extends Command> name) {
        for (final Command command : this.commands) {
            if (command.getClass().equals(name)) {
                return command;
            }
        }
        return null;
    }

    public Set<Command> getCommands() {
        return this.commands;
    }
}
