/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package SocialMediaBot;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;

import javax.security.auth.login.LoginException;

public class App {

    private static String token = "";

    public static void main(String[] args) throws LoginException {
        try {
            Dotenv dotenv = Dotenv.load();
            token = dotenv.get("TOKEN");
        }
        catch (Exception e){
            e.printStackTrace();
        }
        JDABuilder jda = JDABuilder.createDefault(token);
        jda.setStatus(OnlineStatus.ONLINE);
        jda.addEventListeners(new Commands());
        jda.build();
    }

}
