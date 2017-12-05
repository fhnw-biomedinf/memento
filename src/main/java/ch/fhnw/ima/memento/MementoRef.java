package ch.fhnw.ima.memento;

import java.util.Objects;

/**
 * An immutable pair of {@link MementoId}/{@link MementoBranchId}.
 *
 * @author Rahel Lüthy
 */
public final class MementoRef {

    private final MementoId mementoId;
    private final MementoBranchId branchId;

    @SuppressWarnings("WeakerAccess")
    public MementoRef(MementoId mementoId, MementoBranchId branchId) {
        this.mementoId = mementoId;
        this.branchId = branchId;
    }

    public MementoId getMementoId() {
        return mementoId;
    }

    public MementoBranchId getBranchId() {
        return branchId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MementoRef that = (MementoRef) o;
        return Objects.equals(mementoId, that.mementoId) &&
                Objects.equals(branchId, that.branchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mementoId, branchId);
    }

}
