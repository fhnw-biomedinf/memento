package ch.fhnw.ima.memento;

import io.vavr.collection.List;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class CareTakingMementoModelTest {

    private static final String DUMMY_STATE = "";

    private static Originator<String> mockOriginator(String displayName) {
        return id -> new Memento<>(id, displayName, DUMMY_STATE);
    }

    @Test
    void empty() {
        MementoModel<String> model = new CareTakingMementoModel<>();
        assertNotNull(model.getMasterBranchId());
        assertTrue(model.getMementos(model.getMasterBranchId()).isEmpty());
    }

    @Test
    void getMementos() {
        MementoModel<Integer> model = new CareTakingMementoModel<>();
        assertTrue(model.getMementos(model.getMasterBranchId()).isEmpty());

        MementoId one = model.save(id -> new Memento<>(id, "1", 1));
        MementoId two = model.save(id -> new Memento<>(id, "2", 2));

        List<Memento<Integer>> mementos = model.getMementos(model.getMasterBranchId());

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
        MementoModel<String> model = new CareTakingMementoModel<>();

        model.save(mockOriginator("*"));
        model.save(mockOriginator("*"));
        MementoId mementoId = model.save(mockOriginator("*"));

        model.saveForked(mementoId, mockOriginator("*"));

        assertFalse(model.getMementos(model.getMasterBranchId()).isEmpty());
        assertFalse(model.getBranches(mementoId).isEmpty());

        model.clear();

        assertTrue(model.getMementos(model.getMasterBranchId()).isEmpty());
        assertTrue(model.getBranches(mementoId).isEmpty());
    }

    @Test
    void observability() {
        MementoModel<String> model = new CareTakingMementoModel<>();

        AtomicInteger counterOne = new AtomicInteger();
        MementoModel.Listener listenerOne = mementoRef -> counterOne.incrementAndGet();

        AtomicInteger counterTwo = new AtomicInteger();
        MementoModel.Listener listenerTwo = mementoRef -> counterTwo.incrementAndGet();

        model.addListener(listenerOne);
        model.addListener(listenerTwo);

        MementoId mementoId = model.save(mockOriginator("*"));
        model.saveForked(mementoId, mockOriginator("*"));

        model.removeListener(listenerOne);

        model.save(mockOriginator("*"));

        assertEquals(2, counterOne.get());
        assertEquals(3, counterTwo.get());

    }

    @Test
    void rowHeightSingleton() {
        MementoModel<String> model = new CareTakingMementoModel<>();
        model.save(mockOriginator("1"));
        assertEquals(1, model.getRowHeight(0));
    }

    @Test
    void rowHeightMasterOnly() {
        MementoModel<String> model = new CareTakingMementoModel<>();
        model.save(mockOriginator("1"));
        model.save(mockOriginator("2"));
        assertEquals(1, model.getRowHeight(0));
        assertEquals(1, model.getRowHeight(1));
    }

    @Test
    void rowHeightOneBranchOffMaster() {
        MementoModel<String> model = new CareTakingMementoModel<>();
        MementoId one = model.save(mockOriginator("1"));
        MementoBranchId levelOneBranch = model.saveForked(one, mockOriginator("1.1")).getBranchId();
        assertEquals(2, model.getRowHeight(0));
        assertEquals(1, model.getRowHeight(levelOneBranch, 0));
    }

    @Test
    void rowHeightBranchOffBranchOffMaster() {

        // 1             <- master
        // └── 1.1       <- level 1
        // │   └── 1.1.1 <- level 2
        // └── 1.2       <- level 3

        MementoModel<String> model = new CareTakingMementoModel<>();

        MementoId one = model.save(mockOriginator("1"));

        MementoRef oneDotOneRef = model.saveForked(one, mockOriginator("1.1"));
        MementoBranchId levelOneBranch = oneDotOneRef.getBranchId();
        MementoId oneDotOne = oneDotOneRef.getMementoId();

        MementoBranchId levelTwoBranch = model.saveForked(oneDotOne, mockOriginator("1.1.1")).getBranchId();
        MementoBranchId levelThreeBranch = model.saveForked(one, mockOriginator("1.2")).getBranchId();

        assertEquals(4, model.getRowHeight(0));
        assertEquals(2, model.getRowHeight(levelOneBranch, 0));
        assertEquals(1, model.getRowHeight(levelTwoBranch, 0));
        assertEquals(1, model.getRowHeight(levelThreeBranch, 0));
    }

    @Test
    void rowHeight() {

        // 1 ––––––––––––––– 2 ––––––––––––––– 3
        // │                 │                 └── A 3.1
        // │                 │                 └── B 3.1 – B 3.2
        // │                 └── A 2.1 - A 2.2
        // │                 └── B 2.1 - B 2.2
        // └── A 1.1

        MementoModel<String> model = new CareTakingMementoModel<>();

        MementoId one = model.save(mockOriginator("1"));
        MementoId two = model.save(mockOriginator("2"));
        MementoId three = model.save(mockOriginator("3"));

        model.saveForked(one, mockOriginator("A 1.1"));

        MementoBranchId branch2A = model.saveForked(two, mockOriginator("A 2.1")).getBranchId();
        model.save(branch2A, mockOriginator("A 2.2"));

        MementoBranchId branch2B = model.saveForked(two, mockOriginator("B 2.1")).getBranchId();
        model.save(branch2B, mockOriginator("B 2.2"));

        model.saveForked(three, mockOriginator("A 3.1"));
        MementoBranchId branch3B = model.saveForked(three, mockOriginator("B 3.1")).getBranchId();
        model.save(branch3B, mockOriginator("B 3.2"));

        assertEquals(3, model.getRowHeight(2));
        assertEquals(5, model.getRowHeight(1));
        assertEquals(6, model.getRowHeight(0));

    }

}