package won.bot.jobbot.event;

import won.bot.framework.eventbot.event.BaseEvent;
import won.bot.jobbot.api.HokifyBotsApi;
import won.bot.jobbot.api.model.HokifyJob;

import java.util.ArrayList;

/**
 * Created by MS on 18.09.2018.
 */
public class CreateAtomFromJobEvent extends BaseEvent {
    private final ArrayList<HokifyJob> hokifyJobs;
    private final HokifyBotsApi hokifyBotsApi;

    public CreateAtomFromJobEvent(ArrayList<HokifyJob> hokifyJobs, HokifyBotsApi hokifyBotsApi) {
        this.hokifyJobs = hokifyJobs;
        this.hokifyBotsApi = hokifyBotsApi;
    }

    public ArrayList<HokifyJob> getHokifyJobs() {
        return hokifyJobs;
    }

    public HokifyBotsApi getHokifyBotsApi() {
        return hokifyBotsApi;
    }
}
