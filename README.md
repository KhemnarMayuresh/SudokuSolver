# SudokuSolver
An android application that can solve a Sudoku puzzle by both manual and camera mode. Where the manual mode needs to enter numbers in each cell in the grid as in the puzzle whereas the in-camera mode device scans an image of Sudoku by using image processing and provide the solution for it.

### Screenshots of Working App :

<img src="images/1.png" alt="Input Puzzle in Manual mode" style="width:150pt"/>

<img src="images/2.png" alt="Input Puzzle in Manual mode" style="width:150pt"/>

<img src="images/3.png" alt="Input Puzzle in Manual mode" style="width:150pt"/>

<img src="images/4.png" alt="Solved Puzzle in Manual mode" style="width:150pt"/>

<img src="images/Image3.png" alt="Solved Puzzle in Camera mode" style="width:150pt"/>

<img src="images/croped outer block.jpeg" alt="Saved Image on Device storage" style="width:150pt"/>

### Used Technologies:
1. Android NDK
2. OpenCV version-2.4.10
3. Tesseract OCR

### How to run
1. Download the SudokuSolver package and open it in android studio.
2. Make sure your IDE have NDK support.
 

**Note:** *The App's Camera mode doesn't run on xiaomi devices. The reason behind the failure of camera mode in xiaomi devices is that the xiaomi device did not support the background synchronized thread. So, the OpenCV can’t be loaded on the device and hence the camera mode is crashed.*
