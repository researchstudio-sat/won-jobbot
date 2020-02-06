package won.bot.jobbot.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.jobbot.context.JobBotContextWrapper;
import won.protocol.model.Connection;

import java.lang.invoke.MethodHandles;
import java.net.URI;

/**
 * Created by MS on 24.09.2018.
 */
public class Message2HokifyAction extends BaseEventBotAction {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public Message2HokifyAction(EventListenerContext ctx) {
        super(ctx);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        logger.info("MessageEvent received");
        EventListenerContext ctx = getEventListenerContext();
        if (event instanceof MessageFromOtherAtomEvent && ctx.getBotContextWrapper() instanceof JobBotContextWrapper) {
            JobBotContextWrapper botContextWrapper = (JobBotContextWrapper) ctx.getBotContextWrapper();
            Connection con = ((MessageFromOtherAtomEvent) event).getCon();
            URI atomUri = con.getAtomURI();
            String jobUrl = botContextWrapper.getJobForAtom(atomUri);
            String respondWith = jobUrl != null ? "You need more information?\n Just follow this link: " + jobUrl
                    : "The job is no longer available, sorry!";
            try {
                getEventListenerContext().getEventBus().publish(new ConnectionMessageCommandEvent(con, respondWith));
            } catch (Exception te) {
                logger.error(te.getMessage());
            }
        }
    }
}
