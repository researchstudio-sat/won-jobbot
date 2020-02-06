package won.bot.jobbot.context;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import won.bot.framework.bot.context.BotContext;
import won.bot.framework.extensions.serviceatom.ServiceAtomEnabledBotContextWrapper;

/**
 * Created by MS on 24.09.2018.
 */
public class JobBotContextWrapper extends ServiceAtomEnabledBotContextWrapper {

    private String atomjobRelations;
    private String jobAtomRelations;

    public JobBotContextWrapper(BotContext botContext, String botName) {
        super(botContext, botName);
        this.atomjobRelations = botName + ":atomjobRelations";
        this.jobAtomRelations = botName + ":jobAtomRelations";
    }

    public void addAtomJobRelation(String jobURL, URI atomURI) {
        getBotContext().saveToObjectMap(atomjobRelations, atomURI.toString(), jobURL);
        getBotContext().saveToObjectMap(jobAtomRelations, jobURL, atomURI.toString());

    }

    public void removeAtomJobRelation(URI atomURI) {
        String jobURL = (String) getBotContext().loadFromObjectMap(atomjobRelations, atomURI.toString());
        getBotContext().removeFromObjectMap(atomjobRelations, atomURI.toString());
        getBotContext().removeFromObjectMap(jobAtomRelations, jobURL);
    }

    public void removeAtomJobRelation(String jobURL) {
        String atomURI = (String) getBotContext().loadFromObjectMap(jobAtomRelations, jobURL);
        getBotContext().removeFromObjectMap(atomjobRelations, atomURI);
        getBotContext().removeFromObjectMap(jobAtomRelations, jobURL);
    }

    public String getJobForAtom(URI atomURI) {
        return (String) getBotContext().loadFromObjectMap(atomjobRelations, atomURI.toString());
    }

    public String getAtomForJob(String jobURL) {
        return (String) getBotContext().loadFromObjectMap(jobAtomRelations, jobURL);
    }

    public ArrayList<String> getAllJobs() {
        Map<String, Object> jobMap = getBotContext().loadObjectMap(this.jobAtomRelations);
        ArrayList<String> jobList = new ArrayList<String>();
        jobList.addAll(jobMap.keySet());
        return jobList;
    }
}
