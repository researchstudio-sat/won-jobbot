package won.bot.jobbot.actions;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import won.bot.framework.bot.context.BotContextWrapper;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.EventBotActionUtils;
import won.bot.framework.eventbot.action.impl.atomlifecycle.AbstractCreateAtomAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.atomlifecycle.AtomCreatedEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.FailureResponseEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.jobbot.api.model.HokifyJob;
import won.bot.jobbot.context.JobBotContextWrapper;
import won.bot.jobbot.event.CreateAtomFromJobEvent;
import won.bot.jobbot.util.JobAtomModelWrapper;
import won.protocol.message.WonMessage;
import won.protocol.service.WonNodeInformationService;
import won.protocol.util.RdfUtils;
import won.protocol.util.WonRdfUtils;

/**
 * Created by MS on 18.09.2018.
 */
public class CreateAtomFromJobAction extends AbstractCreateAtomAction {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final boolean createAllInOne;

    public CreateAtomFromJobAction(EventListenerContext eventListenerContext, boolean createAllInOne) {
        super(eventListenerContext);
        this.createAllInOne = createAllInOne;
    }

    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();
        if (event instanceof CreateAtomFromJobEvent && ctx.getBotContextWrapper() instanceof JobBotContextWrapper) {
            JobBotContextWrapper botContextWrapper = (JobBotContextWrapper) ctx.getBotContextWrapper();
            ArrayList<HokifyJob> hokifyJobs = ((CreateAtomFromJobEvent) event).getHokifyJobs();
            try {
                if (createAllInOne) {
                    logger.info("Create all job atoms");
                    for (HokifyJob hokifyJob : hokifyJobs) {
                        this.createAtomFromJob(ctx, botContextWrapper, hokifyJob);
                    }
                } else {
                    boolean created = false;
                    Random random = new Random();
                    while (!created) {
                        // Only one single random job
                        logger.info("Create 1 random job atom");
                        HokifyJob hokifyJob = hokifyJobs.get(random.nextInt(hokifyJobs.size()));
                        if (this.createAtomFromJob(ctx, botContextWrapper, hokifyJob)) {
                            created = true;
                        }
                    }
                }
            } catch (Exception me) {
                logger.error("messaging exception occurred:", me);
            }
        }
    }

    protected boolean createAtomFromJob(EventListenerContext ctx, JobBotContextWrapper botContextWrapper,
            HokifyJob hokifyJob) {
        if (botContextWrapper.getAtomForJob(hokifyJob.getUrl()) != null) {
            logger.info("Atom already exists for job: {}", hokifyJob.getUrl());
            return false;
        } else {
            final URI wonNodeUri = ctx.getNodeURISource().getNodeURI();
            WonNodeInformationService wonNodeInformationService = ctx.getWonNodeInformationService();
            final URI atomURI = wonNodeInformationService.generateAtomURI(wonNodeUri);
            Dataset dataset = new JobAtomModelWrapper(atomURI, hokifyJob).copyDataset();
            logger.debug("creating atom on won node {} with content {} ", wonNodeUri,
                    StringUtils.abbreviate(RdfUtils.toString(dataset), 150));
            WonMessage createAtomMessage = ctx.getWonMessageSender().prepareMessage(createWonMessage(atomURI, dataset));

            botContextWrapper.rememberAtomUri(atomURI);
            botContextWrapper.addAtomJobRelation(hokifyJob.getUrl(), atomURI);
            EventBus bus = ctx.getEventBus();
            EventListener successCallback = event -> {
                logger.debug("atom creation successful, new atom URI is {}", atomURI);
                bus.publish(new AtomCreatedEvent(atomURI, wonNodeUri, dataset, null));
            };
            EventListener failureCallback = event -> {
                String textMessage = WonRdfUtils.MessageUtils
                        .getTextMessage(((FailureResponseEvent) event).getFailureMessage());
                logger.error("atom creation failed for atom URI {}, original message URI {}: {}", atomURI,
                        ((FailureResponseEvent) event).getOriginalMessageURI(), textMessage);
                botContextWrapper.removeAtomUri(atomURI);

                botContextWrapper.removeAtomJobRelation(atomURI);
            };
            EventBotActionUtils.makeAndSubscribeResponseListener(createAtomMessage, successCallback, failureCallback,
                    ctx);
            logger.debug("registered listeners for response to message URI {}", createAtomMessage.getMessageURI());
            ctx.getWonMessageSender().sendMessage(createAtomMessage);
            logger.debug("atom creation message sent with message URI {}", createAtomMessage.getMessageURI());
            return true;
        }
    }
}
