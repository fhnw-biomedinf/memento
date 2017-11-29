package ch.fhnw.ima.memento.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;

public class MementoModel {

    private static final MementoBranch EMPTY = new MementoBranch();

    private final ObjectProperty<MementoBranch> masterBranchProperty = new SimpleObjectProperty<>();
    private final List<Listener> listeners = new ArrayList<>();
    private final MementoBranch.Listener masterBranchListener = branch -> fireModelChanged();

    public MementoModel(MementoBranch masterBranch) {
        this.masterBranchProperty.addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.removeListener(masterBranchListener);
            }
            if (newValue != null) {
                newValue.addListener(masterBranchListener);
            }
            fireModelChanged();
        });
        setMasterBranch(masterBranch);
    }

    private static MementoBranch nullSafe(MementoBranch masterBranch) {
        return masterBranch == null ? EMPTY : masterBranch;
    }

    public MementoBranch getMasterBranch() {
        return masterBranchProperty.get();
    }

    public void setMasterBranch(MementoBranch masterBranch) {
        this.masterBranchProperty.set(nullSafe(masterBranch));
    }

    public boolean addListener(Listener listener) {
        return listeners.add(listener);
    }

    private void fireModelChanged() {
        for (Listener listener : listeners) {
            listener.modelChanged();
        }
    }

    public interface Listener {

        void modelChanged();

    }

}
