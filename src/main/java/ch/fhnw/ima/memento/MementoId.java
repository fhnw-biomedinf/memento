package ch.fhnw.ima.memento;

import java.util.Objects;
import java.util.UUID;

/**
 * Uniquely identifies a memento.
 *
 * @author Rahel Lüthy
 */
public interface MementoId {

    // Used to check equality

    /**
     * A {@link MementoId} implementation backed by a {@link UUID}.
     */
    final class DefaultMementoId implements MementoId {

        private final UUID value;

        public DefaultMementoId() {
            this.value = UUID.randomUUID();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DefaultMementoId aDefault = (DefaultMementoId) o;
            return Objects.equals(value, aDefault.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " <" + value + ">";
        }

    }

}