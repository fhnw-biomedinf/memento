package ch.fhnw.ima.memento;

import io.vavr.collection.List;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

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

        MementoId one = model.appendToMasterBranch(id -> new Memento<>(id, "1", 1));
        MementoId two = model.appendToMasterBranch(id -> new Memento<>(id, "2", 2));

        List<Memento<Integer>> mementos = model.getMementos(model.getMasterBranchId()).flatMap(model::getMemento);

        assertEquals(2, mementos.length());

        assertEquals(1, mementos.get(0).getState().intValue());
        assertEquals(one, mementos.get(0).getId());

        assertEquals(2, mementos.get(1).getState().intValue());
        assertEquals(two, mementos.get(1).getId());

        mementos.forEach(m -> {
            Option<Memento<Integer>> mementoOption = model.getMemento(m.getId());
            assertTrue(mementoOption.isDefined());
            assertEquals(m.getDisplayName(), mementoOption.get().getDisplayName());
            assertEquals(m.getState(), mementoOption.get().getState());
        });

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

}