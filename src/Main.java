import com.sun.javafx.PlatformUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.effect.Light;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class Main extends Application{
    String path = null;
    double imArea;
    ImageView globalImageView = null;
    Text t_ratio = new Text();
    Text t_n_boxes = new Text();
    Bounds imBounds;
    double proportion;
    int n_boxes;
    List<File> listFiles = new ArrayList<File>();
    List<imgData> listData = new ArrayList<imgData>();
    File file = null;
    Boolean exported = null;
    SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss");
    Stack<Rectangle> lastDragbox = new Stack<Rectangle>();
    Stack<Double> lastArea = new Stack<Double>();



    public void loadImage(BorderPane pane, boolean reload){
        ImageView myImageView = new ImageView();
        file = listFiles.get(0);
        Image image = new Image(file.toURI().toString());
        myImageView.setImage(image);
        exported = false;
        myImageView.setFitHeight(pane.getHeight()*.80);
        myImageView.setFitWidth(pane.getWidth()*.80);
        myImageView.setPreserveRatio(true);
        myImageView.setSmooth(true);

        imBounds = myImageView.localToScene(myImageView.getBoundsInLocal());
        imArea = imBounds.getHeight() * imBounds.getWidth();
        pane.setCenter(myImageView);

        myImageView.addEventFilter(MouseEvent.ANY, new EventHandler<MouseEvent>() {
        Rectangle dragBox = null;
        Light.Point anchor = null;

        @Override
        public void handle(MouseEvent mouseEvent) {
            //System.out.println(mouseEvent.getEventType());
            if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
                double ax = mouseEvent.getSceneX();
                double ay = mouseEvent.getSceneY();
                n_boxes+=1;
                dragBox = new Rectangle(0, 0, 0, 0);
                dragBox.setFill(null);
                dragBox.setStroke(Color.RED);
                dragBox.setStrokeWidth(2);
                dragBox.setTranslateX(ax);
                dragBox.setTranslateY(ay);
                pane.getChildren().add(dragBox);
                t_n_boxes.setText(String.valueOf(n_boxes));
                anchor = new Light.Point();
                anchor.setX(ax);
                anchor.setY(ay);
            }
            if ((mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED) && (dragBox != null)) {
                double cx = mouseEvent.getSceneX();
                double cy = mouseEvent.getSceneY();

                dragBox.setWidth(Math.abs(cx - anchor.getX()));
                dragBox.setHeight(Math.abs(cy - anchor.getY()));
                dragBox.setTranslateX(Math.min(anchor.getX(), cx));
                dragBox.setTranslateY(Math.min(anchor.getY(), cy));

                double delta =  (dragBox.getHeight()*dragBox.getWidth())/imArea;
                t_ratio.setText(String.format("%.12f", proportion+delta));

            }
            if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
                if(dragBox.getHeight()==0 || dragBox.getWidth()==0){
                    pane.getChildren().remove(dragBox);
                    n_boxes-=1;
                    t_n_boxes.setText(String.valueOf(n_boxes));

                }
                else{
                    double curArea = ((dragBox.getHeight()*dragBox.getWidth())/imArea);

                    proportion+=(curArea);

                    lastDragbox.push(dragBox);
                    lastArea.push(curArea);
                }
                dragBox = null;
                anchor = null;
            }
        }});

        globalImageView = myImageView;


    }

    public void screenshot(imgData img_data, BorderPane pane){
            Bounds bounds = globalImageView.getBoundsInLocal();
            Bounds screenBounds = globalImageView.localToScreen(bounds);
            int x = (int) screenBounds.getMinX();
            int y = (int) screenBounds.getMinY();
            int width = (int) screenBounds.getWidth();
            int height = (int) screenBounds.getHeight();
            try {
            Robot robot = new Robot();
            java.awt.Rectangle screenRect = new java.awt.Rectangle(x, y, width, height);

            BufferedImage image = robot.createScreenCapture(screenRect);

            if(PlatformUtil.isWindows()){
                File file = new File(path+"images\\"+"proportioner_"+dateFormat.format(img_data.getTimestamp())+"_"+img_data.getFilename());
                ImageIO.write(image, "png", file);
            }
            if(PlatformUtil.isMac()){
                File file = new File(path+"images/"+"proportioner_"+dateFormat.format(img_data.getTimestamp())+"_"+img_data.getFilename());
                ImageIO.write(image, "png", file);

            }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }

    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        if(PlatformUtil.isWindows()){
            path = new String("C:\\Users\\"+System.getProperty("user.name")+"\\Desktop\\Proportioner\\");
        }

        else if(PlatformUtil.isMac()){
            path = new String("/Users/"+System.getProperty("user.name")+"/Desktop/Proportioner/");
        }

        File directory = new File(path);
        if (!directory.exists()){
            directory.mkdir();
        }

        directory = new File(path+"images");
        if (!directory.exists()){
            directory.mkdir();
        }





        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color: #FFFFFF;");
        pane.setPadding(new Insets(10));


        //DISPLAY AREA
        Text t_n_boxes_junk = new Text();
        t_n_boxes_junk.setText("Number of boxes: ");
        t_n_boxes_junk.setStyle("    -fx-font-size: 14px;" +
                " -fx-font-weight: bold");


        t_n_boxes.setText("0");
        t_n_boxes.setStyle("    -fx-font-size: 14px;");

        HBox boxesArea = new HBox(t_n_boxes_junk, t_n_boxes);
        boxesArea.setAlignment(Pos.CENTER);

        Text t_ratio_junk= new Text();
        t_ratio_junk.setText("Ratio: ");
        t_ratio_junk.setStyle("    -fx-font-size: 14px;" +
                " -fx-font-weight: bold");

        t_ratio.setText("0.00");
        t_ratio.setStyle("    -fx-font-size: 14px;");

        HBox ratioArea = new HBox(t_ratio_junk, t_ratio);
        ratioArea.setAlignment(Pos.CENTER);

        HBox displayArea = new HBox(boxesArea, ratioArea);
        displayArea.setAlignment(Pos.CENTER);
        displayArea.setSpacing(120);
        displayArea.setPadding( new Insets( 10 ) );


        //BUTTONS BOTTOM
        Button undo = new Button("Undo");
        Button clear = new Button("Clear");
        Button load = new Button("Load images");
        Button next = new Button("Save & Next");
        Button export = new Button("Export");
        undo.setOnAction(l -> {
            if(lastDragbox.size()!=0){
                pane.getChildren().remove(lastDragbox.pop());
                proportion -= lastArea.pop();
                if(proportion<0){
                    proportion*=-1;
                }
                n_boxes-=1;
                t_n_boxes.setText(String.valueOf(n_boxes));
                t_ratio.setText(String.format("%.12f", proportion));
                exported = false;
            }
        });
        export.setOnAction(l -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Export");
            alert.setHeaderText(null);
            alert.setContentText("Please choose a file to export to");

            ButtonType buttonTypeOne = new ButtonType("New file",  ButtonBar.ButtonData.FINISH);
            ButtonType buttonTypeTwo = new ButtonType("Load file", ButtonBar.ButtonData.BACK_PREVIOUS);
            ButtonType buttonTypeThree = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeThree);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonTypeOne){
                // write a new file
                Timestamp time = new Timestamp(System.currentTimeMillis());
                String csvFile = path+"proportioner_"+dateFormat.format(time)+".csv";
                try {
                FileWriter writer = new FileWriter(csvFile);

                    writer.write("timestamp,filename,n_boxes,sel_ratio");

                for (imgData d : listData) {
                    writer.write("\n");
                    writer.write(d.toString());
                }
                    writer.flush();
                    writer.close();
                    Alert alert_exp = new Alert(Alert.AlertType.INFORMATION);
                    alert_exp.setTitle("Export");
                    alert_exp.setHeaderText(null);
                    alert_exp.setContentText("Data successfully exported!");
                    listData.clear();

                    alert_exp.showAndWait();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                exported = true;
            } else if (result.get() == buttonTypeTwo) {

                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV File", "*.csv"));
                File file = fileChooser.showOpenDialog(null);
                String csvFile = file.getAbsolutePath();
                try {
                FileWriter writer = new FileWriter(csvFile, true);

                for (imgData d : listData) {
                    writer.write("\n");
                    writer.write(d.toString());
                    }
                    writer.flush();
                    writer.close();
                    Alert alert_exp = new Alert(Alert.AlertType.INFORMATION);
                    alert_exp.setTitle("Export");
                    alert_exp.setHeaderText(null);
                    alert_exp.setContentText("Data successfully exported!");
                    listData.clear();

                    alert_exp.showAndWait();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                exported = true;
            }
        });

        HBox toolbarArea = new HBox(load, undo, clear, next, export);
        toolbarArea.setAlignment(Pos.CENTER);
        toolbarArea.setSpacing(20);
        toolbarArea.setPadding( new Insets( 10 ) );

        clear.setOnAction(t -> {
            try {
                proportion = 0.00;
                n_boxes = 0;
                lastDragbox.clear();
                t_n_boxes.setText(String.valueOf(n_boxes));
                t_ratio.setText(String.format("%.12f", proportion));
                pane.getChildren().clear();
                pane.setTop(displayArea);
                pane.setBottom(toolbarArea);
                loadImage(pane, true);
                //pane.setCenter(loadImage(pane, true));
                exported = false;
            }
            catch (IndexOutOfBoundsException e){

            }
        });

        next.setOnAction(l -> {
            try{
            imgData newEntry = new imgData(listFiles.get(0).getName(), n_boxes, proportion);
            if(listData.contains(newEntry)){
                int ind = listData.indexOf(newEntry);
                imgData updateEntry = listData.get(ind);
                updateEntry.setTimestamp();
                updateEntry.setN_boxes(n_boxes);
                updateEntry.setSel_ratio(proportion);
            }
            else{
                listData.add(newEntry);
            }

            screenshot(listData.get(listData.size() - 1), pane);
            listFiles.remove(0);
            }
            catch (Exception junkException){
                //do nothing
            }

            if(listFiles.size()!=0){
                proportion = 0.00;
                n_boxes = 0;
                lastDragbox.clear();
                t_n_boxes.setText(String.valueOf(n_boxes));
                t_ratio.setText(String.format("%.12f", proportion));
                pane.getChildren().clear();
                pane.setTop(displayArea);
                pane.setBottom(toolbarArea);

                loadImage(pane, false);
 //               pane.setCenter(loadImage(pane, false));

            }
            else{
                proportion = 0.00;
                n_boxes = 0;
                lastDragbox.clear();
                t_n_boxes.setText(String.valueOf(n_boxes));
                t_ratio.setText(String.format("%.12f", proportion));
                pane.getChildren().clear();
                pane.setTop(displayArea);
                pane.setBottom(toolbarArea);

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setTitle("");
                alert.setContentText("There are no images to load.\nPlease select a new set of images to load.");
                alert.showAndWait();

            }


        });

        load.setOnAction(t -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            List<File> listFiles1 = fileChooser.showOpenMultipleDialog(null);
            if(listFiles1!=null) {
                if(listFiles.size()==0){
                    listFiles.addAll(listFiles1);
                    pane.getChildren().clear();
                    pane.setTop(displayArea);
                    pane.setBottom(toolbarArea);
                    loadImage(pane, false);
                }
                else{
                    listFiles.addAll(listFiles1);
                }

            }

        });


        pane.setTop(displayArea);
        pane.setBottom(toolbarArea);
        Scene scene = new Scene(pane, 1000, 640);
        scene.getStylesheets().add("stylesheet.css");





        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                Alert alert;
                Optional<ButtonType> result;
                if(listFiles.size()!=0 || (exported!=null && exported==false)){
                    alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText(null);
                    alert.setTitle("Quit");
                    alert.setContentText("Are you sure you want to exit?");
                    ButtonType b1 = new ButtonType("Yes",  ButtonBar.ButtonData.NO);
                    ButtonType b2 = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                    alert.getButtonTypes().setAll(b1, b2);

                    result = alert.showAndWait();
                    if (result.get() == b1){
                        if(exported==false) {
                            Alert alert1 = new Alert(Alert.AlertType.CONFIRMATION);
                            alert1.setTitle("Export");
                            alert1.setHeaderText(null);
                            alert1.setContentText("There is unsaved data.\nPlease choose a file to export to");

                            ButtonType buttonTypeOne = new ButtonType("New file",  ButtonBar.ButtonData.FINISH);
                            ButtonType buttonTypeTwo = new ButtonType("Load file", ButtonBar.ButtonData.BACK_PREVIOUS);
                            ButtonType buttonTypeThree = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                            ButtonType buttonTypeFour = new ButtonType("Close without saving", ButtonBar.ButtonData.RIGHT);

                            alert1.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeFour, buttonTypeThree);

                            Optional<ButtonType> result1 = alert1.showAndWait();
                            if (result1.get() == buttonTypeOne){
                                // write a new file
                                Timestamp time = new Timestamp(System.currentTimeMillis());
                                String csvFile = path+"proportioner_"+dateFormat.format(time)+".csv";
                                try {
                                FileWriter writer = new FileWriter(csvFile);

                                    writer.write("timestamp,filename,n_boxes,sel_ratio");

                                    for (imgData d : listData) {
                                        writer.write("\n");
                                        writer.write(d.toString());
                                    }
                                    writer.flush();
                                    writer.close();
                                    Alert alert_exp = new Alert(Alert.AlertType.INFORMATION);
                                    alert_exp.setTitle("Export");
                                    alert_exp.setHeaderText(null);
                                    alert_exp.setContentText("Data successfully exported!");
                                    listData.clear();

                                    alert_exp.showAndWait();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                            else if (result1.get() == buttonTypeTwo) {
                                // load file and append
                                FileChooser fileChooser = new FileChooser();
                                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV File", "*.csv"));
                                File file = fileChooser.showOpenDialog(null);
                                String csvFile = file.getAbsolutePath();
                                try {
                                FileWriter writer = new FileWriter(csvFile, true);

                                    for (imgData d : listData) {
                                        writer.write("\n");
                                        writer.write(d.toString());
                                    }
                                    writer.flush();
                                    writer.close();
                                    Alert alert_exp = new Alert(Alert.AlertType.INFORMATION);
                                    alert_exp.setTitle("Export");
                                    alert_exp.setHeaderText(null);
                                    alert_exp.setContentText("Data successfully exported!");
                                    listData.clear();

                                    alert_exp.showAndWait();
                                }
                                catch (IOException e2) {
                                    e2.printStackTrace();
                                }
                                Platform.exit();
                                System.exit(0);
                            }
                            else if (result1.get() == buttonTypeFour) {
                                // load file and append
                                Platform.exit();
                                System.exit(0);
                            }

                            else {
                                e.consume();
                            }

                        }

                    }
                    else {
                        e.consume();
                    }

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
