#lang scheme
; get the list of all textfiles in a directory
; (list-text-files-in-directory "C:\\Users\\Documents\\csi2520")
(define (list-text-files-in-directory directory-path)
  (map (lambda (file)
         (string-append directory-path "/" (path->string file)))
       (filter (lambda (file)
                 (string-suffix? (path->string file) ".txt"))
               (directory-list directory-path))))

			   
; read a histogram textfile and returns the values in a list
; (read-hist-file "C:\\Users\\Documents\\csi2520\\q00.jpg.txt")
(define (read-hist-file filename) 
(cdr (call-with-input-file filename
  (lambda (p)
    (let f ((x (read p)))
      (if (eof-object? x) '() (cons x (f (read p)))))))))

; norrmalize the histograms
; input: histograms represented as a list
; output: normalized histogram represented as a list
(define (normalize hist)
  (define total (apply + hist))
  (map (lambda (x) (/ x total)) hist))

; calculate histogram intersection
; input: 2 normalized histograms represented as a list
; output: the similarity between the 2 histograms
(define (compare-hist hist1 hist2)
  (if (or (null? hist1) (null? hist2))
      0.0
      (+ (min (car hist1) (car hist2))
         (compare-hist (cdr hist1) (cdr hist2)))))

; calculate the similarity between the query image and the images in the dataset
; input: normalized histogram of the query image represented as a list
; output: list of textfiles of histograms of the images in the dataset
(define (similarity query imagesList)
  (map
   (lambda (file)
     ; Extract just the filename without the directory path
     (let* ((file-parts (reverse (string-split file "/")))
            (filename (car file-parts)))
       ; Read the histogram file
       (let ((histogram (read-hist-file file)))
         ; Normalize the histogram
         (let ((normalized-hist (normalize histogram)))
           ; Calculate the intersection score
           (let ((intersection (compare-hist query normalized-hist)))
             ; Return a filename-similarity pair
             (cons filename intersection))))))
   imagesList))

; sorts a list of filename-similarity pairs based on the similarity in descending order
; input: list of filename-similarity pairs
; output: sorted list
(define (sort-similarity similarity)
  (sort similarity (lambda (a b) (> (cdr a) (cdr b)))))

; find the name of the 5 most similar images to the query image
; input: the filename of the query image, the directory of the dataset
; output: the name and the similarity of the top 5 most similar images
; usage example: (similaritySearch "queryImages/q00.jpg.txt" "imageDataset2_15_20")
(define (similaritySearch queryHistogramFilename imageDatasetDirectory)
  (display "Query image filename: ")
  (display queryHistogramFilename)
  (newline)
  (define query (read-hist-file queryHistogramFilename))
  (define norm-query (normalize query))
  (define listOfImages(list-text-files-in-directory imageDatasetDirectory))
  (define similarity-list(similarity norm-query listOfImages))
  (define sorted-list(sort-similarity similarity-list))
  (let ((top-five (take sorted-list 5)))
    (for-each (lambda (pair)
                (display "Filename: ")
                (display (car pair))
                (display ", ")
                (display "Similarity: ")
                (display (cdr pair))
                (newline)) top-five)))

