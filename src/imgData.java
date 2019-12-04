import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class imgData {
    private Timestamp timestamp;
    private String filename;
    private int n_boxes;
    private double sel_ratio;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public imgData(String filename, int n_boxes, double sel_ratio) {
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.filename = filename;
        this.n_boxes = n_boxes;
        this.sel_ratio = sel_ratio;
    }

    @Override
    public String toString() {
        return (dateFormat.format(timestamp) +
                "," + filename +
                "," + n_boxes +
                "," + String.format("%.12f", sel_ratio));
    }


    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getFilename() {
        return filename;
    }

    public int getN_boxes() {
        return n_boxes;
    }

    public double getSel_ratio() {
        return sel_ratio;
    }

    public void setTimestamp() {
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public void setN_boxes(int n_boxes) {
        this.n_boxes = n_boxes;
    }

    public void setSel_ratio(double sel_ratio) {
        this.sel_ratio = sel_ratio;
    }

    @Override
    public boolean equals(Object obj) {
        imgData obj1 = (imgData) obj;
        return (obj1.getFilename()).equals(this.filename);
    }
}
