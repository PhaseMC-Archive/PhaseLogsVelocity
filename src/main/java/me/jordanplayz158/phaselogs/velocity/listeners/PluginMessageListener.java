package me.jordanplayz158.phaselogs.velocity.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import me.jordanplayz158.phaselogs.velocity.PhaseLogs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PluginMessageListener {
    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().toString().startsWith("logmessage:sent")) {
            return;
        }

        /* args[0] = uuid
         * args[1] = prefix
         * args[2] = user (with ":")
         * args[3] = message
         */
        String[] args = ByteStreams.newDataInput(event.getData()).readUTF().split("\\s+");
        Player player = PhaseLogs.getInstance().getServer().getPlayer(args[2].substring(0, args[2].length() - 1)).orElse(null);

        sendCustomData(player, args[0]);

        StringBuilder format = new StringBuilder(PhaseLogs.getInstance().getConfig().get("botOutput"));
        StringBuilder message = new StringBuilder();

        for(int i = 3; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }

        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("{SERVER}", player.getCurrentServer().get().getServerInfo().getName());
            put("{PREFIX}", args[1]);
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

    public void sendCustomData(Player player, String id) {
        Collection<Player> networkPlayers = PhaseLogs.getInstance().getServer().getAllPlayers();
        // perform a check to see if globally are no players
        if (networkPlayers == null || networkPlayers.isEmpty()) {
            return;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(id); // this data could be whatever you want

        // we send the data to the server
        // using ServerInfo the packet is being queued if there are no players in the server
        // using only the server to send data the packet will be lost if no players are in it
        player.sendPluginMessage(MinecraftChannelIdentifier.create("logmessage","received"), out.toByteArray());
    }
}
