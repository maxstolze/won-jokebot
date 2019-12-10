package won.bot.jokebot.actions;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Date;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.EventBotActionUtils;
import won.bot.framework.eventbot.action.impl.atomlifecycle.AbstractCreateAtomAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.atomlifecycle.AtomCreatedEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.FailureResponseEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.jokebot.api.model.ChuckNorrisJoke;
import won.bot.jokebot.context.JokeBotContextWrapper;
import won.bot.jokebot.event.CreateAtomFromJokeEvent;
import won.protocol.message.WonMessage;
import won.protocol.service.WonNodeInformationService;
import won.protocol.util.DefaultAtomModelWrapper;
import won.protocol.util.RdfUtils;
import won.protocol.util.WonRdfUtils;
import won.protocol.vocabulary.SCHEMA;
import won.protocol.vocabulary.WONCON;
import won.protocol.vocabulary.WONMATCH;
import won.protocol.vocabulary.WXCHAT;

/**
 * Created by ms on 14.11.2019.
 */
public class CreateAtomFromJokeAction extends AbstractCreateAtomAction {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public CreateAtomFromJokeAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();
        if (event instanceof CreateAtomFromJokeEvent
                        && ctx.getBotContextWrapper() instanceof JokeBotContextWrapper) {
            JokeBotContextWrapper botContextWrapper = (JokeBotContextWrapper) ctx.getBotContextWrapper();
            // JokeBotsApi jokeBotsApi = ((CreateAtomFromJokeEvent) event).getJokeBotsApi();
            ChuckNorrisJoke chuckNorrisJoke = ((CreateAtomFromJokeEvent) event).getJoke();
            try {
                // Only one single random joke
                logger.info("Create 1 random Chuck Norris joke atom");
                this.createAtomFromjoke(ctx, botContextWrapper, chuckNorrisJoke);
            } catch (Exception me) {
                logger.error("messaging exception occurred:", me);
            }
        }
    }

    protected boolean createAtomFromjoke(EventListenerContext ctx, JokeBotContextWrapper botContextWrapper,
                    ChuckNorrisJoke chuckNorrisJoke) {
        if (botContextWrapper.getAtomUriForJokeURL(chuckNorrisJoke.getUrl()) != null) {
            logger.info("Atom already exists for joke: {}", chuckNorrisJoke.getUrl());
            return false;
        } else {
            final URI wonNodeUri = ctx.getNodeURISource().getNodeURI();
            WonNodeInformationService wonNodeInformationService = ctx.getWonNodeInformationService();
            final URI atomURI = wonNodeInformationService.generateAtomURI(wonNodeUri);
            Dataset dataset = this.generatejokeAtomStructure(atomURI, chuckNorrisJoke);
            logger.debug("creating atom on won node {} with content {} ", wonNodeUri,
                            StringUtils.abbreviate(RdfUtils.toString(dataset), 150));
            WonMessage createAtomMessage = ctx.getWonMessageSender()
                            .prepareMessage(createWonMessage(atomURI, wonNodeUri, dataset,
                                            false, false));
            EventBotActionUtils.rememberInList(ctx, atomURI, uriListName);
            botContextWrapper.addURIJokeURLRelation(chuckNorrisJoke.getUrl(), atomURI);
            EventBus bus = ctx.getEventBus();
            EventListener successCallback = event -> {
                logger.debug("atom creation successful, new atom URI is {}", atomURI);
                bus.publish(new AtomCreatedEvent(atomURI, wonNodeUri, dataset, null));
            };
            EventListener failureCallback = event -> {
                String textMessage = WonRdfUtils.MessageUtils
                                .getTextMessage(((FailureResponseEvent) event).getFailureMessage());
                logger.error("atom creation failed for atom URI {}, original message URI {}: {}", new Object[] {
                                atomURI, ((FailureResponseEvent) event).getOriginalMessageURI(), textMessage });
                EventBotActionUtils.removeFromList(ctx, atomURI, uriListName);
                botContextWrapper.removeURIJokeURLRelation(atomURI);
            };
            EventBotActionUtils.makeAndSubscribeResponseListener(createAtomMessage, successCallback, failureCallback,
                            ctx);
            logger.debug("registered listeners for response to message URI {}", createAtomMessage.getMessageURI());
            ctx.getWonMessageSender().sendMessage(createAtomMessage);
            logger.debug("atom creation message sent with message URI {}", createAtomMessage.getMessageURI());
            return true;
        }
    }

    private Dataset generatejokeAtomStructure(URI atomURI, ChuckNorrisJoke chuckNorrisJoke) {
        DefaultAtomModelWrapper atomModelWrapper = new DefaultAtomModelWrapper(atomURI);
        Resource atom = atomModelWrapper.getAtomModel().createResource(atomURI.toString());
        // @type
        // atom.addProperty(RDF.type, SCHEMA.JOBPOSTING);
        // s:url
        atom.addProperty(SCHEMA.URL, chuckNorrisJoke.getUrl());
        // s:title
        atom.addProperty(SCHEMA.TITLE, "Chuck Norris Joke #" + new Random().nextInt(1000));
        // s:datePosted
        // TODO:convert to s:Date (ISO 8601)
        atom.addProperty(SCHEMA.DATEPOSTED, new Date().toString());
        // s:image
        Resource image = atom.getModel().createResource();
        image.addProperty(RDF.type, SCHEMA.URL);
        image.addProperty(SCHEMA.VALUE, chuckNorrisJoke.getIcon_url());
        // s:description
        atom.addProperty(SCHEMA.DESCRIPTION, chuckNorrisJoke.getValue());
        // s:name
        atom.addProperty(SCHEMA.NAME, "Chuck Norris");
        // won:tags
        String[] tags = { "joke", "chuck norris", "chuck", "norris" };
        for (String tag : tags) {
            atom.addProperty(WONCON.tag, tag);
        }
        atomModelWrapper.addSocket("#ChatSocket", WXCHAT.ChatSocketString);
        atomModelWrapper.setDefaultSocket("#ChatSocket");
        atomModelWrapper.addFlag(WONMATCH.NoHintForMe);
        return atomModelWrapper.copyDataset();
    }
}
