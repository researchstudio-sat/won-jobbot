package won.bot.jobbot.actions;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.EventBotActionUtils;
import won.bot.framework.eventbot.action.impl.atomlifecycle.AbstractDeleteAtomAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.wonmessage.FailureResponseEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.jobbot.api.model.HokifyJob;
import won.bot.jobbot.context.JobBotContextWrapper;
import won.bot.jobbot.event.CreateAtomFromJobEvent;
import won.protocol.message.WonMessage;
import won.protocol.util.WonRdfUtils;

/**
 * Created by MS on 06.02.2020.
 */
public class DeleteAtomFromJobAction extends AbstractDeleteAtomAction {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public DeleteAtomFromJobAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);

    }

    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();
        if (event instanceof CreateAtomFromJobEvent && ctx.getBotContextWrapper() instanceof JobBotContextWrapper) {
            JobBotContextWrapper botContextWrapper = (JobBotContextWrapper) ctx.getBotContextWrapper();
            ArrayList<HokifyJob> hokifyJobs = ((CreateAtomFromJobEvent) event).getHokifyJobs();
            // generate IndexList from JobURLs
            ArrayList<String> jobsIndexList = new ArrayList<String>();
            hokifyJobs.forEach((HokifyJob job) -> jobsIndexList.add(job.getUrl()));
            System.out.println("TEST me" + jobsIndexList);
            try {
                ArrayList<String> jobsInDb = botContextWrapper.getAllJobs();
                logger.info("Check all jobs if still available");
                for (String jobInDb : jobsInDb) {
                    if (!jobsIndexList.contains(jobInDb)) {
                        // job is outdated -> remove job from db
                        final URI wonNodeUri = ctx.getNodeURISource().getNodeURI();
                        final URI atomURI = new URI(botContextWrapper.getAtomForJob(jobInDb));
                        logger.debug("deleting atom on won node {} with uri {} ", wonNodeUri, atomURI);
                        WonMessage deleteAtomMessage = ctx.getWonMessageSender()
                                .prepareMessage(buildWonMessage(atomURI));

                        EventListener successCallback = new EventListener() {
                            @Override
                            public void onEvent(Event event) {
                                logger.debug("atom deletion successful, URI was {}", atomURI);
                                botContextWrapper.removeAtomJobRelation(jobInDb);
                                EventBotActionUtils.removeFromList(ctx, atomURI, uriListName);
                            }
                        };
                        EventListener failureCallback = new EventListener() {
                            @Override
                            public void onEvent(Event event) {
                                String textMessage = WonRdfUtils.MessageUtils
                                        .getTextMessage(((FailureResponseEvent) event).getFailureMessage());
                                logger.error("atom deletion failed for atom URI {}, original message URI {}: {}",
                                        atomURI, ((FailureResponseEvent) event).getOriginalMessageURI(), textMessage);
                            }
                        };
                        EventBotActionUtils.makeAndSubscribeResponseListener(deleteAtomMessage, successCallback,
                                failureCallback, ctx);
                        logger.debug("registered listeners for response to message URI {}",
                                deleteAtomMessage.getMessageURI());
                        ctx.getWonMessageSender().sendMessage(deleteAtomMessage);
                        logger.debug("atom deletion message sent with message URI {}",
                                deleteAtomMessage.getMessageURI());

                    }
                }

            } catch (Exception me) {
                logger.error("messaging exception occurred:", me);
            }
        }
    }
}
