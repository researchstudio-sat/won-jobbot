package won.bot.jobbot.context;

import won.bot.framework.bot.context.BotContext;
import won.bot.framework.bot.context.BotContextWrapper;

import java.net.URI;

/**
 * Created by MS on 24.09.2018.
 */
public class JobBotContextWrapper extends BotContextWrapper {
    private String uriJobURLRelationsName;
    private String jobUrlUriRelationsName;

    public JobBotContextWrapper(BotContext botContext, String botName) {
        super(botContext, botName);
        this.uriJobURLRelationsName = botName + ":uriJobURLRelations";
        this.jobUrlUriRelationsName = botName + ":jobUrlUriRelations";
    }

    public void addURIJobURLRelation(String jobURL, URI uri) {
        getBotContext().saveToObjectMap(jobUrlUriRelationsName, jobURL, uri.toString());
        getBotContext().saveToObjectMap(uriJobURLRelationsName, uri.toString(), jobURL);
    }

    public void removeURIJobURLRelation(URI uri) {
        String jobURL = (String) getBotContext().loadFromObjectMap(uriJobURLRelationsName, uri.toString());
        getBotContext().removeFromObjectMap(uriJobURLRelationsName, uri.toString());
        getBotContext().removeFromObjectMap(jobUrlUriRelationsName, jobURL);
    }

    public String getJobURLForURI(URI uri) {
        return (String) getBotContext().loadFromObjectMap(uriJobURLRelationsName, uri.toString());
    }

    public String getAtomUriForJobURL(String jobURL) {
        return (String) getBotContext().loadFromObjectMap(jobUrlUriRelationsName, jobURL);
    }
}
