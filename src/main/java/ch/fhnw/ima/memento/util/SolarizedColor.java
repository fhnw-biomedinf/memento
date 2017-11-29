package ch.fhnw.ima.memento.util;

import javafx.scene.paint.Color;

public enum SolarizedColor {

    YELLOW(Color.web("#b58900")),
    ORANGE(Color.web("#cb4b16")),
    RED(Color.web("#dc322f")),
    MAGENTA(Color.web("#d33682")),
    VIOLET(Color.web("#6c71c4")),
    BLUE(Color.web("#268bd2")),
    CYAN(Color.web("#2aa198")),
    GREEN(Color.web("#859900"));

    private final Color color;

    SolarizedColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

}
