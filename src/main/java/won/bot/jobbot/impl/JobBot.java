package won.bot.jobbot.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.bot.base.EventBot;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.action.impl.PublishEventAction;
import won.bot.framework.eventbot.action.impl.trigger.ActionOnTriggerEventListener;
import won.bot.framework.eventbot.action.impl.trigger.BotTrigger;
import won.bot.framework.eventbot.action.impl.trigger.BotTriggerEvent;
import won.bot.framework.eventbot.action.impl.trigger.StartBotTriggerCommandEvent;
import won.bot.framework.eventbot.behaviour.BotBehaviour;
import won.bot.framework.eventbot.behaviour.ExecuteWonMessageCommandBehaviour;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.wonmessage.ConnectFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.eventbot.listener.impl.ActionOnEventListener;
import won.bot.framework.eventbot.listener.impl.ActionOnFirstEventListener;
import won.bot.jobbot.actions.Connect2HokifyAction;
import won.bot.jobbot.actions.CreateAtomFromJobAction;
import won.bot.jobbot.actions.Message2HokifyAction;
import won.bot.jobbot.api.HokifyBotsApi;
import won.bot.jobbot.api.model.HokifyJob;
import won.bot.jobbot.event.CreateAtomFromJobEvent;
import won.bot.jobbot.event.FetchHokifyJobDataEvent;
import won.bot.jobbot.event.StartHokifyFetchEvent;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.ArrayList;

/**
 * This Bot checks the Hokify jobs and creates and publishes them as atoms
 * created by MS on 17.09.2018
 */
public class JobBot extends EventBot {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private String botName;
    private int updateTime;
    private String jsonURL;
    private String geoURL;
    private int publishTime;
    private boolean createAllInOne;
    private ArrayList<HokifyJob> hokifyJobsList;
    private EventBus bus;

    @Override
    protected void initializeEventListeners() {
        EventListenerContext ctx = getEventListenerContext();
        bus = getEventBus();
        HokifyBotsApi hokifyBotsApi = new HokifyBotsApi(this.jsonURL, this.geoURL);
        hokifyJobsList = hokifyBotsApi.fetchHokifyData();
        logger.info("Register JobBot with update time {}", updateTime);
        try {
            bus = getEventBus();
            BotBehaviour executeWonMessageCommandBehaviour = new ExecuteWonMessageCommandBehaviour(ctx);
            executeWonMessageCommandBehaviour.activate();
            bus.subscribe(CreateAtomFromJobEvent.class, new ActionOnEventListener(ctx, "CreateAtomFromJobEvent",
                            new CreateAtomFromJobAction(ctx, this.createAllInOne)));
            // Create the atoms
            BotTrigger createHokifyJobBotTrigger = new BotTrigger(ctx, Duration.ofMinutes(publishTime));
            createHokifyJobBotTrigger.activate();
            bus.subscribe(StartHokifyFetchEvent.class, new ActionOnFirstEventListener(ctx,
                            new PublishEventAction(ctx, new StartBotTriggerCommandEvent(createHokifyJobBotTrigger))));
            bus.subscribe(BotTriggerEvent.class, new ActionOnTriggerEventListener(ctx, createHokifyJobBotTrigger,
                            new BaseEventBotAction(ctx) {
                                @Override
                                protected void doRun(Event event, EventListener executingListener) throws Exception {
                                    bus.publish(new CreateAtomFromJobEvent(hokifyJobsList, hokifyBotsApi));
                                }
                            }));
            // Get Hokify data
            BotTrigger fetchHokifyJobDataTrigger = new BotTrigger(ctx, Duration.ofMinutes(updateTime));
            fetchHokifyJobDataTrigger.activate();
            bus.subscribe(FetchHokifyJobDataEvent.class, new ActionOnFirstEventListener(ctx,
                            new PublishEventAction(ctx, new StartBotTriggerCommandEvent(fetchHokifyJobDataTrigger))));
            bus.subscribe(BotTriggerEvent.class, new ActionOnTriggerEventListener(ctx, fetchHokifyJobDataTrigger,
                            new BaseEventBotAction(ctx) {
                                @Override
                                protected void doRun(Event event, EventListener executingListener) throws Exception {
                                    logger.info("Update Hokify Job Data");
                                    hokifyJobsList = hokifyBotsApi.fetchHokifyData();
                                }
                            }));
            // WON initiated Events
            /*
             * When uncommenting this one day, remember to EventBotAction hint2HokifyAction
             * = new Hint2HokifyAction(ctx); bus.subscribe(AtomHintFromMatcherEvent.class,
             * new ActionOnEventListener(ctx, "AtomHintReceived", hint2HokifyAction));
             * bus.subscribe(SocketHintFromMatcherEvent.class, new
             * ActionOnEventListener(ctx, "AtomHintReceived", hint2HokifyAction));
             */
            bus.subscribe(ConnectFromOtherAtomEvent.class,
                            new ActionOnEventListener(ctx, "ConnectReceived", new Connect2HokifyAction(ctx)));
            bus.subscribe(MessageFromOtherAtomEvent.class,
                            new ActionOnEventListener(ctx, "ReceivedTextMessage", new Message2HokifyAction(ctx)));
            bus.publish(new StartHokifyFetchEvent());
            bus.publish(new FetchHokifyJobDataEvent());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String getBotName() {
        return this.botName;
    }

    public void setBotName(final String botName) {
        this.botName = botName;
    }

    public void setJsonURL(final String jsonURL) {
        this.jsonURL = jsonURL;
    }

    public int getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(int updateTime) {
        this.updateTime = updateTime;
    }

    public String getGeoURL() {
        return geoURL;
    }

    public void setGeoURL(String geoURL) {
        this.geoURL = geoURL;
    }

    public int getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(int publishTime) {
        this.publishTime = publishTime;
    }

    public boolean isCreateAllInOne() {
        return createAllInOne;
    }

    public void setCreateAllInOne(boolean createAllInOne) {
        this.createAllInOne = createAllInOne;
    }
}
