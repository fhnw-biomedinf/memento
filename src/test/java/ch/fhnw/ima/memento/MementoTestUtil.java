package ch.fhnw.ima.memento;

final class MementoTestUtil {

    public static final String DUMMY_TOOLTIP = "dummy-tooltip";
    private static final String DUMMY_STATE = "dummy-state";

    static Originator<String> mockOriginator(String label) {
        return () -> new Originator.Capture<>(new Memento<>(new MementoId.DefaultMementoId(), label, DUMMY_TOOLTIP, DUMMY_STATE), true);
    }

}
