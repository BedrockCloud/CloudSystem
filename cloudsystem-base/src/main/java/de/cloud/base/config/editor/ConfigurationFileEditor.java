package de.cloud.base.config.editor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ConfigurationFileEditor {

    public ConfigurationFileEditor(final Path filePath, final ConfigReplace configReplace) {
        try {
            // Read all lines from the file
            List<String> lines = Files.readAllLines(filePath);

            // Apply replacements to each line
            List<String> updatedLines = lines.stream()
                .map(configReplace::replace)
                .toList(); // Collect results into a new list

            // Write updated lines back to the file
            Files.write(filePath, updatedLines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

