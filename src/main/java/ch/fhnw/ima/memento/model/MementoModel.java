package ch.fhnw.ima.memento.model;

import java.util.ArrayList;
import java.util.List;

public class MementoModel {

    private final MementoBranch masterBranch = new MementoBranch();

    private final List<Listener> listeners = new ArrayList<>();

    public MementoModel() {
        masterBranch.addListener(branch -> fireModelChanged());
    }

    public MementoBranch getMasterBranch() {
        return masterBranch;
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
