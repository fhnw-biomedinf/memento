package ch.fhnw.ima.memento;

/**
 * Captures application state in a {@link Memento}.
 * Loosely inspired by the classic <a href="https://en.wikipedia.org/wiki/Memento_pattern">Memento Pattern</a>.
 *
 * @param <S> State type of captured mementos
 * @author Rahel Lüthy
 */
public interface Originator<S> {

    Memento<S> captureMemento(MementoId id);

}
