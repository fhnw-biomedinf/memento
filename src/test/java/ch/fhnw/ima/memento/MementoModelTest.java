package ch.fhnw.ima.memento;

import io.vavr.collection.List;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static ch.fhnw.ima.memento.MementoTestUtil.DUMMY_TOOLTIP;
import static ch.fhnw.ima.memento.MementoTestUtil.mockOriginator;
import static org.junit.jupiter.api.Assertions.*;

class MementoModelTest {

    @Test
    void empty() {
        MementoModel<String> model = new MementoModel<>();
        assertNotNull(model.getMasterBranchId());
        assertTrue(model.getMementos(model.getMasterBranchId()).isEmpty());
    }

    @Test
    void getMementos() {
        MementoModel<Integer> model = new MementoModel<>();
        assertTrue(model.getMementos(model.getMasterBranchId()).isEmpty());

        MementoId one = model.appendToMasterBranch(() -> new Originator.Capture<>(new Memento<>(new MementoId.DefaultMementoId(), "1", DUMMY_TOOLTIP, 1), true));
        MementoId two = model.appendToMasterBranch(() -> new Originator.Capture<>(new Memento<>(new MementoId.DefaultMementoId(), "2", DUMMY_TOOLTIP, 2), true));

        List<Memento<Integer>> mementos = model.getMementos(model.getMasterBranchId()).flatMap(model::getMemento);

        assertEquals(2, mementos.length());

        assertEquals(1, mementos.get(0).getState().intValue());
        assertEquals(one, mementos.get(0).getId());

        assertEquals(2, mementos.get(1).getState().intValue());
        assertEquals(two, mementos.get(1).getId());

        mementos.forEach(m -> {
            Option<Memento<Integer>> mementoOption = model.getMemento(m.getId());
            assertTrue(mementoOption.isDefined());
            assertEquals(m.getLabel(), mementoOption.get().getLabel());
            assertEquals(m.getState(), mementoOption.get().getState());
        });

    }

    @Test
    void allMementosFlattened() {
        MementoModel<String> model = new MementoModel<>();

        model.appendToMasterBranch(mockOriginator("throwaway"));

        model.clear();

        MementoId one = model.appendToMasterBranch(mockOriginator("1"));
        model.appendToNewBranch(one, mockOriginator("2"));
        model.appendToMasterBranch(mockOriginator("3"));

        List<Memento<String>> mementos = model.getAllMementosFlattened().flatMap(model::getMemento);
        assertEquals("1", mementos.get(0).getLabel());
        assertEquals("2", mementos.get(1).getLabel());
        assertEquals("3", mementos.get(2).getLabel());
    }

    @Test
    void clear() {
        MementoModel<String> model = new MementoModel<>();

        model.appendToMasterBranch(mockOriginator("*"));
        model.appendToMasterBranch(mockOriginator("*"));
        MementoId mementoId = model.appendToMasterBranch(mockOriginator("*"));

        model.appendToNewBranch(mementoId, mockOriginator("*"));

        assertFalse(model.getMementos(model.getMasterBranchId()).isEmpty());
        assertFalse(model.getBranches(mementoId).isEmpty());

        model.clear();

        assertTrue(model.getMementos(model.getMasterBranchId()).isEmpty());
        assertTrue(model.getBranches(mementoId).isEmpty());
    }

    @Test
    void observability() {
        MementoModel<String> model = new MementoModel<>();

        AtomicInteger counterOne = new AtomicInteger();
        MementoModel.Listener listenerOne = mementoRef -> counterOne.incrementAndGet();

        AtomicInteger counterTwo = new AtomicInteger();
        MementoModel.Listener listenerTwo = mementoRef -> counterTwo.incrementAndGet();

        model.addListener(listenerOne);
        model.addListener(listenerTwo);

        MementoId mementoId = model.appendToMasterBranch(mockOriginator("*"));
        model.appendToNewBranch(mementoId, mockOriginator("*"));

        model.removeListener(listenerOne);

        model.appendToMasterBranch(mockOriginator("*"));

        assertEquals(2, counterOne.get());
        assertEquals(3, counterTwo.get());

    }

    @Test
    void replaceExisting() {
        MementoModel<String> model = new MementoModel<>();
        MementoId mementoId = model.appendToMasterBranch(mockOriginator("*"));

        Option<Memento<String>> mementoOption = model.getMemento(mementoId);
        assertTrue(mementoOption.isDefined());

        // Using the same Id will lead to an actual replacement
        MementoId sameId = mementoOption.get().getId();

        final String REPLACED_LABEL = "replacedLabel";
        boolean replacedExisting = model.replace(() -> new Originator.Capture<>(
                new Memento<>(sameId, REPLACED_LABEL, "replaceToolTip", "replacedState"), true)
        );
        assertTrue(replacedExisting);
        assertTrue(model.getMemento(mementoId).isDefined());
        assertEquals(REPLACED_LABEL, model.getMemento(mementoId).get().getLabel());
    }

    @Test
    void replaceNonExisting() {
        final String ORIGINAL_LABEL = "*";
        MementoModel<String> model = new MementoModel<>();
        MementoId mementoId = model.appendToMasterBranch(mockOriginator(ORIGINAL_LABEL));

        Option<Memento<String>> mementoOption = model.getMemento(mementoId);
        assertTrue(mementoOption.isDefined());

        // Using a new Id will NOT lead to a replacement
        MementoId.DefaultMementoId newId = new MementoId.DefaultMementoId();

        boolean replacedExisting = model.replace(() -> new Originator.Capture<>(
                new Memento<>(newId, "replacedLabel", "replaceToolTip", "replacedState"), true)
        );

        assertFalse(replacedExisting);
        // old one still exists...
        assertTrue(model.getMemento(mementoId).isDefined());
        // ... but with original rather than replaced values
        assertEquals(ORIGINAL_LABEL, model.getMemento(mementoId).get().getLabel());
    }

}