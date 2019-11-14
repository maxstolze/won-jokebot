package won.bot.jokebot.event;

import won.bot.framework.eventbot.event.BaseEvent;
import won.bot.jokebot.api.JokeBotsApi;
import won.bot.jokebot.api.model.ChuckNorrisJoke;

/**
 * Created by ms on 14.11.2019.
 */
public class CreateAtomFromJokeEvent extends BaseEvent {
    private final ChuckNorrisJoke chuckNorrisJoke;
    private final JokeBotsApi jokeBotsApi;

    public CreateAtomFromJokeEvent(ChuckNorrisJoke chuckNorrisJoke, JokeBotsApi jokeBotsApi) {
        this.chuckNorrisJoke = chuckNorrisJoke;
        this.jokeBotsApi = jokeBotsApi;
    }

    public ChuckNorrisJoke getJoke() {
        return chuckNorrisJoke;
    }

    public JokeBotsApi getJokeBotsApi() {
        return jokeBotsApi;
    }
}
