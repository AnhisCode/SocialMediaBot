package SocialMediaBot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
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
        String channelName = textChannel.getName();

        // which user texted
        Member userRaw = event.getMember();
        String username = Objects.requireNonNull(userRaw).getEffectiveName();
        String userID = userRaw.getId();

        // what is the content of their message
        String userMessage = event.getMessage().getContentDisplay().toLowerCase();
        String[] userCommand = userMessage.split(" ");

        // ok wahetevr asli
        if (Objects.equals(userCommand[0], "=>idea")) {
            event.getChannel().sendMessage("Yea it was ace1919191's idea ok? but this bot took me like " +
                    "60 hours to make so please give me the credit instead otherwise im gonna die on the inside - Anh").queue();
        }

        // leaderboard command
        if (Objects.equals(userCommand[0], "=>leaderboard")) {
            try {
                System.out.printf("[%s][%s]%s:%s\n", serverName, textChannel.getName(), username, userMessage);
                String twitchUser = userCommand[1];
                String embedColour;
                try {
                    embedColour = UpdateDB.getElement(twitchUser, "embedColour", channelID).get(0);
                } catch (IndexOutOfBoundsException e){
                    embedColour = MediaPost.defaultColour;
                }
                MediaPost.displayLeaderboard(textChannel, twitchUser, embedColour);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                event.getChannel().sendMessage("Please Mention a monitored twitch streamer's username").queue();
            }
        }

        // Get Started command
        if (Objects.equals(userCommand[0], "=>getstarted")) {
            System.out.printf("[%s][%s]%s:%s\n", serverName, textChannel.getName(), username, userMessage);
            MediaPost.displayInfo(textChannel);
        }

        // Get Started command
        if (Objects.equals(userCommand[0], "=>customisation")) {
            System.out.printf("[%s][%s]%s:%s\n", serverName, textChannel.getName(), username, userMessage);
            MediaPost.displayCustomisationInfo(textChannel);
        }

        if (userRaw.isOwner()) {
            // add user command
            if (Objects.equals(userCommand[0], "=>adduser")) {
                try {
                    String twitchUser = userCommand[1];
                    // check if user exist
                    if (!App.streamerExists(twitchUser)){
                        event.getChannel().sendMessage("Streamer: "+twitchUser+" does not exist").queue();
                    } else {
                        boolean success = UpdateDB.addTwitchUser(serverName, serverID, textChannel.getName(), channelID, twitchUser);
                        if (success) { // works
                            event.getChannel().sendMessage(String.format("User: %s added to monitored channels", twitchUser)).queue();
                            App.addMonitoredUser(twitchUser);
                            MediaPost.displayAddUser(channelID, twitchUser);
                            MediaPost.logInfo(String.format("[%s] added twitch user: %s to server %s", username, twitchUser, serverName));
                        } else {
                            event.getChannel().sendMessage(String.format("User may have already been set up in this text channel", twitchUser)).queue();
                        }
                    }
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
                        MediaPost.logInfo(String.format("[%s] removed twitch user: %s in server %s", username, twitchUser, serverName));
                        if(!UpdateDB.twitchUserExists(twitchUser)) {
                            App.removeMonitoredUser(twitchUser);
                        }
                    } else
                        event.getChannel().sendMessage(String.format("Unknown error has occured", twitchUser)).queue();
                } catch (IndexOutOfBoundsException e) {
                    event.getChannel().sendMessage("Please Mention a user to remove").queue();
                }
            }

            // set colour for individual streamer in the channel
            if(Objects.equals(userCommand[0], "=>setcolour")){
                try{
                    // check if enough arguments are given
                    String twitchUser = userCommand[1];
                    if(!UpdateDB.twitchUserExists(twitchUser)){
                        event.getChannel().sendMessage("Twitch streamer not found, please try again").queue();
                        return;
                    }
                    String newColour = userCommand[2];
                    // check if format is correct
                    Color.decode(newColour);
                    UpdateDB.changeColourIndividual(twitchUser, newColour, channelID);
                    MediaPost.displayColour(channelID, newColour);
                    MediaPost.logInfo(String.format("[%s] changed colour for twitch user: %s to %s in server %s", username, twitchUser, newColour, serverName));
                } catch(Exception e){
                    event.getChannel().sendMessage("Correct usage: **\"=>setcolour <twitch username> <0x??????>\"**" +
                            " where 0x?????? is the hexadecimal RGB code." +
                            " Hint, use https://www.rapidtables.com/web/color/RGB_Color.html\" to"+
                            "choose colour").queue();
                }
            }

            // set colour for all monitored streamers in the channel
            if(Objects.equals(userCommand[0], "=>setcolourall")){
                try{
                    // check if enough arguments are given
                    String newColour = userCommand[1];
                    // check if format is correct
                    Color.decode(newColour);
                    UpdateDB.changeColourAll(newColour, channelID);
                    MediaPost.displayColour(channelID, newColour);
                    MediaPost.logInfo(String.format("[%s] changed colour for all twitch user to %s in server %s", username, newColour, serverName));
                } catch(Exception e){
                    event.getChannel().sendMessage("Correct usage: **\"=>setcolourall <0x??????>\"**" +
                            " where 0x?????? is the hexadecimal RGB code. Hint, use https://www.rapidtables.com/web/color/RGB_Color.html" +
                            " to choose colour").queue();
                }
            }

            // set message for individual streamer in the channel
            if(Objects.equals(userCommand[0], "=>setmessage")){
                try{
                    // check if enough arguments are given
                    String twitchUser = userCommand[1];
                    if(!UpdateDB.twitchUserExists(twitchUser)){
                        event.getChannel().sendMessage("Twitch streamer not found, please try again").queue();
                        return;
                    }
                    StringBuilder newMessage = new StringBuilder();
                    for (int i = 2; i < userCommand.length; i++){
                        newMessage.append(userCommand[i]).append(" ");
                    }
                    String newMessages = newMessage.toString();
                    UpdateDB.changeMessageIndividual(twitchUser, newMessages, channelID);
                    event.getChannel().sendMessage(String.format("Message: \"%s\" set for streamer %s in channel %s",
                            newMessages,username,channelName)).queue();
                    MediaPost.logInfo(String.format("[%s] changed message for twitch user: %s in server %s", username, twitchUser, serverName));

                } catch(Exception e){
                    event.getChannel().sendMessage("Correct usage: **\"=>setmessage <twitch username> <Message (can" +
                            " be longer than one word)>\"**").queue();
                }
            }

            // set message for all streamer in the channel
            if(Objects.equals(userCommand[0], "=>setmessageall")){
                try{
                    // check if enough arguments are given
                    StringBuilder newMessage = new StringBuilder();
                    for (int i = 1; i < userCommand.length; i++){
                        newMessage.append(userCommand[i]).append(" ");
                    }
                    String newMessages = newMessage.toString();
                    UpdateDB.changeMessageAll(newMessages, channelID);
                    event.getChannel().sendMessage(String.format("Message: \"%s\" set for all streamer in channel %s",
                            newMessages,channelName)).queue();
                    MediaPost.logInfo(String.format("[%s] changed message for all twitch user in server %s", username, serverName));
                } catch(Exception e){
                    event.getChannel().sendMessage("Correct usage: **\"=>setmessageall <Message (can" +
                            " be longer than one word)>\"**").queue();
                }
            }

            System.out.printf("[%s][%s]%s:%s\n", serverName, textChannel.getName(), username, userMessage);
        }

    }

    public void onGuildJoin(GuildJoinEvent event){
        Guild newGuild = event.getGuild();
        String guildName = newGuild.getName();
        MediaPost.logInfo("Joined new server! Server name: " + guildName);
    }

}
