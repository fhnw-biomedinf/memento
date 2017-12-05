package ch.fhnw.ima.memento.util;

import ch.fhnw.ima.memento.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CareTakingMementoModelTest {

    private static final String DUMMY_STATE = "";

    private static Originator<String> originator(String displayName) {
        return id -> new Memento<>(id, displayName, DUMMY_STATE);
    }

    @Test
    void rowHeightSingleton() {
        MementoModel<String> model = new CareTakingMementoModel<>();
        model.save(originator("1"));
        assertEquals(1, model.getRowHeight(0));
    }

    @Test
    void rowHeightMasterOnly() {
        MementoModel<String> model = new CareTakingMementoModel<>();
        model.save(originator("1"));
        model.save(originator("2"));
        assertEquals(1, model.getRowHeight(0));
        assertEquals(1, model.getRowHeight(1));
    }

    @Test
    void rowHeightOneBranchOffMaster() {
        MementoModel<String> model = new CareTakingMementoModel<>();
        MementoId one = model.save(originator("1"));
        MementoBranchId levelOneBranch = model.saveForked(one, originator("1.1")).getBranchId();
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

        MementoId one = model.save(originator("1"));

        MementoRef oneDotOneRef = model.saveForked(one, originator("1.1"));
        MementoBranchId levelOneBranch = oneDotOneRef.getBranchId();
        MementoId oneDotOne = oneDotOneRef.getMementoId();

        MementoBranchId levelTwoBranch = model.saveForked(oneDotOne, originator("1.1.1")).getBranchId();
        MementoBranchId levelThreeBranch = model.saveForked(one, originator("1.2")).getBranchId();

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

        MementoId one = model.save(originator("1"));
        MementoId two = model.save(originator("2"));
        MementoId three = model.save(originator("3"));

        model.saveForked(one, originator("A 1.1"));

        MementoBranchId branch2A = model.saveForked(two, originator("A 2.1")).getBranchId();
        model.save(branch2A, originator("A 2.2"));

        MementoBranchId branch2B = model.saveForked(two, originator("B 2.1")).getBranchId();
        model.save(branch2B, originator("B 2.2"));

        model.saveForked(three, originator("A 3.1"));
        MementoBranchId branch3B = model.saveForked(three, originator("B 3.1")).getBranchId();
        model.save(branch3B, originator("B 3.2"));

        assertEquals(3, model.getRowHeight(2));
        assertEquals(5, model.getRowHeight(1));
        assertEquals(6, model.getRowHeight(0));

    }

}