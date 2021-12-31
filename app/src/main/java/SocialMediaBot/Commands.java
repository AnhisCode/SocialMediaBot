package SocialMediaBot;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Commands extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event){

        // the server name
        String serverName = event.getGuild().getName();

        // which text channel just got a message
        TextChannel textChannel = event.getTextChannel();

    }

}
