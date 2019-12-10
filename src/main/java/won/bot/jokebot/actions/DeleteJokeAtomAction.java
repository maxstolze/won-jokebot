package won.bot.jokebot.actions;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.EventBotActionUtils;
import won.bot.framework.eventbot.action.impl.atomlifecycle.AbstractDeleteAtomAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.wonmessage.FailureResponseEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.jokebot.context.JokeBotContextWrapper;
import won.bot.jokebot.event.DeleteJokeAtomEvent;
import won.protocol.message.WonMessage;
import won.protocol.util.WonRdfUtils;

public class DeleteJokeAtomAction extends AbstractDeleteAtomAction {
    private static final Logger logger = LoggerFactory.getLogger(DeleteJokeAtomAction.class);

    public DeleteJokeAtomAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) {
        EventListenerContext ctx = getEventListenerContext();
        if (!(ctx.getBotContextWrapper() instanceof JokeBotContextWrapper) || !(event instanceof DeleteJokeAtomEvent)) {
            logger.error("DeleteJokeAtomAction does not work without a JokeBotContextWrapper and DeleteJokeAtomEvent");
            throw new IllegalStateException(
                            "DeleteJokeAtomAction does not work without a JokeBotContextWrapper and DeleteJokeAtomEvent");
        }
        JokeBotContextWrapper botContextWrapper = (JokeBotContextWrapper) ctx.getBotContextWrapper();
        DeleteJokeAtomEvent deleteJokeAtomEvent = (DeleteJokeAtomEvent) event;
        final URI atomURI = deleteJokeAtomEvent.getAtomUriToDelete();
        /*
         * if (botContextWrapper.getAtomUriForJokeURL(atomURI.toString()) == null) {
         * logger.
         * warn("JokeAtom does not exist in the botContext(must have been deleted) no deletion possible: "
         * + atomURI); botContextWrapper.removeURIJokeURLRelation(atomURI); return; }
         */
        final URI wonNodeUri = ctx.getNodeURISource().getNodeURI();
        logger.debug("deleting atom on won node {} with uri {} ", wonNodeUri, atomURI);
        WonMessage deleteAtomMessage = buildWonMessage(atomURI);
        EventListener successCallback = new EventListener() {
            @Override
            public void onEvent(Event event) {
                logger.debug("atom deletion successful, URI was {}", atomURI);
                botContextWrapper.removeURIJokeURLRelation(atomURI);
                EventBotActionUtils.removeFromList(ctx, atomURI, uriListName);
            }
        };
        EventListener failureCallback = new EventListener() {
            @Override
            public void onEvent(Event event) {
                String textMessage = WonRdfUtils.MessageUtils
                                .getTextMessage(((FailureResponseEvent) event).getFailureMessage());
                logger.error("atom deletion failed for atom URI {}, original message URI {}: {}", atomURI,
                                ((FailureResponseEvent) event).getOriginalMessageURI(), textMessage);
            }
        };
        EventBotActionUtils.makeAndSubscribeResponseListener(deleteAtomMessage, successCallback, failureCallback, ctx);
        logger.debug("registered listeners for response to message URI {}", deleteAtomMessage.getMessageURI());
        ctx.getWonMessageSender().prepareAndSendMessage(deleteAtomMessage);
        logger.debug("atom deletion message sent with message URI {}", deleteAtomMessage.getMessageURI());
    }
}
