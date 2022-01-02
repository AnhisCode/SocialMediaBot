package SocialMediaBot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.util.Objects;

public class Commands extends ListenerAdapter {

    public static JDA jda;

    @Override
    public void onReady(ReadyEvent ev) {
        jda = ev.getJDA();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        // the server name
        String serverName = event.getGuild().getName();
        String serverID = event.getGuild().getId();

        // which text channel just got a message
        TextChannel textChannel = event.getTextChannel();
        String channelID = textChannel.getId();

        // which user texted
        Member userRaw = event.getMember();
        String username = Objects.requireNonNull(userRaw).getEffectiveName();
        String userID = userRaw.getId();

        // what is the content of their message
        String userMessage = event.getMessage().getContentDisplay().toLowerCase();
        String[] userCommand = userMessage.split(" ");

        // leaderboard command
        if (Objects.equals(userCommand[0], "=>leaderboard")) {
            try {
                String twitchUser = userCommand[1];
                MediaPost.displayLeaderboard(textChannel, twitchUser);
            } catch (IndexOutOfBoundsException e) {
                event.getChannel().sendMessage("Please Mention a monitored twitch streamer's username").queue();
            }
        }

        if (userRaw.isOwner()) {
            // add user command
            if (Objects.equals(userCommand[0], "=>adduser")) {
                try {
                    String twitchUser = userCommand[1];
                    boolean success = UpdateDB.addTwitchUser(serverName, serverID, textChannel.getName(), channelID, twitchUser);
                    if (success) { // works
                        event.getChannel().sendMessage(String.format("User: %s added to monitored channels", twitchUser)).queue();
                        App.addMonitoredUser(twitchUser);
                    } else
                        event.getChannel().sendMessage(String.format("User may have already been set up in this text channel", twitchUser)).queue();
                } catch (IndexOutOfBoundsException e) {
                    event.getChannel().sendMessage("Please Mention a user to add").queue();
                }
            }

            // remove user command
            if (Objects.equals(userCommand[0], "=>removeuser")) {
                try {
                    String twitchUser = userCommand[1];
                    boolean success = UpdateDB.removeTwitchUser(serverID, channelID, twitchUser);
                    if (success) { // works
                        event.getChannel().sendMessage(String.format("User: %s removed from monitored channels", twitchUser)).queue();
                        App.removeMonitoredUser(twitchUser);
                    } else
                        event.getChannel().sendMessage(String.format("Unknown error has occured", twitchUser)).queue();
                } catch (IndexOutOfBoundsException e) {
                    event.getChannel().sendMessage("Please Mention a user to remove").queue();
                }
            }
        }

    }

}
