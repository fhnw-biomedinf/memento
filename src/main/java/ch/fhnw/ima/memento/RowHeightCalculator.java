package ch.fhnw.ima.memento;

import io.vavr.collection.List;

/**
 * Internal helper class to calculate row heights of memento (sub)trees.
 * <p>
 * Suppose we have the following tree:
 * <p>
 * <pre>
 * {@code
 *
 * 1 ––––––––––––––– 2 ––––––––––––––– 3
 * │                 │                 └── A 3.1
 * │                 └── A 2.1 - A 2.2
 * │                 └── B 2.1 - B 2.2
 * └── A 1.1
 *
 * }
 * </pre>
 * <p>
 * Calculation of row height for node 2 (index 1):
 * <ul>
 * <li>First, calculate how many rows the component would need just on its own
 * i.e. if it didn't need to encompass node 3 ("own row height"). In our example, the result is <b>3</b>.</li>
 * <li>Then calculate the row height of node 3 by recursively calling <code>calcRowHeight(index + 1)</code>. For
 * node 3, the result is <b>2</b>.</li>
 * <li>To calculate the total row height of node 2, the two results are added and corrected by -1 (because we
 * only need to count the base branch once).
 * </ul>
 *
 * @author Rahel Lüthy
 */
final class RowHeightCalculator<S> {

    private final MementoModel<S> model;

    RowHeightCalculator(MementoModel<S> model) {
        this.model = model;
    }

    int calcRowHeight(int mementoIndex) {
        return calcRowHeight(model.getMasterBranchId(), mementoIndex);
    }

    int calcRowHeight(MementoBranchId branchId, int mementoIndex) {
        List<Memento<S>> mementos = model.getMementos(branchId).flatMap(model::getMemento);
        if (mementoIndex >= mementos.size()) {
            return 1;
        } else {
            int ownRowHeight = calcOwnRowHeight(mementos.get(mementoIndex).getId());
            if (mementoIndex == mementos.size() - 1) {
                return ownRowHeight;
            } else {
                return ownRowHeight + calcRowHeight(branchId, mementoIndex + 1) - 1; // -1 only count base branch once
            }
        }
    }

    private int calcOwnRowHeight(MementoId mementoId) {
        List<MementoBranchId> branches = model.getBranches(mementoId);
        if (branches.isEmpty()) {
            return 1;
        } else {
            int rowHeight = 0;
            for (MementoBranchId mementoBranch : model.getBranches(mementoId)) {
                rowHeight += calcRowHeight(mementoBranch, 0);
            }
            return rowHeight + 1; // + 1 for base branch
        }
    }

}
