package ch.fhnw.ima.memento.ui;

import ch.fhnw.ima.memento.model.Memento;
import ch.fhnw.ima.memento.model.MementoBranch;
import ch.fhnw.ima.memento.model.MementoModel;
import ch.fhnw.ima.memento.util.MementoHeightCalculator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

import java.util.function.Function;

public class MementoView extends Region {

    private static final Color CIRCLE_FILL_COLOR = new Color(0.9019608f, 0.9019608f, 0.98039216f, 0.7);
    private static final Color CIRCLE_STROKE_COLOR_SELECTED = Color.BLACK;
    private static final Color CIRCLE_STROKE_COLOR_UNSELECTED = Color.GREY;
    private static final int CIRCLE_RADIUS = 20;
    private static final double OFFSET_X = 50;
    private static final double OFFSET_Y = 50;

    private final ObjectProperty<Memento> selectionModel = new SimpleObjectProperty<>();
    private final MementoModel model;
    private final Function<MementoBranch, Color> colorProvider;

    public MementoView(MementoModel model, Function<MementoBranch, Color> colorProvider) {
        this.model = model;
        this.colorProvider = colorProvider;
        model.addListener(() -> {
            getChildren().clear();
            drawMementoBranch(model.getMasterBranch());
        });
        drawMementoBranch(model.getMasterBranch());
    }

    public MementoModel getModel() {
        return model;
    }

    private Node createLabelledCircle(Memento memento, Color valueNodeColor) {
        Circle circle = new Circle(CIRCLE_RADIUS, CIRCLE_FILL_COLOR);
        circle.setFill(valueNodeColor);
        circle.setStrokeWidth(3);
        circle.strokeProperty().bind(Bindings.createObjectBinding(() -> {
            if (memento.equals(selectionModel.get())) {
                return CIRCLE_STROKE_COLOR_SELECTED;
            } else {
                return CIRCLE_STROKE_COLOR_UNSELECTED;
            }
        }, selectionModel));
        circle.setOnMouseClicked(e -> selectionModel.set(memento));
        circle.setCursor(Cursor.HAND);

        Text text = new Text(memento.getDisplayName());
        text.setMouseTransparent(true);
        text.setBoundsType(TextBoundsType.VISUAL);

        StackPane labelledCircle = new StackPane();
        labelledCircle.getChildren().addAll(circle, text);
        return labelledCircle;
    }

    private void drawMementoBranch(MementoBranch branch) {
        getChildren().add(draw(branch));
    }

    private Group draw(MementoBranch branch) {
        Group group = new Group();
        Color color = colorProvider.apply(branch);
        drawBranchImpl(branch, 0, 0, group, color);
        return group;
    }

    private void drawBranchImpl(MementoBranch branch, final int col, final int row, Group group, Color color) {
        for (int i = 0; i < branch.getMementos().size(); i++) {
            Memento memento = branch.getMementos().get(i);
            double x = (i + col) * OFFSET_X;
            double y = row * OFFSET_Y;

            Node labelledCircle = createLabelledCircle(memento, color);
            labelledCircle.setTranslateX(x);
            labelledCircle.setTranslateY(y);

            group.getChildren().add(labelledCircle);

            int rowHeight = MementoHeightCalculator.getRowHeight(branch, i + 1);

            int siblingRowHeightAcc = 0;
            for (int j = 0; j < memento.getBranches().size(); j++) {
                siblingRowHeightAcc += j == 0 ? 0 : MementoHeightCalculator.getRowHeight(memento.getBranches().get(j - 1), 0);
                MementoBranch childBranch = memento.getBranches().get(j);
                drawBranchImpl(childBranch, (i + col) + 1, row + rowHeight + siblingRowHeightAcc, group, colorProvider.apply(childBranch));
            }

        }
    }

    public ReadOnlyObjectProperty<Memento> getSelectionModel() {
        return selectionModel;
    }

}