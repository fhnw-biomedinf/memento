package ch.fhnw.ima.memento;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.fhnw.ima.memento.MementoTestUtil.mockOriginator;
import static org.junit.jupiter.api.Assertions.*;

class RowHeightCalculatorTest {

    private MementoModel<String> model;
    private RowHeightCalculator<String> calculator;

    @BeforeEach
    void beforeEach() {
        model = new MementoModel<>();
        calculator = new RowHeightCalculator<>(model);
    }

    @Test
    void rowHeightSingleton() {
        model.appendToMasterBranch(mockOriginator("1"));
        assertEquals(1, calculator.calcRowHeight(0));
    }

    @Test
    void rowHeightMasterOnly() {
        model.appendToMasterBranch(mockOriginator("1"));
        model.appendToMasterBranch(mockOriginator("2"));
        assertEquals(1, calculator.calcRowHeight(0));
        assertEquals(1, calculator.calcRowHeight(1));
    }

    @Test
    void rowHeightOneBranchOffMaster() {
        MementoId one = model.appendToMasterBranch(mockOriginator("1"));
        MementoBranchId levelOneBranch = model.appendToNewBranch(one, mockOriginator("1.1")).getBranchId();
        assertEquals(2, calculator.calcRowHeight(0));
        assertEquals(1, calculator.calcRowHeight(levelOneBranch, 0));
    }

    @Test
    void rowHeightBranchOffBranchOffMaster() {

        // 1             <- master
        // └── 1.1       <- level 1
        // │   └── 1.1.1 <- level 2
        // └── 1.2       <- level 3

        MementoId one = model.appendToMasterBranch(mockOriginator("1"));

        MementoRef oneDotOneRef = model.appendToNewBranch(one, mockOriginator("1.1"));
        MementoBranchId levelOneBranch = oneDotOneRef.getBranchId();
        MementoId oneDotOne = oneDotOneRef.getMementoId();

        MementoBranchId levelTwoBranch = model.appendToNewBranch(oneDotOne, mockOriginator("1.1.1")).getBranchId();
        MementoBranchId levelThreeBranch = model.appendToNewBranch(one, mockOriginator("1.2")).getBranchId();

        assertEquals(4, calculator.calcRowHeight(0));
        assertEquals(2, calculator.calcRowHeight(levelOneBranch, 0));
        assertEquals(1, calculator.calcRowHeight(levelTwoBranch, 0));
        assertEquals(1, calculator.calcRowHeight(levelThreeBranch, 0));
    }

    @Test
    void rowHeight() {

        // 1 ––––––––––––––– 2 ––––––––––––––– 3
        // │                 │                 └── A 3.1
        // │                 │                 └── B 3.1 – B 3.2
        // │                 └── A 2.1 - A 2.2
        // │                 └── B 2.1 - B 2.2
        // └── A 1.1

        MementoId one = model.appendToMasterBranch(mockOriginator("1"));
        MementoId two = model.appendToMasterBranch(mockOriginator("2"));
        MementoId three = model.appendToMasterBranch(mockOriginator("3"));

        model.appendToNewBranch(one, mockOriginator("A 1.1"));

        MementoBranchId branch2A = model.appendToNewBranch(two, mockOriginator("A 2.1")).getBranchId();
        model.appendToBranch(branch2A, mockOriginator("A 2.2"));

        MementoBranchId branch2B = model.appendToNewBranch(two, mockOriginator("B 2.1")).getBranchId();
        model.appendToBranch(branch2B, mockOriginator("B 2.2"));

        model.appendToNewBranch(three, mockOriginator("A 3.1"));
        MementoBranchId branch3B = model.appendToNewBranch(three, mockOriginator("B 3.1")).getBranchId();
        model.appendToBranch(branch3B, mockOriginator("B 3.2"));

        assertEquals(3, calculator.calcRowHeight(2));
        assertEquals(5, calculator.calcRowHeight(1));
        assertEquals(6, calculator.calcRowHeight(0));

    }

}