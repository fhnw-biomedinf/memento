package ch.fhnw.ima.memento.util;

import ch.fhnw.ima.memento.model.Memento;
import ch.fhnw.ima.memento.model.MementoBranch;

public final class MementoHeightCalculator {

    public static int getRowHeight(MementoBranch branch, int mementoIndex) {
        if (mementoIndex >= branch.getMementos().size()) {
            return 1;
        } else {
            int ownRowHeight = getOwnRowHeight(branch.getMementos().get(mementoIndex));
            if (mementoIndex == branch.getMementos().length() - 1) {
                return ownRowHeight;
            } else {
                return ownRowHeight + getRowHeight(branch, mementoIndex + 1) - 1;
            }
        }
    }

    private static int getOwnRowHeight(Memento memento) {
        if (memento.getBranches().isEmpty()) {
            return 1;
        } else {
            int rowHeight = 0;
            for (MementoBranch mementoBranch : memento.getBranches()) {
                rowHeight += getRowHeight(mementoBranch, 0);
            }
            return rowHeight + 1;
        }
    }

}
