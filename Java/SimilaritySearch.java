import java.io.File;
import java.util.*;

public class SimilaritySearch {
    // to run the main program: java SimilaritySearch queryImages/q01.jpg imageDataset2_15_20
    public static void main(String[] args) {
        String queryImageFilename = args[0]; // in the format "queryImages/q01.jpg" since the file is in a different directory
        String imageDatasetDirectory = args[1];

        // read the query image
        ColorImage queryImage = new ColorImage(queryImageFilename);

        // compute the color histogram of the query image
        ColorHistogram queryHistogram = new ColorHistogram(3);
        queryHistogram.setImage(queryImage);
        queryHistogram.computeHistogram();
        queryHistogram.save(queryImageFilename + ".txt");

        // read the images from the dataset
        File directory = new File(imageDatasetDirectory);
        File[] files = directory.listFiles();

        // arrayList to store the similarity values
        ArrayList<ColorImage> similarImages = new ArrayList<ColorImage>();

        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".jpg")) {
                    
                    // read the image from the dataset
                    ColorImage image = new ColorImage(file.getPath());
                    image.setFilename(file.getName());

                    // get the histogram of the image
                    ColorHistogram imageHistogram = new ColorHistogram(file.getPath() + ".txt");
                    imageHistogram.setImage(image);

                    // compare the histograms
                    double similarity = imageHistogram.compare(queryHistogram);
                    image.setSimilarity(similarity);
                    
                    // add the image to the arrayList if it's one of the 5 most similar images
                    if (similarImages.size() < 5) {
                        similarImages.add(image);
                    } else {
                        for (int i = 0; i < similarImages.size(); i++) {
                            if (similarity > similarImages.get(i).getSimilarity()) {
                                similarImages.set(i, image);
                                break;
                            }
                        }
                    }
                    
                }
            }
        }

        System.out.println("The 5 most similar images are:");
        for (ColorImage image : similarImages) {
            System.out.println(image.getFilename() + ", intersection: " + image.getSimilarity());
        }

    }
}
