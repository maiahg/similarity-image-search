import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class ColorImage {
    private int width;
    private int height;
    private int depth;
    private BufferedImage image;
    private int[][][] pixels;
    private String filename; // to store the filename
    private double similarity; // to store the similarity value
    
    public ColorImage(String filename) {
        // read the image from the jpg file
        try {
            this.image = ImageIO.read(new File(filename));
            this.width = image.getWidth();
            this.height = image.getHeight();
            this.depth = image.getColorModel().getPixelSize(); // method to get the number of bits per pixel
            this.filename = filename;
            this.similarity = 0.0;

            // 2D array where each element is a 1D array of 3 elements for RGB
            pixels = new int[width][height][3]; 
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int rgb = image.getRGB(i, j);
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = (rgb & 0xFF);
                    pixels[i][j] = new int[]{red, green, blue};
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e + " " + filename);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }

    public int[] getPixel(int i, int j) {
        return pixels[i][j];
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void reduceColor(int d) {
        this.depth = d;

        // reduce pixel values by applying (8-d) right bit shifts for each channel R, G, B
        int shiftBits = 8 - d;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int[] pixel = getPixel(i, j);
                pixel[0] = pixel[0] >> shiftBits;
                pixel[1] = pixel[1] >> shiftBits;
                pixel[2] = pixel[2] >> shiftBits;
            }
        }
    }
}
