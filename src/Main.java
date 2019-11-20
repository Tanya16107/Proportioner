import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.text.DecimalFormat;

public class Main extends Application {
    double imX, imY, imArea, imXX, imYY;
    double proportion;
    DecimalFormat df = new DecimalFormat("#.##");
    @Override
    public void start(Stage primaryStage) throws Exception{
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(10));

        ImageView myImageView = new ImageView();

        Button calculate = new Button("Compute Ratio");
        calculate.setOnAction(l -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setTitle("");
            alert.setContentText("Ratio of selected regions is "+ df.format(proportion));
            alert.showAndWait();
        });

        Button load = new Button("Load image");
        HBox toolbarArea = new HBox(load, calculate);
        toolbarArea.setAlignment(Pos.CENTER);
        toolbarArea.setSpacing(20);
        toolbarArea.setPadding( new Insets( 10 ) );

        load.setOnAction(t -> {
            proportion = 0;
            pane.getChildren().clear();
            pane.setBottom(toolbarArea);
            pane.setCenter(myImageView);

            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                Image image = new Image(file.toURI().toString());
                myImageView.setImage(image);
                myImageView.setFitHeight(pane.getHeight()*.70);
                //myImageView.fitHeightProperty().bind(pane.heightProperty().multiply(.7));
                myImageView.setPreserveRatio(true);
                myImageView.setSmooth(true);
                myImageView.setCache(true);
                //System.out.println(myImageView.getBoundsInParent());
                Bounds imBounds = myImageView.localToScene(myImageView.getBoundsInLocal());
                imX = imBounds.getMinX();
                imY = imBounds.getMinY();
                imXX = imBounds.getMaxX();
                imYY = imBounds.getMaxY();

                imArea = imBounds.getHeight() * imBounds.getWidth();



            }

        });

        pane.setCenter(myImageView);
        pane.setBottom(toolbarArea);
        Scene scene = new Scene(pane, 1000, 640);

        myImageView.addEventFilter(MouseEvent.ANY, new EventHandler<MouseEvent>() {
            Rectangle dragBox = null;
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    dragBox = new Rectangle(0, 0, 0, 0);
                    dragBox.setFill(null);
                    dragBox.setStroke(Color.RED);
                    pane.getChildren().add(dragBox);
                    dragBox.setTranslateX(mouseEvent.getSceneX());
                    dragBox.setTranslateY(mouseEvent.getSceneY());
                }
                if ((mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED) && (dragBox != null)) {
                    dragBox.setWidth((mouseEvent.getSceneX() - dragBox.getTranslateX()));
                    dragBox.setHeight((mouseEvent.getSceneY() - dragBox.getTranslateY()));

                }
                if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
                    proportion+=((dragBox.getHeight()*dragBox.getWidth())/imArea);
                }
            }

        });

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Proportioner");
        primaryStage.show();


    }



    public static void main(String[] args) {
        launch(args);
    }
}
