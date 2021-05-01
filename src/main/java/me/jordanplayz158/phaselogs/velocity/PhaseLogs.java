package me.jordanplayz158.phaselogs.velocity;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import me.jordanplayz158.phaselogs.velocity.listeners.LeaveAndJoinListener;
import me.jordanplayz158.phaselogs.velocity.listeners.PluginMessageListener;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Plugin(id = "phaselogs", name = "Phase Logs", version = "0.0.1-SNAPSHOT",
        url = "https://jordanplayz158.me", description = "Phase Logs for Velocity", authors = {"JordanPlayz158"})
public class PhaseLogs {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private static PhaseLogs instance;
    private FileConfig config;
    private List<String> messages = new ArrayList<>();


    @Inject
    public PhaseLogs(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        instance = this;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getChannelRegistrar().register(MinecraftChannelIdentifier.create("logmessage", "sent"));
        server.getEventManager().register(this, new PluginMessageListener());
        server.getEventManager().register(this, new LeaveAndJoinListener());

        File dataDirectory = this.dataDirectory.toFile();

        if (!dataDirectory.exists()) {
            dataDirectory.mkdir();
        }
        File configFile = new File(dataDirectory, "config.toml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (InputStream is = PhaseLogs.class.getResourceAsStream("/" + configFile.getName());
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create configuration file", e);
            }
        }

        config = FileConfig.of(configFile);

        config.load();

        new DiscordBot();
    }

    public ProxyServer getServer() {
        return server;
    }

    public static PhaseLogs getInstance() {
        return instance;
    }

    public FileConfig getConfig() {
        return config;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void addMessage(String message) {
        messages.add(message);
    }
}
