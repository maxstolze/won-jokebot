package won.bot.jokebot.impl;

import java.lang.invoke.MethodHandles;
import java.time.Duration;

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
import won.bot.jokebot.actions.Connect2ChuckAction;
import won.bot.jokebot.actions.CreateAtomFromJokeAction;
import won.bot.jokebot.actions.Message2ChuckNorrisAction;
import won.bot.jokebot.api.JokeBotsApi;
import won.bot.jokebot.api.model.ChuckNorrisJoke;
import won.bot.jokebot.event.CreateAtomFromJokeEvent;
import won.bot.jokebot.event.FetchJokeDataEvent;
import won.bot.jokebot.event.StartJokeFetchEvent;

/**
 * This Bot checks the Chuck Norris Jokes and creates and publishes them as
 * atoms created by MS on 14.11.2019
 */
public class JokeBot extends EventBot {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private String botName;
    private int updateTime;
    private String jsonURL;
    private String geoURL;
    private int publishTime;
    private ChuckNorrisJoke chuckNorrisJoke;
    private EventBus bus;

    @Override
    protected void initializeEventListeners() {
        EventListenerContext ctx = getEventListenerContext();
        bus = getEventBus();
        JokeBotsApi jokeBotsApi = new JokeBotsApi(this.jsonURL);
        chuckNorrisJoke = jokeBotsApi.fetchJokeData();
        logger.info("Register JokeBot with update time {}", updateTime);
        try {
            bus = getEventBus();
            BotBehaviour executeWonMessageCommandBehaviour = new ExecuteWonMessageCommandBehaviour(ctx);
            executeWonMessageCommandBehaviour.activate();
            bus.subscribe(CreateAtomFromJokeEvent.class, new ActionOnEventListener(ctx, "CreateAtomFromJobEvent",
                            new CreateAtomFromJokeAction(ctx)));
            // Create the atoms
            BotTrigger createHokifyJobBotTrigger = new BotTrigger(ctx, Duration.ofMinutes(publishTime));
            createHokifyJobBotTrigger.activate();
            bus.subscribe(StartJokeFetchEvent.class, new ActionOnFirstEventListener(ctx,
                            new PublishEventAction(ctx, new StartBotTriggerCommandEvent(createHokifyJobBotTrigger))));
            bus.subscribe(BotTriggerEvent.class, new ActionOnTriggerEventListener(ctx, createHokifyJobBotTrigger,
                            new BaseEventBotAction(ctx) {
                                @Override
                                protected void doRun(Event event, EventListener executingListener) throws Exception {
                                    bus.publish(new CreateAtomFromJokeEvent(chuckNorrisJoke, jokeBotsApi));
                                }
                            }));
            // Get Hokify data
            BotTrigger fetchHokifyJobDataTrigger = new BotTrigger(ctx, Duration.ofMinutes(updateTime));
            fetchHokifyJobDataTrigger.activate();
            bus.subscribe(FetchJokeDataEvent.class, new ActionOnFirstEventListener(ctx,
                            new PublishEventAction(ctx, new StartBotTriggerCommandEvent(fetchHokifyJobDataTrigger))));
            bus.subscribe(BotTriggerEvent.class, new ActionOnTriggerEventListener(ctx, fetchHokifyJobDataTrigger,
                            new BaseEventBotAction(ctx) {
                                @Override
                                protected void doRun(Event event, EventListener executingListener) throws Exception {
                                    logger.info("Update Chucks Joke Data");
                                    chuckNorrisJoke = jokeBotsApi.fetchJokeData();
                                }
                            }));
            bus.subscribe(ConnectFromOtherAtomEvent.class,
                            new ActionOnEventListener(ctx, "ConnectReceived", new Connect2ChuckAction(ctx)));
            bus.subscribe(MessageFromOtherAtomEvent.class,
                            new ActionOnEventListener(ctx, "ReceivedTextMessage", new Message2ChuckNorrisAction(ctx)));
            bus.publish(new StartJokeFetchEvent());
            bus.publish(new FetchJokeDataEvent());
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
}
