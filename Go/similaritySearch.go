package main

import (
	"fmt"
	"image"
	_ "image/jpeg"
	"log"
	"math"
	"os"
	"strings"
	"sync"
	"time"
)

// Histo struct to store the histogram of the image
type Histo struct {
	Name string
	H    []float64 // cannot be int because of the division
}

// Compute the histograms of the specified jpeg images
func computeHistogram(imagePath string, depth int) (Histo, error) {
	// Open the JPEG file
	file, err := os.Open(imagePath)
	if err != nil {
		return Histo{"", nil}, err
	}
	defer file.Close()

	// Decode the JPEG data
	img, _, err := image.Decode(file)
	if err != nil {
		return Histo{"", nil}, err
	}

	// Get the dimensions of the image
	bounds := img.Bounds()
	width, height := bounds.Max.X, bounds.Max.Y

	// Create a histogram of the image
	histogramSize := math.Pow(2, float64(depth*3))
	histogram := make([]float64, int(histogramSize))

	// Compute the reduced color histogram
	for y := 0; y < height; y++ {
		for x := 0; x < width; x++ {
			// Convert the pixel to RGBA
			red, green, blue, _ := img.At(x, y).RGBA()

			// A color's RGBA method returns values in the range [0, 65535].
			// Shifting by 8 reduces this to the range [0, 255].
			red >>= 8
			green >>= 8
			blue >>= 8

			// Reduce the color space
			red >>= (8 - depth)
			green >>= (8 - depth)
			blue >>= (8 - depth)

			index := int((red << (2 * depth)) + (green << depth) + blue)
			histogram[index]++
		}
	}

	// Normalize the histogram
	totalPixels := float64(width * height)
	for i := range histogram {
		histogram[i] /= totalPixels
	}

	h := Histo{imagePath, histogram}
	return h, nil
}

// Compute the histograms of a slice of image filenames
func computeHistograms(imagePath []string, depth int, hChan chan<- Histo) {
	for _, path := range imagePath {
		h, err := computeHistogram(path, depth)
		if err != nil {
			log.Fatal(err)
			continue
		}
		hChan <- h
	}
}

// Usage: go run similaritySearch.go queryImages/q00.jpg imageDataset2_15_20
func main() {
	start := time.Now()
	// read the directory name from the command line
	args := os.Args

	files, err := os.ReadDir(args[2])
	if err != nil {
		log.Fatal(err)
	}

	// Create an array of image paths
	var filenames []string

	// Get the list of jpg files
	for _, file := range files {
		if strings.HasSuffix(file.Name(), ".jpg") {
			filenames = append(filenames, fmt.Sprint(args[2], "/", file.Name()))
		}
	}

	// Create a channel to store the histograms
	hChan := make(chan Histo, len(filenames))

	// Split the list into K slices and send each slice to the computeHistograms function
	K := 64 // Number of threads
	sliceSize := len(filenames) / K
	var wg sync.WaitGroup
	for i := 0; i < K; i++ {
		start := i * sliceSize
		end := start + sliceSize
		if i == K-1 {
			end = len(filenames)
		}
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			computeHistograms(filenames[start:end], 3, hChan)
		}(i)
	}

	// Wait for all the computeHistograms threads to finish
	wg.Wait()
	close(hChan)

	// In a seperate thread, open the query image and compute its histogram
	queryChan := make(chan Histo, 1)
	go func() {
		queryHist, err := computeHistogram(args[1], 3)
		if err != nil {
			log.Println("Error computing query histogram:", err)
			return
		}
		queryChan <- queryHist
	}()

	// Wait for the query histogram to be computed
	queryHist := <-queryChan

	similarImages := make([]string, 5)
	mostSimilar := make([]float64, 5)
	// Compare histograms and maintain list of K most similar images
	for histo := range hChan {
		// Compute the similarity between the query image and the current image
		similarity := 0.0
		for i := 0; i < len(histo.H); i++ {
			similarity += float64(math.Min(histo.H[i], queryHist.H[i]))
		}

		// Update the list of K most similar images
		for j := range similarImages {
			if similarity > float64(mostSimilar[j]) {
				copy(mostSimilar[j+1:], mostSimilar[j:])
				mostSimilar[j] = similarity
				copy(similarImages[j+1:], similarImages[j:])
				similarImages[j] = histo.Name
				break
			}
		}
	}

	// Print the list of K most similar images
	for i := range similarImages {
		fmt.Println(similarImages[i], mostSimilar[i])
	}

	elapsed := time.Since(start)
	fmt.Println("Time taken for K =", K, "for", args[1], "is", elapsed)

}
