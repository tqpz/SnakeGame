import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;

import java.awt.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 * Created by Mateusz on 29.03.2017.
 */
public class GameScene extends Pane {
    private int SNAKE_SIZE = 10; //size of one cell
    private int gameSceneWidth;
    private int gameSceneHeight;
    private Canvas canvas = new Canvas(gameSceneWidth, gameSceneHeight);
    private GraphicsContext gc = canvas.getGraphicsContext2D();
    private ArrayList<Point> snake;
    private ArrayList<Point> apple;
    private KeyCode lastDirection;
    private int snakeLenght = 1;
    private int posX = 10;
    private int posY = 10;

    public void setLastDirection(KeyCode lastDirection) {
        this.lastDirection = lastDirection;
    }

    public GameScene() {
        gameSceneHeight = 600;
        gameSceneWidth = 800;
        snake = new ArrayList<>(snakeLenght);
        snake.add(new Point(posX,posY));
        apple = new ArrayList<>(0);
        apple.add(new Point(generateApple()));
        getChildren().add(canvas); //add canvas to pane

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                boolean[][] cellsBoard = new boolean[gameSceneWidth / SNAKE_SIZE + 2][gameSceneHeight / SNAKE_SIZE + 2];

                try {
                    for (int i = 0; i < snake.size(); i++) {
                        cellsBoard[snake.get(i).x + 1][snake.get(i).y + 1] = true;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Out of bounds at thread start");
                } catch (NullPointerException e) {
                    System.out.println("Nullpointer at thread start");
                }

                try {
                    Thread.sleep(30);
                    if(snake.get(snake.size()-1).equals(apple.get(0))){
                        snakeLenght++;
                        apple.clear();
                        apple.add(generateApple());
                    }

                    move();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        timer.start();
    }

    private Point generateApple(){
        int x = (int)(Math.random()*gameSceneWidth/SNAKE_SIZE);
        int y = (int)(Math.random()*gameSceneHeight/SNAKE_SIZE);
        return new Point(x,y);
    }

    public void move() {
        gc.clearRect(0, 0, getWidth(), getHeight()); //clear canvas after each generation
        if(lastDirection == KeyCode.DOWN) {
            if(snake.size() >= snakeLenght){
                snake.remove(0);
            }
            posY += 1;
            snake.add(new Point(posX,posY));
            requestLayout();

        }else if(lastDirection == KeyCode.UP) {
            if(snake.size() >= snakeLenght){
                snake.remove(0);
            }
            posY -= 1;
            snake.add(new Point(posX,posY));
            requestLayout();

        }else if(lastDirection == KeyCode.LEFT) {
            if(snake.size() >= snakeLenght){
                snake.remove(0);
            }
            posX -= 1;
            snake.add(new Point(posX,posY));
            requestLayout();

        }else if(lastDirection == KeyCode.RIGHT) {
            if(snake.size() >= snakeLenght){
                snake.remove(0);
            }
            posX += 1;
            snake.add(new Point(posX,posY));
            requestLayout();
        }
    }

    protected void layoutChildren() {
        super.layoutChildren();
        canvas.setWidth(gameSceneWidth - SNAKE_SIZE + 1);
        canvas.setHeight(gameSceneHeight - SNAKE_SIZE + 1);
        gc.setLineWidth(0.2);


        try {
            //iterate through cell array and set its color, size and shape
            for (Point newPoint : snake) {
                gc.fillRect(SNAKE_SIZE + (SNAKE_SIZE * newPoint.x),
                        SNAKE_SIZE + (SNAKE_SIZE * newPoint.y),
                        SNAKE_SIZE,
                        SNAKE_SIZE);
            }
        } catch (ConcurrentModificationException e) {
        } catch (NullPointerException e) {
        }

        try {
            //iterate through cell array and set its color, size and shape
            for (Point newPoint : apple) {
                gc.fillRect(SNAKE_SIZE + (SNAKE_SIZE * newPoint.x),
                        SNAKE_SIZE + (SNAKE_SIZE * newPoint.y),
                        SNAKE_SIZE,
                        SNAKE_SIZE);
            }
        } catch (ConcurrentModificationException e) {
        } catch (NullPointerException e) {
        }

        /*this block is responsible for painting grid,
         grid is drawn regardless of cell*/
        try {
            //create lines horizontally
            for (int i = 0; i <= gameSceneWidth / SNAKE_SIZE; i++) {
                gc.strokeLine(((i * SNAKE_SIZE) + SNAKE_SIZE),
                        SNAKE_SIZE,
                        (i * SNAKE_SIZE) + SNAKE_SIZE,
                        SNAKE_SIZE + (SNAKE_SIZE * gameSceneHeight / SNAKE_SIZE));
            }

            //create lines vertically
            for (int i = 0; i <= gameSceneHeight / SNAKE_SIZE; i++) {
                gc.strokeLine(SNAKE_SIZE,
                        ((i * SNAKE_SIZE) + SNAKE_SIZE),
                        SNAKE_SIZE * (gameSceneWidth / SNAKE_SIZE),
                        ((i * SNAKE_SIZE) + SNAKE_SIZE));
            }

        } catch (NullPointerException e) {
        }
    }
}
