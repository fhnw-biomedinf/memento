package ch.fhnw.ima.memento;

final class MementoTestUtil {

    private static final String DUMMY_STATE = "dummy-state";
    public static final String DUMMY_TOOLTIP = "dummy-tooltip";

    static Originator<String> mockOriginator(String label) {
        return id -> new Memento<>(id, label, DUMMY_TOOLTIP, DUMMY_STATE);
    }

}
