package me.jordanplayz158.phaselogs.velocity;

import com.electronwill.nightconfig.core.Config;
import me.jordanplayz158.utils.Initiate;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.log4j.Level;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DiscordBot extends ListenerAdapter {
    private final Config config;
    private JDA jda;

    public DiscordBot() {
        config = PhaseLogs.getInstance().getConfig();

        Initiate.log(Level.toLevel(config.get("logLevel")));

        JDABuilder jdaBuilder = JDABuilder.createLight(config.get("token"));

        try {
            jda = jdaBuilder
                    .addEventListeners(this)
                    .setActivity(Activity.of(Activity.ActivityType.valueOf(config.get("activity.type").toString().toUpperCase()), config.get("activity.name")))
                    .build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    public void onReady(@NotNull ReadyEvent event) {
        String delay = config.get("messageBufferRate");
        long delayLong = Long.parseLong(delay.substring(0, delay.length() - 1));

        PhaseLogs.getInstance().getServer().getScheduler()
                .buildTask(PhaseLogs.getInstance(), this::sendMessages)
                .repeat(delayLong, getTimeUnit(delay))
                .schedule();
    }

    private TimeUnit getTimeUnit(String time) {
        switch(time.substring(time.length() - 1).toLowerCase()) {
            case "s":
                return TimeUnit.SECONDS;
            case "m":
                return TimeUnit.MINUTES;
            case "h":
                return TimeUnit.HOURS;
            case "d":
                return TimeUnit.DAYS;
            default:
                return null;
        }
    }

    public void sendMessages() {
        Guild guild = jda.getGuilds().get(0);

        long guildId = config.getLong("guild");

        if(guildId != 0L) {
            guild = jda.getGuildById(guildId);
        }

        StringBuilder log = new StringBuilder();

        List<String> messages = PhaseLogs.getInstance().getMessages();

        if(messages.size() < 1) {
            return;
        }

        for(int i = 0; i < messages.size();) {
            if(log.length() + messages.get(i).length() > 2000) {
                break;
            }

            log.append(messages.get(i)).append("\n");

            PhaseLogs.getInstance().getMessages().remove(i);
        }

        if(log.toString().isEmpty()) {
            return;
        }

        assert guild != null;
        Objects.requireNonNull(guild.getTextChannelById(config.getLong("logChannel"))).sendMessage(log.toString()).queue();
    }
}
