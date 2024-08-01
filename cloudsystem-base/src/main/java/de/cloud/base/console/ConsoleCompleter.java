package de.cloud.base.console;

import de.cloud.base.Base;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public record ConsoleCompleter(SimpleConsoleManager consoleManager) implements Completer {

    @Override
    public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> candidates) {
        String input = parsedLine.line();
        String[] arguments = input.split(" ");
        List<String> suggestions = null;

        var consoleInput = consoleManager.getInputs().peek();
        if (input.isEmpty() || input.indexOf(' ') == -1) {
            suggestions = (consoleInput == null) ? suggestCommands(arguments) : consoleInput.tabCompletions();
        } else {
            if (consoleInput == null) {
                var command = Base.getInstance().getCommandManager().getCachedCloudCommands().get(arguments[0]);
                arguments = prepareArguments(input, arguments);
                if (command != null) {
                    suggestions = command.tabComplete(arguments);
                }
            }
        }

        if (suggestions != null && !suggestions.isEmpty()) {
            suggestions.stream().filter(Objects::nonNull).map(Candidate::new).forEach(candidates::add);
        }
    }

    private List<String> suggestCommands(String[] arguments) {
        var registeredCommands = Base.getInstance().getCommandManager().getCachedCloudCommands().keySet();
        String lastArg = arguments[arguments.length - 1].trim();
        var result = new LinkedList<String>();

        for (var command : registeredCommands) {
            if (command.toLowerCase().contains(lastArg.toLowerCase())) {
                result.add(command);
            }
        }

        if (result.isEmpty() && !registeredCommands.isEmpty()) {
            result.addAll(registeredCommands);
        }

        return result;
    }

    private String[] prepareArguments(String input, String[] arguments) {
        if (input.endsWith(" ")) {
            arguments = Arrays.copyOfRange(arguments, 1, arguments.length + 1);
            arguments[arguments.length - 1] = "";
        } else {
            arguments = Arrays.copyOfRange(arguments, 1, arguments.length);
        }
        return arguments;
    }
}
