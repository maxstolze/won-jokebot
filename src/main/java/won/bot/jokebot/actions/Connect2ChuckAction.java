package won.bot.jokebot.actions;

import java.lang.invoke.MethodHandles;
import java.net.URI;

import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.command.connect.ConnectCommandEvent;
import won.bot.framework.eventbot.event.impl.command.connect.ConnectCommandResultEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.ConnectFromOtherAtomEvent;
import won.bot.framework.eventbot.filter.impl.CommandResultFilter;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.eventbot.listener.impl.ActionOnFirstEventListener;
import won.bot.jokebot.context.JokeBotContextWrapper;
import won.protocol.model.Connection;
import won.protocol.util.WonRdfUtils;

/**
 * Created by ms on 14.11.2019.
 */
public class Connect2ChuckAction extends BaseEventBotAction {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public Connect2ChuckAction(EventListenerContext ctx) {
        super(ctx);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        logger.info("ConnectionEvent received");
        EventListenerContext ctx = getEventListenerContext();
        if (event instanceof ConnectFromOtherAtomEvent
                        && ctx.getBotContextWrapper() instanceof JokeBotContextWrapper) {
            JokeBotContextWrapper botContextWrapper = (JokeBotContextWrapper) ctx.getBotContextWrapper();
            Connection con = ((ConnectFromOtherAtomEvent) event).getCon();
            URI yourAtomUri = con.getAtomURI();
            try {
                String welcomeMessage = "Not funny enough?\n Chuck Norris will find you!";
                URI localSocket = con.getSocketURI();
                URI targetSocket = con.getTargetSocketURI();
                final ConnectCommandEvent openCommandEvent = new ConnectCommandEvent(localSocket, targetSocket,
                                welcomeMessage);
                ctx.getEventBus().subscribe(ConnectCommandResultEvent.class, new ActionOnFirstEventListener(ctx,
                                new CommandResultFilter(openCommandEvent), new BaseEventBotAction(ctx) {
                                    @Override
                                    protected void doRun(Event event, EventListener executingListener)
                                                    throws Exception {
                                        ConnectCommandResultEvent connectionMessageCommandResultEvent = (ConnectCommandResultEvent) event;
                                        if (connectionMessageCommandResultEvent.isSuccess()) {
                                            String jokeUrl = botContextWrapper.getJokeURLForURI(yourAtomUri);
                                            String respondWith = jokeUrl != null
                                                            ? "You want more jokes?"
                                                            : "The Cuck is gone! There are way more important things than telling you about him... Deal with it";
                                            Model messageModel = WonRdfUtils.MessageUtils.textMessage(respondWith);
                                            ctx.getEventBus().publish(
                                                            new ConnectionMessageCommandEvent(con, messageModel));
                                        } else {
                                            logger.error("FAILURERESPONSEEVENT FOR JOKE PAYLOAD");
                                        }
                                    }
                                }));
                ctx.getEventBus().publish(openCommandEvent);
            } catch (Exception te) {
                logger.error(te.getMessage());
            }
        }
    }
}
