package ch.fhnw.ima.memento;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;

import java.util.Objects;
import java.util.UUID;

/**
 * Default implementation of a {@link MementoModel}.
 * Loosely inspired by the classic <a href="https://en.wikipedia.org/wiki/Memento_pattern">Memento Pattern</a>, in
 * which this class takes the role of the caretaker.
 *
 * @param <S> State type of captured mementos
 * @author Rahel Lüthy
 */
public final class CareTakingMementoModel<S> implements MementoModel<S> {

    private final MementoBranchId masterBranchId;

    private List<Listener> listeners = List.empty();
    private Map<MementoId, Memento<S>> mementos = HashMap.empty();
    private Map<MementoId, List<MementoBranchId>> branchesByMemento = HashMap.empty();
    private Map<MementoBranchId, List<MementoId>> mementosByBranch = HashMap.empty();

    @SuppressWarnings("WeakerAccess")
    public CareTakingMementoModel() {
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

    @Override
    public Option<Memento<S>> getMemento(MementoId mementoId) {
        return mementos.get(mementoId);
    }

    @Override
    public MementoBranchId getMasterBranchId() {
        return masterBranchId;
    }

    @Override
    public List<Memento<S>> getMementos(MementoBranchId branchId) {
        List<MementoId> mementos = mementosByBranch.get(branchId).getOrElse(List.empty());
        return mementos.flatMap(id -> this.mementos.get(id));
    }

    @Override
    public List<MementoBranchId> getBranches(MementoId mementoId) {
        return branchesByMemento.getOrElse(mementoId, List.empty());
    }

    @Override
    public MementoId save(Originator<S> originator) {
        return save(masterBranchId, originator);
    }

    @Override
    public MementoId save(MementoBranchId branchId, Originator<S> originator) {
        MementoId mementoId = createNewMementoId();
        Memento<S> memento = originator.captureMemento(mementoId);
        mementos = mementos.put(mementoId, memento);
        List<MementoId> existingMementoIds = mementosByBranch.getOrElse(branchId, List.empty());
        mementosByBranch = mementosByBranch.put(branchId, existingMementoIds.append(memento.getId()));
        fireModelChanged(new MementoRef(mementoId, branchId));
        return mementoId;
    }

    @Override
    public MementoRef saveForked(MementoId branchRoot, Originator<S> originator) {
        MementoBranchId branchId = createNewMementoBranchId();
        List<MementoBranchId> existingBranches = branchesByMemento.getOrElse(branchRoot, List.empty());
        branchesByMemento = branchesByMemento.put(branchRoot, existingBranches.append(branchId));
        MementoId mementoId = save(branchId, originator);
        return new MementoRef(mementoId, branchId);
    }

    @Override
    public void clear() {
        mementos = HashMap.empty();
        branchesByMemento = HashMap.empty();
        mementosByBranch = HashMap.empty();
    }

    @Override
    public int getRowHeight(int mementoIndex) {
        return getRowHeight(masterBranchId, mementoIndex);
    }

    @Override
    public int getRowHeight(MementoBranchId branchId, int mementoIndex) {
        List<Memento<S>> mementos = getMementos(branchId);
        if (mementoIndex >= mementos.size()) {
            return 1;
        } else {
            int ownRowHeight = getOwnRowHeight(mementos.get(mementoIndex).getId());
            if (mementoIndex == mementos.size() - 1) {
                return ownRowHeight;
            } else {
                return ownRowHeight + getRowHeight(branchId, mementoIndex + 1) - 1;
            }
        }
    }

    private int getOwnRowHeight(MementoId mementoId) {
        List<MementoBranchId> branches = getBranches(mementoId);
        if (branches.isEmpty()) {
            return 1;
        } else {
            int rowHeight = 0;
            for (MementoBranchId mementoBranch : getBranches(mementoId)) {
                rowHeight += getRowHeight(mementoBranch, 0);
            }
            return rowHeight + 1;
        }
    }

    @Override
    public void addListener(Listener listener) {
        listeners = listeners.append(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        listeners = listeners.remove(listener);
    }

    private void fireModelChanged(MementoRef mementoRef) {
        for (Listener listener : listeners) {
            listener.modelChanged(mementoRef);
        }
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