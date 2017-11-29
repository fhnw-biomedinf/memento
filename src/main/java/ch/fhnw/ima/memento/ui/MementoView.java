package ch.fhnw.ima.memento.ui;

import ch.fhnw.ima.memento.model.Memento;
import ch.fhnw.ima.memento.model.MementoBranch;
import ch.fhnw.ima.memento.model.MementoModel;
import ch.fhnw.ima.memento.util.MementoHeightCalculator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

import java.util.Optional;
import java.util.function.Function;

public class MementoView extends Region {

    private static final Color CIRCLE_STROKE_COLOR_SELECTED = Color.BLACK;
    private static final Color CIRCLE_STROKE_COLOR_UNSELECTED = Color.GREY;
    private static final int CIRCLE_RADIUS = 20;

    private static final int LINE_STROKE_WIDTH = 3;
    private static final Color LINE_COLOR = Color.LIGHTGRAY;

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

    private static Line createLine(double x, double y, double parentX, double parentY) {
        Line line = new Line(parentX, parentY, x, y);
        line.setTranslateX(CIRCLE_RADIUS);
        line.setTranslateY(CIRCLE_RADIUS);
        line.setMouseTransparent(true);
        line.setStrokeWidth(LINE_STROKE_WIDTH);
        line.setStroke(LINE_COLOR);
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        return line;
    }

    public MementoModel getModel() {
        return model;
    }

    private Node createLabelledCircle(Memento memento, double x, double y, Color valueNodeColor) {
        Circle circle = new Circle(CIRCLE_RADIUS);
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
        labelledCircle.setTranslateX(x);
        labelledCircle.setTranslateY(y);
        labelledCircle.getChildren().addAll(circle, text);
        return labelledCircle;
    }

    private void drawMementoBranch(MementoBranch branch) {
        getChildren().add(draw(branch));
    }

    private Group draw(MementoBranch branch) {
        Group nodeGroup = new Group();
        Group lineGroup = new Group();
        Color color = colorProvider.apply(branch);
        drawBranchImpl(branch, 0, 0, nodeGroup, lineGroup, color, Optional.empty());
        return new Group(lineGroup, nodeGroup);
    }

    private void drawBranchImpl(MementoBranch branch, final int col, final int row, Group nodeGroup, Group lineGroup, Color color, Optional<Point2D> optionalParentCoordinates) {
        for (int i = 0; i < branch.getMementos().size(); i++) {
            Memento memento = branch.getMementos().get(i);
            int mementoCol = col + i;
            Optional<Point2D> mementoCoordinates = Optional.of(new Point2D(mementoCol, row));

            double x = mementoCol * OFFSET_X;
            double y = row * OFFSET_Y;

            Node labelledCircle = createLabelledCircle(memento, x, y, color);
            nodeGroup.getChildren().add(labelledCircle);

            optionalParentCoordinates.ifPresent(parentCoordinates -> {

                double parentX = parentCoordinates.getX() * OFFSET_X;
                double parentY = parentCoordinates.getY() * OFFSET_Y;

                if (parentCoordinates.getX() == col) {
                    // horizontally connecting nodes in a branch
                    Line line = createLine(x, y, parentX, parentY);
                    lineGroup.getChildren().add(line);
                } else {
                    // L-shape connecting parent with branch start
                    Line lineVertical = createLine(parentX, y, parentX, parentY);
                    Line lineHorizontal = createLine(x, y, parentX, y);
                    lineGroup.getChildren().addAll(lineVertical, lineHorizontal);
                }
            });

            int rowHeight = MementoHeightCalculator.getRowHeight(branch, i + 1);

            int siblingRowHeightAcc = 0;
            for (int j = 0; j < memento.getBranches().size(); j++) {
                siblingRowHeightAcc += j == 0 ? 0 : MementoHeightCalculator.getRowHeight(memento.getBranches().get(j - 1), 0);
                MementoBranch childBranch = memento.getBranches().get(j);
                drawBranchImpl(childBranch, (i + col) + 1, row + rowHeight + siblingRowHeightAcc, nodeGroup, lineGroup, colorProvider.apply(childBranch), mementoCoordinates);
            }

            optionalParentCoordinates = mementoCoordinates;
        }
    }

    public ReadOnlyObjectProperty<Memento> getSelectionModel() {
        return selectionModel;
    }

}