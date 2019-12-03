package com.mit.miniproject.sudokusolver;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

import com.mit.miniproject.sudokusolver.manual.SudokuGrid;

public class SudokuBord extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku_bord);


        GridLayout gridLayout= findViewById(R.id.sudokuGrid);
        //get screen size in pixels to adjust size of sudoku cells
        Display display=getWindowManager().getDefaultDisplay();
        Point size=new Point();
        display.getSize(size);
        int dimensions=size.x/11;
        SudokuGrid.initGrid(this,gridLayout,dimensions);

        final Button solveButton= findViewById(R.id.solveButton);


        solveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SudokuGrid.getCellValues();
                if(!SudokuGrid.getSolution()){
                    Toast.makeText(getApplicationContext(),"Solution does not exist",Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getApplicationContext(),"Solution Found",Toast.LENGTH_SHORT).show();
                SudokuGrid.updateSolution();
            }
        });

        Button clearButton= findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SudokuGrid.clearGrid();
            }
        });

    }
}
