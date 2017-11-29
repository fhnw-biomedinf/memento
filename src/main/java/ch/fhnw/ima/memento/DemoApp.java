package ch.fhnw.ima.memento;

import ch.fhnw.ima.memento.model.Memento;
import ch.fhnw.ima.memento.model.MementoBranch;
import ch.fhnw.ima.memento.model.MementoModel;
import ch.fhnw.ima.memento.ui.MementoView;
import ch.fhnw.ima.memento.util.SolarizedColor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DemoApp extends Application {

    private static final double WIDTH = 600;
    private static final double HEIGHT = 600;

    private Iterator<Color> colorIterator = createColorIterator();

    private static MementoBranch createEmptyMasterBranch() {
        return MementoBranch.of(new Memento("*"));
    }

    private static MementoBranch createMockMasterBranch() {
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
        return masterBranch;
    }

    private static Iterator<Color> createColorIterator() {
        List<Color> colors = Arrays.stream(SolarizedColor.values()).map(v -> v.getColor().desaturate().desaturate()).collect(Collectors.toList());
        IntStream infiniteStream = IntStream.iterate(0, i -> i + 1);
        return infiniteStream.mapToObj(i -> colors.get(i % colors.size())).iterator();
    }

    @Override
    public void start(Stage stage) {
        MementoModel model = new MementoModel(createEmptyMasterBranch());

        Function<MementoBranch, Color> colorProvider = createColorProvider();
        MementoView mementoView = new MementoView(model, colorProvider);

        Pane controlPanel = createControlPanel(mementoView);

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

    private Pane createControlPanel(MementoView view) {
        Label selectedMementoLabel = new Label("Selected: ");
        Label selectedMementoValueLabel = new Label("–");
        selectedMementoValueLabel.setPadding(new Insets(5));
        ReadOnlyObjectProperty<MementoView.MementoContext> selectionModel = view.getSelectionModel();
        selectionModel.addListener((observable, oldValue, newValue) -> selectedMementoValueLabel.setText(newValue.getMemento().getDisplayName()));

        Button appendButton = new Button("Append");
        appendButton.disableProperty().bind(Bindings.createBooleanBinding(() -> selectionModel.get().isNone() || !selectionModel.get().isTip(), selectionModel));
        appendButton.setOnAction(e -> selectionModel.get().getBranch().append(new Memento("*")));

        Button forkButton = new Button("Branch");
        forkButton.disableProperty().bind(Bindings.createBooleanBinding(() -> selectionModel.get().isNone(), selectionModel));
        forkButton.setOnAction(e -> selectionModel.get().getMemento().appendInBranch(new Memento("*")));

        Button mockButton = new Button("Mock");
        mockButton.setOnAction(e -> {
            colorIterator = createColorIterator();
            view.getModel().setMasterBranch(createMockMasterBranch());
        });

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> {
            colorIterator = createColorIterator();
            view.getModel().setMasterBranch(createEmptyMasterBranch());
        });

        HBox centerBox = new HBox(10, selectedMementoLabel, selectedMementoValueLabel);
        centerBox.setAlignment(Pos.CENTER_LEFT);

        HBox rightBox = new HBox(10, appendButton, forkButton, mockButton, clearButton);
        centerBox.setAlignment(Pos.CENTER_LEFT);

        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10));
        borderPane.setCenter(centerBox);
        borderPane.setRight(rightBox);

        return borderPane;
    }

    private Function<MementoBranch, Color> createColorProvider() {
        Map<MementoBranch, Color> colorCache = new HashMap<>();
        return branch -> colorCache.computeIfAbsent(branch, b -> colorIterator.next());
    }

}