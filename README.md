# SudokuSolver
An android application that can solve a Sudoku puzzle by both manual and camera mode. Where the manual mode needs to enter numbers in each cell in the grid as in the puzzle whereas the in-camera mode device scans an image of Sudoku by using image processing and provide the solution for it.

### Screenshots of Working App are in images folder:

### Used Technologies:
1. Android NDK
2. OpenCV version-2.4.10
3. Tesseract OCR

### How to run
1. Download the SudokuSolver package and open it in android studio.
2. Make sure your IDE have NDK support.
 

**Note:** *The App's Camera mode doesn't run on xiaomi devices. The reason behind the failure of camera mode in xiaomi devices is that the xiaomi device did not support the background synchronized thread. So, the OpenCV can’t be loaded on the device and hence the camera mode is crashed.*
