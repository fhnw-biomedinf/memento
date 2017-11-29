package ch.fhnw.ima.memento.model;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class Memento {

    private final String displayName;
    private final ObservableList<MementoBranch> branches = FXCollections.observableArrayList();
    private final java.util.List<Listener> listeners = new ArrayList<>();

    public Memento(String displayName) {
        this.displayName = displayName;
        branches.addListener((ListChangeListener<MementoBranch>) c -> fireMementoChanged());
    }

    public MementoBranch appendInBranch(Memento memento) {
        MementoBranch branch = MementoBranch.of(memento);
        branch.addListener(b -> fireMementoChanged());
        branches.add(branch);
        return branch;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<MementoBranch> getBranches() {
        return new ArrayList<>(branches);
    }

    public boolean addListener(Listener listener) {
        return listeners.add(listener);
    }

    private void fireMementoChanged() {
        for (Listener listener : listeners) {
            listener.mementoChanged(this);
        }
    }

    public interface Listener {

        void mementoChanged(Memento memento);

    }

}