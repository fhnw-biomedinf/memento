package ch.fhnw.ima.memento.util;

import ch.fhnw.ima.memento.model.Memento;
import ch.fhnw.ima.memento.model.MementoBranch;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MementoHeightCalculatorTest {

    @Test
    void rowHeightSingleton() {
        MementoBranch branch = new MementoBranch();
        branch.append(new Memento("1"));
        assertEquals(1, MementoHeightCalculator.getRowHeight(branch, 0));
    }

    @Test
    void rowHeightMasterOnly() {
        MementoBranch branch = new MementoBranch();
        branch.append(new Memento("1"));
        branch.append(new Memento("2"));
        assertEquals(1, MementoHeightCalculator.getRowHeight(branch, 0));
        assertEquals(1, MementoHeightCalculator.getRowHeight(branch, 1));
    }

    @Test
    void rowHeightOneBranchOffMaster() {
        Memento memento = new Memento("1");
        memento.appendInBranch(new Memento("A-1.1"));
        MementoBranch branch = MementoBranch.of(memento);
        assertEquals(2, MementoHeightCalculator.getRowHeight(branch, 0));
    }

    @Test
    void rowHeightBranchOffBranchOffMaster() {

        // 1             <- level 0
        // └── 1.1       <- level 1
        // │   └── 1.1.1 <- level 2
        // └── 1.2       <- level 3

        Memento one = new Memento("1");
        Memento oneDotOne = new Memento("1.1");
        Memento oneDotOneDotOne = new Memento("1.1.1");
        Memento oneDotTwo = new Memento("1.2");

        MementoBranch levelZeroBranch = MementoBranch.of(one);
        MementoBranch levelOneBranch = one.appendInBranch(oneDotOne);
        MementoBranch levelTwoBranch = one.appendInBranch(oneDotTwo);
        oneDotOne.appendInBranch(oneDotOneDotOne);

        assertEquals(4, MementoHeightCalculator.getRowHeight(levelZeroBranch, 0));
        assertEquals(2, MementoHeightCalculator.getRowHeight(levelOneBranch, 0));
        assertEquals(1, MementoHeightCalculator.getRowHeight(levelTwoBranch, 0));
    }

    @Test
    void rowHeight() {

        Memento one = new Memento("1");
        Memento two = new Memento("2");
        Memento three = new Memento("3");

        one.appendInBranch(new Memento("A-1.1"));

        two.appendInBranch(new Memento("A-2.1")).append(new Memento("A-2.2"));
        two.appendInBranch(new Memento("B-2.1")).append(new Memento("B-2.2"));

        three.appendInBranch(new Memento("A-3.1"));
        three.appendInBranch(new Memento("B-3.1")).append(new Memento("B-3.2"));

        MementoBranch masterBranch = new MementoBranch();
        masterBranch.appendAll(one, two, three);

        assertEquals(3, MementoHeightCalculator.getRowHeight(masterBranch, 2));
        assertEquals(5, MementoHeightCalculator.getRowHeight(masterBranch, 1));
        assertEquals(6, MementoHeightCalculator.getRowHeight(masterBranch, 0));

    }

}