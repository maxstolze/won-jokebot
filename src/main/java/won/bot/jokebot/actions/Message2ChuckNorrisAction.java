package won.bot.jokebot.actions;

import java.lang.invoke.MethodHandles;
import java.net.URI;

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
import won.bot.jokebot.event.DeleteJokeAtomEvent;
import won.protocol.message.WonMessage;
import won.protocol.model.Connection;
import won.protocol.util.WonRdfUtils;

/**
 * Created by MS on 24.09.2019.
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
            Connection con = ((MessageFromOtherAtomEvent) event).getCon();
            URI jokeAtomUri = con.getAtomURI();
            String message = "";
            try {
                WonMessage msg = ((MessageEvent) event).getWonMessage();
                message = extractTextMessageFromWonMessage(msg);
            } catch (Exception te) {
                logger.error(te.getMessage());
            }
            String responseMessge = "You want more? Just type \"more\"\n"
                            + "To shred this joke: \"shred\"";
            if (message.equalsIgnoreCase("more")) {
                ChuckNorrisJoke chuckNorrisJoke = jokeBotsApi.fetchJokeData();
                String newJokeText = chuckNorrisJoke.getValue();
                responseMessge = "Okay, how about this one: \n" + newJokeText;
            } else if (message.equalsIgnoreCase("shred")) {
                responseMessge = "Okay, as you wish. I will delete this joke now";
                // Trigger Delete Event
                try {
                    getEventListenerContext().getEventBus().publish(new DeleteJokeAtomEvent(jokeAtomUri));
                } catch (Exception te) {
                    logger.error(te.getMessage());
                }
            }
            try {
                Model messageModel = WonRdfUtils.MessageUtils.textMessage(responseMessge);
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
