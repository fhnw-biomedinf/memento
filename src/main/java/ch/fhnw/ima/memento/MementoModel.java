package ch.fhnw.ima.memento;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;

import java.util.Objects;
import java.util.UUID;

/**
 * Takes care of a {@link Memento} tree.
 * <p>
 * A model starts out with an (empty) master branch, to which mementos can be appended. Either they are appended to
 * an existing branch, or a new branch is forked-off, to which the memento is then appended.
 * <p>
 * Mementos are created by an {@link Originator}, who knows how to capture application state. Because all bookkeeping
 * relies on unique ids, this model is responsible for the creation of new ids (which it passes on to the originator).
 * <p>
 * Loosely inspired by the classic <a href="https://en.wikipedia.org/wiki/Memento_pattern">Memento Pattern</a>, in
 * which this class takes the role of the caretaker.
 *
 * @param <S> State type of captured mementos
 * @author Rahel Lüthy
 */
public final class MementoModel<S> {

    private final MementoBranchId masterBranchId;

    private List<Listener> listeners = List.empty();
    private Map<MementoId, Memento<S>> mementos = HashMap.empty();
    private Map<MementoId, List<MementoBranchId>> branchesByMemento = HashMap.empty();
    private Map<MementoBranchId, List<MementoId>> mementosByBranch = HashMap.empty();

    @SuppressWarnings("WeakerAccess")
    public MementoModel() {
        this.masterBranchId = createNewMementoBranchId();
    }

    private static MementoBranchId createNewMementoBranchId() {
        return createNewId(MementoBranchId.class.getSimpleName());
    }

    private static MementoId createNewMementoId() {
        return createNewId(MementoId.class.getSimpleName());
    }

    private static Id createNewId(String displayName) {
        return new Id(UUID.randomUUID(), displayName);
    }

    public Option<Memento<S>> getMemento(MementoId mementoId) {
        return mementos.get(mementoId);
    }

    public MementoBranchId getMasterBranchId() {
        return masterBranchId;
    }

    public List<MementoId> getMementos(MementoBranchId branchId) {
        return mementosByBranch.get(branchId).getOrElse(List.empty());
    }

    public List<MementoBranchId> getBranches(MementoId mementoId) {
        return branchesByMemento.getOrElse(mementoId, List.empty());
    }

    public MementoId appendToMasterBranch(Originator<S> originator) {
        return appendToBranch(masterBranchId, originator);
    }

    public MementoId appendToBranch(MementoBranchId branchId, Originator<S> originator) {
        MementoId mementoId = createNewMementoId();
        Memento<S> memento = originator.captureMemento(mementoId);
        mementos = mementos.put(mementoId, memento);
        List<MementoId> existingMementoIds = mementosByBranch.getOrElse(branchId, List.empty());
        mementosByBranch = mementosByBranch.put(branchId, existingMementoIds.append(memento.getId()));
        fireModelChanged(new MementoRef(mementoId, branchId));
        return mementoId;
    }

    public MementoRef appendToNewBranch(MementoId branchRoot, Originator<S> originator) {
        MementoBranchId branchId = createNewMementoBranchId();
        List<MementoBranchId> existingBranches = branchesByMemento.getOrElse(branchRoot, List.empty());
        branchesByMemento = branchesByMemento.put(branchRoot, existingBranches.append(branchId));
        MementoId mementoId = appendToBranch(branchId, originator);
        return new MementoRef(mementoId, branchId);
    }

    public void clear() {
        mementos = HashMap.empty();
        branchesByMemento = HashMap.empty();
        mementosByBranch = HashMap.empty();
    }

    public void addListener(Listener listener) {
        listeners = listeners.append(listener);
    }

    public void removeListener(Listener listener) {
        listeners = listeners.remove(listener);
    }

    private void fireModelChanged(MementoRef mementoRef) {
        for (Listener listener : listeners) {
            listener.modelChanged(mementoRef);
        }
    }

    interface Listener {

        void modelChanged(MementoRef mementoRef);

    }

    private static final class Id implements MementoId, MementoBranchId {

        private final UUID value;
        private final String displayName;

        private Id(UUID value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Id mementoId = (Id) o;
            return Objects.equals(value, mementoId.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return displayName + "<" + value + ">";
        }

    }

}