package ch.fhnw.ima.memento;

import io.vavr.Function1;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Iterator;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates usage of {@link MementoView}.
 *
 * @author Rahel Lüthy
 */
public class App extends Application {

    private static final double WIDTH = 600;
    private static final double HEIGHT = 600;

    private final AtomicInteger counter = new AtomicInteger(0);
    private final Originator<Integer> originator = id -> new Memento<>(id, String.valueOf(counter.incrementAndGet()), String.valueOf(counter.get()), counter.get());
    private final ColorHandler colorHandler = new ColorHandler();

    @Override
    public void start(Stage stage) {
        MementoModel<Integer> caretaker = new MementoModel<>();
        MementoView<Integer> mementoView = new MementoView<>(caretaker, colorHandler);
        Pane controlPanel = createControlPanel(caretaker, mementoView.getSelectionModel(), mementoView.appendAllowedProperty());

        // Create an initial Memento
        caretaker.appendToMasterBranch(originator);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(5));
        root.setCenter(new ScrollPane(mementoView));
        root.setBottom(controlPanel);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Memento");
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);

        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        stage.show();
    }

    private Pane createControlPanel(MementoModel<Integer> model, ReadOnlyObjectProperty<Option<MementoRef>> selectionModel, ReadOnlyBooleanProperty appendAllowedProperty) {
        Label selectedMementoLabel = new Label("Selected:");
        Label selectedMementoValueLabel = new Label("–");
        selectionModel.addListener((observable, oldValue, newValue) -> {
            selectedMementoValueLabel.setText("–");
            if (!newValue.isEmpty()) {
                Option<Memento<Integer>> mementoOption = model.getMemento(newValue.get().getMementoId());
                mementoOption.forEach(memento -> selectedMementoValueLabel.setText(memento.getLabel()));
            }
        });

        Button appendButton = new Button("Append");
        appendButton.disableProperty().bind(appendAllowedProperty.not());
        appendButton.setOnAction(e -> {
            MementoBranchId branchId = selectionModel.get().get().getBranchId();
            model.appendToBranch(branchId, originator);
        });

        Button forkButton = new Button("Branch");
        forkButton.disableProperty().bind(Bindings.createBooleanBinding(() -> selectionModel.get().isEmpty(), selectionModel));
        forkButton.setOnAction(e -> {
            MementoId mementoId = selectionModel.get().get().getMementoId();
            model.appendToNewBranch(mementoId, originator);
        });

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> {
            colorHandler.reset();
            model.clear();
            counter.set(0);
            model.appendToMasterBranch(originator);
        });

        HBox centerBox = new HBox(5, selectedMementoLabel, selectedMementoValueLabel);
        centerBox.setAlignment(Pos.CENTER_LEFT);

        HBox rightBox = new HBox(10, appendButton, forkButton, clearButton);
        centerBox.setAlignment(Pos.CENTER_LEFT);

        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10));
        borderPane.setCenter(centerBox);
        borderPane.setRight(rightBox);

        return borderPane;
    }

    private enum SolarizedColor {

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

    private static class ColorHandler implements Function1<MementoBranchId, Color> {

        private Map<MementoBranchId, Color> colorCache = HashMap.empty();
        private Iterator<Color> colorIterator = createColorIterator();

        private static Iterator<Color> createColorIterator() {
            Stream<Color> colors = Stream.of(SolarizedColor.values()).map(v -> v.getColor().desaturate().desaturate());
            return colors.cycle().iterator();
        }

        void reset() {
            colorCache = HashMap.empty();
            colorIterator = createColorIterator();
        }

        @Override
        public Color apply(MementoBranchId branchId) {
            Tuple2<Color, ? extends Map<MementoBranchId, Color>> result = colorCache.computeIfAbsent(branchId, b -> colorIterator.next());
            colorCache = result._2;
            return result._1;
        }

    }

}