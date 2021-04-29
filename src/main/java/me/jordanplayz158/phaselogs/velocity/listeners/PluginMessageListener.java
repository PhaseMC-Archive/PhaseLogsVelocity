package me.jordanplayz158.phaselogs.velocity.listeners;

import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import me.jordanplayz158.phaselogs.velocity.PhaseLogs;

import java.util.HashMap;
import java.util.Map;

public class PluginMessageListener {
    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().toString().startsWith("logmessage:sent")) {
            return;
        }

        /* args[0] = prefix
         * args[1] = user (with ":")
         * args[2] = message
         */
        String[] args = ByteStreams.newDataInput(event.getData()).readUTF().split("\\s+");
        Player player = PhaseLogs.getInstance().getServer().getPlayer(args[1].substring(0, args[1].length() - 1)).orElse(null);

        StringBuilder format = new StringBuilder(PhaseLogs.getInstance().getConfig().get("botOutput"));
        StringBuilder message = new StringBuilder();

        for(int i = 2; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }

        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("{SERVER}", player.getCurrentServer().get().getServerInfo().getName());
            put("{PREFIX}", args[0]);
            put("{USERNAME}", player.getUsername());
            put("{MESSAGE}", message.toString());
        }};


        for(Map.Entry<String, String> placeholder : placeholders.entrySet()) {
            int index = format.indexOf(placeholder.getKey());


            while(index != -1) {
                format.delete(index, index + placeholder.getKey().length());
                format.insert(index, placeholder.getValue());

                index = format.indexOf(placeholder.getKey());
            }
        }

        char[] chatChars = "0123456789abcdefklmnor".toCharArray();

        for(char chatChar : chatChars) {
            String chatColor = "&" + chatChar;
            int index = format.indexOf(chatColor);

            while(index != -1) {
                format.delete(index, index + chatColor.length());

                index = format.indexOf(chatColor);
            }
        }

        PhaseLogs.getInstance().addMessage(format.toString());
    }
}
