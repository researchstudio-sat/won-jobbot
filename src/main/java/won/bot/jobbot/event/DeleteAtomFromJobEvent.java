package won.bot.jobbot.event;

import won.bot.framework.eventbot.event.BaseEvent;
import won.bot.jobbot.api.model.HokifyJob;

import java.util.ArrayList;

/**
 * Created by MS on 06.02.2020.
 */
public class DeleteAtomFromJobEvent extends BaseEvent {
    private final ArrayList<HokifyJob> hokifyJobs;

    public DeleteAtomFromJobEvent(ArrayList<HokifyJob> hokifyJobs) {
        this.hokifyJobs = hokifyJobs;
    }

    public ArrayList<HokifyJob> getHokifyJobs() {
        return hokifyJobs;
    }
}
