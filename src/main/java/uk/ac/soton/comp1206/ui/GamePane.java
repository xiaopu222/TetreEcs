package uk.ac.soton.comp1206.ui;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class GamePane extends StackPane {
    private static final Logger logger = LogManager.getLogger(GamePane.class);
    private final int width;
    private final int height;
    private double scalar = 1.0D;
    private boolean autoScale = true;

    public GamePane(int width, int height) {
        this.width = width;
        this.height = height;
        this.getStyleClass().add("gamepane");
        this.setAlignment(Pos.TOP_LEFT);
    }

    public void setScalar(double scalar) {
        this.scalar = scalar;
    }

    public void layoutChildren() {
        super.layoutChildren();
        if (this.autoScale) {
            double scaleFactorHeight = this.getHeight() / (double)this.height;
            double scaleFactorWidth = this.getWidth() / (double)this.width;
            if (scaleFactorHeight > scaleFactorWidth) {
                this.setScalar(scaleFactorWidth);
            } else {
                this.setScalar(scaleFactorHeight);
            }

            Scale scale = new Scale(this.scalar, this.scalar);
            double parentWidth = this.getWidth();
            double parentHeight = this.getHeight();
            double paddingLeft = (parentWidth - (double)this.width * this.scalar) / 2.0D;
            double paddingTop = (parentHeight - (double)this.height * this.scalar) / 2.0D;
            Translate translate = new Translate(paddingLeft, paddingTop);
            scale.setPivotX(0.0D);
            scale.setPivotY(0.0D);
            this.getTransforms().setAll(new Transform[]{translate, scale});
        }
    }
}
