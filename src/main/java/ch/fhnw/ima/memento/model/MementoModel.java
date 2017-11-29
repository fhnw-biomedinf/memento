package ch.fhnw.ima.memento.model;

import java.util.ArrayList;
import java.util.List;

public class MementoModel {

    private MementoBranch masterBranch;
    private final MementoBranch.Listener masterBranchListener = branch -> fireModelChanged();
    private final List<Listener> listeners = new ArrayList<>();

    public MementoModel(MementoBranch masterBranch) {
        this.masterBranch = masterBranch;
        masterBranch.addListener(masterBranchListener);
    }

    public MementoBranch getMasterBranch() {
        return masterBranch;
    }

    public void setMasterBranch(MementoBranch newMasterBranch) {
        this.masterBranch = newMasterBranch;
        listeners.forEach(l -> {
            masterBranch.removeListener(masterBranchListener);
            newMasterBranch.addListener(masterBranchListener);
        });
        fireModelChanged();
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
