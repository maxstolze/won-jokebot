package won.bot.jokebot.impl;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import won.bot.framework.bot.base.EventBot;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.behaviour.BotBehaviour;
import won.bot.framework.eventbot.behaviour.ExecuteWonMessageCommandBehaviour;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.impl.wonmessage.ConnectFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.framework.eventbot.filter.impl.NotFilter;
import won.bot.framework.eventbot.listener.impl.ActionOnEventListener;
import won.bot.framework.extensions.serviceatom.ServiceAtomBehaviour;
import won.bot.framework.extensions.serviceatom.ServiceAtomExtension;
import won.bot.jokebot.actions.Connect2ChuckAction;
import won.bot.jokebot.actions.DeleteJokeAtomAction;
import won.bot.jokebot.actions.Message2ChuckNorrisAction;
import won.bot.jokebot.event.DeleteJokeAtomEvent;

/**
 * This Bot checks the Chuck Norris Jokes and creates and publishes them as
 * atoms created by MS on 14.11.2019
 */
public class JokeBot extends EventBot implements ServiceAtomExtension {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private String botName;
    private int updateTime;
    private String jsonURL;
    private String geoURL;
    private int publishTime;
    private EventBus bus;
    private ServiceAtomBehaviour serviceAtomBehaviour;

    @Override
    protected void initializeEventListeners() {
        EventListenerContext ctx = getEventListenerContext();
        bus = getEventBus();
        logger.info("Register JokeBot with update time {}", updateTime);
        try {
            bus = getEventBus();
            BotBehaviour executeWonMessageCommandBehaviour = new ExecuteWonMessageCommandBehaviour(ctx);
            executeWonMessageCommandBehaviour.activate();
            serviceAtomBehaviour = new ServiceAtomBehaviour(ctx);
            serviceAtomBehaviour.activate();
            // filter to prevent reacting to serviceAtom<->ownedAtom events;
            NotFilter noInternalServiceAtomEventFilter = getNoInternalServiceAtomEventFilter();
            bus.subscribe(ConnectFromOtherAtomEvent.class, noInternalServiceAtomEventFilter,
                            new Connect2ChuckAction(ctx, this.jsonURL));
            bus.subscribe(MessageFromOtherAtomEvent.class,
                            new Message2ChuckNorrisAction(ctx, this.jsonURL));
            bus.subscribe(DeleteJokeAtomEvent.class, new ActionOnEventListener(ctx, new DeleteJokeAtomAction(ctx)));
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

    @Override
    public ServiceAtomBehaviour getServiceAtomBehaviour() {
        return this.serviceAtomBehaviour;
    }
}
