package me.jordanplayz158.phaselogs.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import me.jordanplayz158.phaselogs.velocity.PhaseLogs;

public class LeaveAndJoinListener {
    @Subscribe
    public void onJoin(ServerPostConnectEvent event) {
        if(event.getPreviousServer() == null) {
            PhaseLogs.getInstance().addMessage(
                    event.getPlayer().getUsername()
                            + " has joined the network. ("
                            + event.getPlayer().getCurrentServer().get().getServerInfo().getName()
                            + ")"
            );
        }
    }

    @Subscribe
    public void onLeave(DisconnectEvent event) {
        PhaseLogs.getInstance().addMessage(
                event.getPlayer().getUsername()
                        + " has left the network. ("
                        + event.getPlayer().getCurrentServer().get().getServerInfo().getName()
                        + ")"
        );
    }

    @Subscribe
    public void onMove(ServerConnectedEvent event) {
        if (event.getPreviousServer().isPresent()) {
            PhaseLogs.getInstance().addMessage(
                    event.getPlayer().getUsername()
                            + " has moved from "
                            + event.getPreviousServer().get().getServerInfo().getName()
                            + " to "
                            + event.getServer().getServerInfo().getName()
            );
        }
    }
}