package won.bot.jokebot.actions;

import java.lang.invoke.MethodHandles;
import java.net.URI;

import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.jokebot.context.JokeBotContextWrapper;
import won.protocol.model.Connection;
import won.protocol.util.WonRdfUtils;

/**
 * Created by MS on 24.09.2018.
 */
public class Message2ChuckNorrisAction extends BaseEventBotAction {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public Message2ChuckNorrisAction(EventListenerContext ctx) {
        super(ctx);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        logger.info("MessageEvent received");
        EventListenerContext ctx = getEventListenerContext();
        if (event instanceof MessageFromOtherAtomEvent
                        && ctx.getBotContextWrapper() instanceof JokeBotContextWrapper) {
            JokeBotContextWrapper botContextWrapper = (JokeBotContextWrapper) ctx.getBotContextWrapper();
            Connection con = ((MessageFromOtherAtomEvent) event).getCon();
            URI yourAtomUri = con.getAtomURI();
            String jokeUrl = botContextWrapper.getJokeURLForURI(yourAtomUri);
            String respondWith = jokeUrl != null ? "You wanna find me?\n Are you Chuck Norris enough to follow me? "
                            + jokeUrl
                            : "The Cuck is gone! There are way more important things than telling you about him... Deal with it";
            try {
                Model messageModel = WonRdfUtils.MessageUtils.textMessage(respondWith);
                getEventListenerContext().getEventBus().publish(new ConnectionMessageCommandEvent(con, messageModel));
            } catch (Exception te) {
                logger.error(te.getMessage());
            }
        }
    }
}
