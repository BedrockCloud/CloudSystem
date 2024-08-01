package de.cloud.base.templates;

import de.cloud.base.Base;
import de.cloud.base.service.LocalService;
import de.cloud.api.groups.ServiceGroup;
import de.cloud.api.service.CloudService;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public final class GroupTemplateService {

    private final File everyFolder = new File("templates/EVERY");
    private final File everyServiceFolder = new File("templates/EVERY_SERVICE");
    private final File everyProxyFolder = new File("templates/EVERY_PROXY");

    public GroupTemplateService() {
        this.everyFolder.mkdirs();
        this.everyServiceFolder.mkdirs();
        this.everyProxyFolder.mkdirs();
    }

    public void copyTemplates(@NotNull CloudService service) throws IOException {
        final var serviceFolder = ((LocalService) service).getWorkingDirectory();
        FileUtils.copyDirectory(this.everyFolder, serviceFolder);
        FileUtils.copyDirectory(service.getGroup().getGameServerVersion().isProxy()
            ? this.everyProxyFolder : this.everyServiceFolder, serviceFolder);

        final var templateDirection = new File("templates/" + service.getGroup().getTemplate());
        if (templateDirection.exists()) {
            FileUtils.copyDirectory(templateDirection, serviceFolder);
        }
    }

    public void createTemplateFolder(@NotNull ServiceGroup group) {
        if (!group.getNode().equalsIgnoreCase(Base.getInstance().getNode().getName())) return;
        final var file = new File("templates/" + group.getTemplate());
        if (!file.exists()) //noinspection ResultOfMethodCallIgnored
            file.mkdirs();
    }
}
