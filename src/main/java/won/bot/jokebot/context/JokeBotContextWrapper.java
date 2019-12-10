package won.bot.jokebot.context;

import java.net.URI;

import won.bot.framework.bot.context.BotContext;
import won.bot.framework.extensions.serviceatom.ServiceAtomContext;
import won.bot.framework.extensions.serviceatom.ServiceAtomEnabledBotContextWrapper;

/**
 * Created by MS on 14.11.2019.
 */
public class JokeBotContextWrapper extends ServiceAtomEnabledBotContextWrapper implements ServiceAtomContext {
    private String uriJokeURLRelationsName;
    private String jokeUrlUriRelationsName;

    public JokeBotContextWrapper(BotContext botContext, String botName) {
        super(botContext, botName);
        this.uriJokeURLRelationsName = botName + ":urijokeURLRelations";
        this.jokeUrlUriRelationsName = botName + ":jokeUrlUriRelations";
    }

    public void addURIJokeURLRelation(String jokeURL, URI uri) {
        getBotContext().saveToObjectMap(jokeUrlUriRelationsName, jokeURL, uri.toString());
        getBotContext().saveToObjectMap(uriJokeURLRelationsName, uri.toString(), jokeURL);
    }

    public void removeURIJokeURLRelation(URI uri) {
        String jokeURL = (String) getBotContext().loadFromObjectMap(uriJokeURLRelationsName, uri.toString());
        getBotContext().removeFromObjectMap(uriJokeURLRelationsName, uri.toString());
        getBotContext().removeFromObjectMap(jokeUrlUriRelationsName, jokeURL);
    }

    public String getJokeURLForURI(URI uri) {
        return (String) getBotContext().loadFromObjectMap(uriJokeURLRelationsName, uri.toString());
    }

    public String getAtomUriForJokeURL(String jokeURL) {
        return (String) getBotContext().loadFromObjectMap(jokeUrlUriRelationsName, jokeURL);
    }
}
