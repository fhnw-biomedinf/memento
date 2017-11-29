package ch.fhnw.ima.memento.model;

import io.vavr.collection.List;
import io.vavr.collection.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;

public class MementoBranch {

    private final ObservableList<Memento> mementos = FXCollections.observableArrayList();

    private final java.util.List<Listener> listeners = new ArrayList<>();

    public MementoBranch() {
        mementos.addListener((ListChangeListener<Memento>) c -> fireBranchChanged());
    }

    public static MementoBranch of(Memento memento) {
        MementoBranch mementoBranch = new MementoBranch();
        mementoBranch.append(memento);
        return mementoBranch;
    }

    public List<Memento> getMementos() {
        return List.ofAll(mementos);
    }

    public void append(Memento memento) {
        memento.addListener(m -> fireBranchChanged());
        mementos.add(memento);
    }

    public void appendAll(Memento... newMementos) {
        Stream.of(newMementos).forEach(this::append);
    }

    private void fireBranchChanged() {
        for (Listener listener : listeners) {
            listener.branchChanged(this);
        }
    }

    public boolean addListener(Listener listener) {
        return listeners.add(listener);
    }

    public interface Listener {

        void branchChanged(MementoBranch branch);

    }

}
