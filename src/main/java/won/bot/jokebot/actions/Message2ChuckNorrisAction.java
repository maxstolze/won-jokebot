package won.bot.jokebot.actions;

import java.lang.invoke.MethodHandles;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.MessageEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.jokebot.api.JokeBotsApi;
import won.bot.jokebot.api.model.ChuckNorrisJoke;
import won.bot.jokebot.context.JokeBotContextWrapper;
import won.protocol.message.WonMessage;
import won.protocol.model.Connection;
import won.protocol.util.WonRdfUtils;

/**
 * Created by MS on 24.09.2018.
 */
public class Message2ChuckNorrisAction extends BaseEventBotAction {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private JokeBotsApi jokeBotsApi;

    public Message2ChuckNorrisAction(EventListenerContext ctx, JokeBotsApi jokeBotsApi) {
        super(ctx);
        this.jokeBotsApi = jokeBotsApi;
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        logger.info("MessageEvent received");
        EventListenerContext ctx = getEventListenerContext();
        if (event instanceof MessageFromOtherAtomEvent
                        && ctx.getBotContextWrapper() instanceof JokeBotContextWrapper) {
            // JokeBotContextWrapper botContextWrapper = (JokeBotContextWrapper)
            // ctx.getBotContextWrapper();
            Connection con = ((MessageFromOtherAtomEvent) event).getCon();
            // URI yourAtomUri = con.getAtomURI();
            String message = "";
            try {
                WonMessage msg = ((MessageEvent) event).getWonMessage();
                message = extractTextMessageFromWonMessage(msg);
            } catch (Error e) {
            }
            String respondWith = "You want more? Just type \"more\"";
            if (message.equalsIgnoreCase("more")) {
                // TODO: fetch new joke: and add here
                ChuckNorrisJoke chuckNorrisJoke = jokeBotsApi.fetchJokeData();
                String newJokeText = chuckNorrisJoke.getValue();
                respondWith = "Okay, how about this one: \n" + newJokeText;
            }
            try {
                Model messageModel = WonRdfUtils.MessageUtils.textMessage(respondWith);
                getEventListenerContext().getEventBus().publish(new ConnectionMessageCommandEvent(con, messageModel));
            } catch (Exception te) {
                logger.error(te.getMessage());
            }
        }
    }

    private String extractTextMessageFromWonMessage(WonMessage wonMessage) {
        if (wonMessage == null)
            return null;
        String message = WonRdfUtils.MessageUtils.getTextMessage(wonMessage);
        return StringUtils.trim(message);
    }
}
