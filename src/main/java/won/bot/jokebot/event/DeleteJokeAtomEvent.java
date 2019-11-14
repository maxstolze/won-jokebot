package won.bot.jokebot.event;

import java.net.URI;

import won.bot.framework.eventbot.event.BaseEvent;

public class DeleteJokeAtomEvent extends BaseEvent {
    private final URI atomUri;

    public DeleteJokeAtomEvent(URI atomUri) {
        this.atomUri = atomUri;
    }

    public URI getAtomUriToDelete() {
        return atomUri;
    }
}