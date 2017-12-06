package ch.fhnw.ima.memento;

final class MementoTestUtil {

    private static final String DUMMY_STATE = "";

    static Originator<String> mockOriginator(String displayName) {
        return id -> new Memento<>(id, displayName, DUMMY_STATE);
    }

}
