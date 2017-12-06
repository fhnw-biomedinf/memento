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
    private final String label;
    private final String toolTip;
    private final S state;

    public Memento(MementoId id, String label, String toolTip, S state) {
        this.id = id;
        this.label = label;
        this.toolTip = toolTip;
        this.state = state;
    }

    public MementoId getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getToolTip() {
        return toolTip;
    }

    public S getState() {
        return state;
    }

}