# Similarity image search
Find images that are similar to a query image by comparing their histograms.

The histogram is simply the count of the colors contained in an image. It involves counting how many pixels have the color [0, 0, 0], how many have the color [0, 0, 1], and so on. However, this would mean counting  the  pixels  for  all  16  million  possible  colors,  which  is  expensive  and  not  very  precise.  It  is therefore recommended to reduce the color space. This can be done by simply reducing the number of possible values, for example, by going from 8-bit values to 3-bit values per channel, resulting in a color space of only 8 x 8 x 8 = 512 possible colors. In this case, a simple bit right-shift by 8 – 3 = 5 positions reduces the space. The histogram will then have only 512 entries, a bin for each of the possible colors. To compare images with different resolutions (different numbers of pixels), it is necessary to normalize the histogram, i.e., divide each entry by the total number of pixels (such that by summing all the values of the histogram, we will obtain 1.0).

## Histogram comparison
As  explained,  the  images  will  be  compared  by  comparing  their  histograms.  This  can  be  done  using histogram intersection which can be computed, for the histograms H1 and H2, as follows:   

$$d\left(H_1, H_2 \right) = \sum_Imin \left(H_1\left(I \right), H_2\left(I \right)\right) $$

If the two histograms are identical, this sum will give a value equal to 1.0. Conversely, if the two images have no colors in common, then their histogram intersection will be equal to 0.0. Consequently, the more similar are two images, the closer to 1.0 will be their histogram intersection.

## Algorithm
The algorithm that searches the K most similar images to a query image I using a color space reduced to D bits is as follows:  
1. Compute the reduced color histogram of I 
   - Reduce the pixel values by applying (8-D) right bit shifts for each channel R, G, B. 
     - R' = R >> (8-D) 
     - G' = G >> (8-D) 
     - B' = B >> (8-D) 
   - The number of bins in the histogram H will be $N=2^{D*3}$
   - Count how many pixels of each color are contained in I to obtain histogram H. The histogram H is an array of N elements.
     - The index of the histogram bin corresponding to color [R',G',B'] can be computed as (R' << (2 * D)) + (G' << D) + B
     - Normalize H such that the values of all its bins sum to 1.0

2. Compare H with all pre-computed histograms in the image set.
   - This comparison is done using histogram intersection
   - Returns the K images with distances the closest to 1.0