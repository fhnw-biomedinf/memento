package ch.fhnw.ima.memento;

/**
 * Captures application state in a {@link Memento}.
 * Loosely inspired by the classic <a href="https://en.wikipedia.org/wiki/Memento_pattern">Memento Pattern</a>.
 *
 * @param <S> Capture type of captured mementos
 * @author Rahel Lüthy
 */
public interface Originator<S> {

    Capture<S> createCapture();

    final class Capture<S> {

        private final Memento<S> memento;
        private final boolean shouldFireModelChanged;

        /**
         * Constructs a new state capture.
         *
         * @param memento the captured memento
         * @param shouldFireModelChanged whether listeners should be fired. A <code>false</code> flag allows to
         *                               suppress unnecessary update cycles.
         */
        public Capture(Memento<S> memento, boolean shouldFireModelChanged) {
            this.memento = memento;
            this.shouldFireModelChanged = shouldFireModelChanged;
        }

        public Memento<S> getMemento() {
            return memento;
        }

        public boolean isShouldFireModelChanged() {
            return shouldFireModelChanged;
        }

    }

}
