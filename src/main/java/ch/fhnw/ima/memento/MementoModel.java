package ch.fhnw.ima.memento;

import io.vavr.collection.List;
import io.vavr.control.Option;

public interface MementoModel<S> {

    Option<Memento<S>> getMemento(MementoId mementoId);

    MementoBranchId getMasterBranchId();

    List<Memento<S>> getMementos(MementoBranchId branchId);

    List<MementoBranchId> getBranches(MementoId mementoId);

    MementoId save(Originator<S> originator);

    MementoId save(MementoBranchId branchId, Originator<S> originator);

    MementoRef saveForked(MementoId branchRoot, Originator<S> originator);

    void clear();

    int getRowHeight(int mementoIndex);

    int getRowHeight(MementoBranchId branchId, int mementoIndex);

    void addListener(Listener listener);

    interface Listener {

        void modelChanged(MementoRef mementoRef);

    }

}
