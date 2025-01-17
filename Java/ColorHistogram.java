import java.io.*;
import java.util.Arrays;

public class ColorHistogram {
    private int d; 
    private double[] histogram;
    private ColorImage image;

    public ColorHistogram(int d) {
        this.d = d;

        // number of bins in the histogram is 2^(3d)
        int bins = (int) Math.pow(2, 3 * d);
        histogram = new double[bins];
    }

    public ColorHistogram(String filename) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String st;
            br.readLine(); // skip the first line of the file since it's the number of bins
            while ((st = br.readLine()) != null) {
                String[] line = st.split(" ");
                histogram = new double[line.length];
                for (int i = 0; i < line.length; i++) {
                    histogram[i] = Double.parseDouble(line[i]);;
                }
            } br.close();
        } catch (Exception e) {
            System.out.println("Error: " + e + " " + filename);
        }
    }

    public void setImage (ColorImage image) {
        this.image = image;
    }

    public double[] getHistogram() {
        // create a copy of the histogram array
        double[] normalizedHistogram = Arrays.copyOf(histogram, histogram.length);

        // compute the total number of pixels
        double totalPixels = image.getWidth() * image.getHeight();

        // compute the normalized histogram
        for (int i = 0; i < normalizedHistogram.length; i++) {
            normalizedHistogram[i] = normalizedHistogram[i] / totalPixels;
        }
        return normalizedHistogram;
    }

    public double compare(ColorHistogram hist) {
        double[] hist1 = this.getHistogram();
        double[] hist2 = hist.getHistogram();

        double intersection = 0;
        for (int i = 0; i < hist1.length; i++) {
            intersection += Math.min(hist1[i], hist2[i]);
        }
        return intersection;
    }

    // used to compute the histogram of the query image
    public void computeHistogram() {        
        // initialize the histogram
        Arrays.fill(histogram, 0);

        // reduce the pixel values
        image.reduceColor(d);

        // compute the reduced color histogram
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                int[] pixel = image.getPixel(i, j);
                int index = (pixel[0] << (2 * d)) + (pixel[1] << d) + pixel[2];
                histogram[index]++;
            }
        }
    }

    public void save(String filename) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            for (double value : histogram) {
                writer.write(Double.toString(value) + " ");
            }
            writer.close();
    } catch (Exception e) {
        System.out.println("Error: " + e);
    }
    }
}
