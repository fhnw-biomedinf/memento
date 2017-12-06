package ch.fhnw.ima.memento;

import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.control.Option;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

/**
 * Displays a tree of {@link Memento}s as stored in a {@link MementoModel}.
 *
 * @param <S> State type of mementos kept in model
 * @author Rahel Lüthy
 */
public final class MementoView<S> extends Region {

    private static final Color CIRCLE_STROKE_COLOR_SELECTED = Color.BLACK;
    private static final Color CIRCLE_STROKE_COLOR_UNSELECTED = Color.GREY;
    private static final int CIRCLE_RADIUS = 20;

    private static final int LINE_STROKE_WIDTH = 3;
    private static final Color LINE_COLOR = Color.LIGHTGRAY;

    private static final double OFFSET_X = 50;
    private static final double OFFSET_Y = 50;

    private final ObjectProperty<Option<MementoRef>> selectionModel = new SimpleObjectProperty<>(Option.none());
    private final MementoModel<S> model;
    private final Function1<MementoBranchId, Color> colorProvider;
    private final BooleanProperty appendAllowed = new SimpleBooleanProperty(false);
    private final RowHeightCalculator<S> rowHeightCalculator;

    @SuppressWarnings("WeakerAccess")
    public MementoView(MementoModel<S> model, Function1<MementoBranchId, Color> colorProvider) {
        this.model = model;
        this.colorProvider = colorProvider;
        this.rowHeightCalculator = new RowHeightCalculator<>(model);

        model.addListener((MementoRef mementoRef) -> {
            getChildren().clear();
            selectionModel.set(Option.some(mementoRef));
            drawMementoBranch(model.getMasterBranchId());
        });

        selectionModel.addListener((observable, oldValue, newValue) -> {
            boolean isTip = newValue.map(ref -> {
                List<MementoId> mementos = model.getMementos(ref.getBranchId());
                return !mementos.isEmpty() && mementos.last().equals(ref.getMementoId());
            }).getOrElse(false);
            appendAllowed.set(isTip);
        });

        selectionModel.set(Option.none());

        drawMementoBranch(model.getMasterBranchId());
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

    private static boolean mementoEq(Memento<?> memento, Option<MementoRef> refOption) {
        return refOption.map(ref -> memento.getId().equals(ref.getMementoId())).getOrElse(false);
    }

    private Node createLabelledCircle(MementoRef mementoRef, double x, double y, Color valueNodeColor) {
        Option<Memento<S>> mementoOption = model.getMemento(mementoRef.getMementoId());

        StackPane resultNode = new StackPane();
        resultNode.setTranslateX(x);
        resultNode.setTranslateY(y);

        mementoOption.forEach(memento -> {

            Circle circle = new Circle(CIRCLE_RADIUS);
            circle.setFill(valueNodeColor);
            circle.setStrokeWidth(3);
            circle.strokeProperty().bind(Bindings.createObjectBinding(() -> {
                if (mementoEq(memento, selectionModel.get())) {
                    return CIRCLE_STROKE_COLOR_SELECTED;
                } else {
                    return CIRCLE_STROKE_COLOR_UNSELECTED;
                }
            }, selectionModel));
            circle.setOnMouseClicked(e -> {
                if (mementoEq(memento, selectionModel.get())) {
                    selectionModel.set(Option.none());
                } else {
                    selectionModel.set(Option.some(mementoRef));
                }
            });
            circle.setCursor(Cursor.HAND);

            Tooltip tooltip = new Tooltip(memento.getToolTip());
            Tooltip.install(circle, tooltip);

            Text text = new Text(memento.getLabel());
            text.setMouseTransparent(true);
            text.setBoundsType(TextBoundsType.VISUAL);

            resultNode.getChildren().addAll(circle, text);

        });

        return resultNode;
    }

    private void drawMementoBranch(MementoBranchId branch) {
        getChildren().add(draw(branch));
    }

    private Group draw(MementoBranchId branch) {
        Group nodeGroup = new Group();
        Group lineGroup = new Group();
        Color color = colorProvider.apply(branch);
        drawBranchImpl(branch, 0, 0, nodeGroup, lineGroup, color, Option.none());
        return new Group(lineGroup, nodeGroup);
    }

    private void drawBranchImpl(MementoBranchId branchId, final int col, final int row, Group nodeGroup, Group lineGroup, Color color, Option<Point2D> optionalParentCoordinates) {
        List<Memento<S>> mementos = model.getMementos(branchId).flatMap(model::getMemento);
        for (int i = 0; i < mementos.size(); i++) {
            Memento memento = mementos.get(i);
            int mementoCol = col + i;
            Option<Point2D> mementoCoordinates = Option.some(new Point2D(mementoCol, row));

            double x = mementoCol * OFFSET_X;
            double y = row * OFFSET_Y;

            MementoRef mementoRef = new MementoRef(memento.getId(), branchId);
            Node labelledCircle = createLabelledCircle(mementoRef, x, y, color);
            nodeGroup.getChildren().add(labelledCircle);

            optionalParentCoordinates.forEach(parentCoordinates -> {

                double parentX = parentCoordinates.getX() * OFFSET_X;
                double parentY = parentCoordinates.getY() * OFFSET_Y;

                if (parentCoordinates.getX() == col) {
                    // horizontally connecting nodes in a branchId
                    Line line = createLine(x, y, parentX, parentY);
                    lineGroup.getChildren().add(line);
                } else {
                    // L-shape connecting parent with branchId start
                    Line lineVertical = createLine(parentX, y, parentX, parentY);
                    Line lineHorizontal = createLine(x, y, parentX, y);
                    lineGroup.getChildren().addAll(lineVertical, lineHorizontal);
                }
            });

            int rowHeight = rowHeightCalculator.calcRowHeight(branchId, i + 1);

            int siblingRowHeightAcc = 0;

            List<MementoBranchId> branches = model.getBranches(memento.getId());
            for (int j = 0; j < branches.size(); j++) {
                siblingRowHeightAcc += j == 0 ? 0 : rowHeightCalculator.calcRowHeight(branches.get(j - 1), 0);
                MementoBranchId childBranch = branches.get(j);
                drawBranchImpl(childBranch, (i + col) + 1, row + rowHeight + siblingRowHeightAcc, nodeGroup, lineGroup, colorProvider.apply(childBranch), mementoCoordinates);
            }

            optionalParentCoordinates = mementoCoordinates;
        }
    }

    public ReadOnlyObjectProperty<Option<MementoRef>> getSelectionModel() {
        return selectionModel;
    }

    public ReadOnlyBooleanProperty appendAllowedProperty() {
        return appendAllowed;
    }

}