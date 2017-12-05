package ch.fhnw.ima.memento;

/**
 * An immutable snapshot of state.
 * Inspired by the classic <a href="https://en.wikipedia.org/wiki/Memento_pattern">Memento Pattern</a>.
 *
 * @param <S> State type of this memento
 * @author Rahel Lüthy
 */
public final class Memento<S> {

    private final MementoId id;
    private final String displayName;
    private final S state;

    public Memento(MementoId id, String displayName, S state) {
        this.id = id;
        this.displayName = displayName;
        this.state = state;
    }

    public MementoId getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public S getState() {
        return state;
    }

}