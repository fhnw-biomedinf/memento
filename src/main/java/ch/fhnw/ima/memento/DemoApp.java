package ch.fhnw.ima.memento;

import ch.fhnw.ima.memento.model.Memento;
import ch.fhnw.ima.memento.model.MementoBranch;
import ch.fhnw.ima.memento.model.MementoModel;
import ch.fhnw.ima.memento.ui.MementoView;
import ch.fhnw.ima.memento.util.SolarizedColor;
import io.vavr.Function1;
import io.vavr.collection.Iterator;
import io.vavr.collection.Stream;
import javafx.application.Application;
import javafx.application.Platform;
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

import java.util.HashMap;
import java.util.Map;

public class DemoApp extends Application {

    private static final double WIDTH = 600;
    private static final double HEIGHT = 600;

    private static MementoModel createModel() {
        Memento one = new Memento("1");
        Memento two = new Memento("2");
        Memento three = new Memento("3");

        one.appendInBranch(new Memento("A-1.1"));

        two.appendInBranch(new Memento("A-2.1")).append(new Memento("A-2.2"));
        two.appendInBranch(new Memento("B-2.1")).append(new Memento("B-2.2"));

        three.appendInBranch(new Memento("A-3.1"));
        three.appendInBranch(new Memento("B-3.1")).append(new Memento("B-3.2"));

        MementoModel model = new MementoModel();
        model.getMasterBranch().appendAll(one, two, three);
        return model;
    }

    @Override
    public void start(Stage stage) {

        MementoModel model = createModel();
        Function1<MementoBranch, Color> colorProvider = createColorProvider();
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
        ReadOnlyObjectProperty<Memento> selectionModel = view.getSelectionModel();
        selectionModel.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                selectedMementoValueLabel.setText("–");
            } else {
                selectedMementoValueLabel.setText(newValue.getDisplayName());
            }
        });

        Button forkButton = new Button("Create New In Branch");
        forkButton.disableProperty().bind(selectionModel.isNull());
        forkButton.setOnAction(e -> selectionModel.get().appendInBranch(new Memento("*")));

        HBox box = new HBox(10, selectedMementoLabel, selectedMementoValueLabel);
        box.setAlignment(Pos.CENTER_LEFT);

        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10));
        borderPane.setCenter(box);
        borderPane.setRight(forkButton);
        
        return borderPane;
    }

    private Function1<MementoBranch, Color> createColorProvider() {
        Map<MementoBranch, Color> colorCache = new HashMap<>();
        Stream<Color> colors = Stream.of(SolarizedColor.values()).map(v -> v.getColor().desaturate().desaturate());
        Iterator<Color> colorIterator = colors.cycle().iterator();
        return branch -> colorCache.computeIfAbsent(branch, b -> colorIterator.next());
    }

}