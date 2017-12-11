package ch.fhnw.ima.memento;

import io.vavr.collection.HashMap;
import io.vavr.collection.LinkedHashMap;
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
 * Mementos are created by an {@link Originator}, who knows how to capture application state.
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
    private Map<MementoId, Memento<S>> mementos = LinkedHashMap.empty();
    private Map<MementoId, List<MementoBranchId>> branchesByMemento = HashMap.empty();
    private Map<MementoBranchId, List<MementoId>> mementosByBranch = HashMap.empty();

    @SuppressWarnings("WeakerAccess")
    public MementoModel() {
        this.masterBranchId = new MementoBranchIdImpl();
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

    public List<MementoId> getAllMementosFlattened() {
        return mementos.keySet().toList();
    }

    public List<MementoBranchId> getBranches(MementoId mementoId) {
        return branchesByMemento.getOrElse(mementoId, List.empty());
    }

    public MementoId appendToMasterBranch(Originator<S> originator) {
        return appendToBranch(masterBranchId, originator);
    }

    public MementoId appendToBranch(MementoBranchId branchId, Originator<S> originator) {
        Originator.Capture<S> result = originator.createCapture();
        Memento<S> memento = result.getMemento();
        MementoId mementoId = memento.getId();
        mementos = mementos.put(mementoId, memento);
        List<MementoId> existingMementoIds = mementosByBranch.getOrElse(branchId, List.empty());
        mementosByBranch = mementosByBranch.put(branchId, existingMementoIds.append(memento.getId()));
        if (result.isShouldFireModelChanged()) {
            fireModelChanged(new MementoRef(mementoId, branchId));
        }
        return mementoId;
    }

    public MementoRef appendToNewBranch(MementoId branchRoot, Originator<S> originator) {
        MementoBranchId branchId = new MementoBranchIdImpl();
        List<MementoBranchId> existingBranches = branchesByMemento.getOrElse(branchRoot, List.empty());
        branchesByMemento = branchesByMemento.put(branchRoot, existingBranches.append(branchId));
        MementoId mementoId = appendToBranch(branchId, originator);
        return new MementoRef(mementoId, branchId);
    }

    public void clear() {
        mementos = LinkedHashMap.empty();
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

    private static final class MementoBranchIdImpl implements MementoBranchId {

        private final UUID value;

        private MementoBranchIdImpl() {
            this.value = UUID.randomUUID();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MementoBranchIdImpl mementoMementoBranchIdImpl = (MementoBranchIdImpl) o;
            return Objects.equals(value, mementoMementoBranchIdImpl.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "MementoBranchId <" + value + ">";
        }

    }

}