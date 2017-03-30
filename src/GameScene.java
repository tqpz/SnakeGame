import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

import java.awt.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 * Created by Mateusz on 29.03.2017.
 */
public class GameScene extends Pane {
    private final static int SNAKE_SIZE = 10;
    private static int ANIMATION_SPEED = 20;
    private int snakeLength = 1;
    private int gameSceneWidth;
    private int gameSceneHeight;
    private Canvas canvas = new Canvas(gameSceneWidth, gameSceneHeight);
    private GraphicsContext gc = canvas.getGraphicsContext2D();
    private ArrayList<Point> snake;
    private ArrayList<Point> apple;
    private KeyCode lastDirection;
    private int posX = 10;
    private int posY = 10;

    public GameScene() {
        gameSceneHeight = 600;
        gameSceneWidth = 800;
        snake = new ArrayList<>(snakeLength);
        snake.add(new Point(posX, posY));
        apple = new ArrayList<>(0);
        apple.add(new Point(generateApple()));
        getChildren().add(canvas);

        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= ANIMATION_SPEED * 1_000_000) {
                    updateScene();
                    lastUpdate = now;
                }
            }
        };
        timer.start();
    }

    public void setLastDirection(KeyCode lastDirection) {
        this.lastDirection = lastDirection;
    }

    public void updateScene() {
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
            //Thread.sleep(ANIMATION_SPEED);
            if (snake.get(snake.size() - 1).equals(apple.get(0))) {
                snakeLength++;
                apple.clear();
                apple.add(generateApple());
            }
            move();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Point generateApple() {
        int x = (int) (Math.random() * gameSceneWidth / SNAKE_SIZE);
        int y = (int) (Math.random() * gameSceneHeight / SNAKE_SIZE);
        return new Point(x, y);
    }

    public void move() {
        gc.clearRect(0, 0, getWidth(), getHeight());
        if (lastDirection == KeyCode.DOWN) {
            if (snake.size() >= snakeLength) {
                snake.remove(0);
            }
            posY += 1;
            snake.add(new Point(posX, posY));
            requestLayout();

        } else if (lastDirection == KeyCode.UP) {
            if (snake.size() >= snakeLength) {
                snake.remove(0);
            }
            posY -= 1;
            snake.add(new Point(posX, posY));
            requestLayout();

        } else if (lastDirection == KeyCode.LEFT) {
            if (snake.size() >= snakeLength) {
                snake.remove(0);
            }
            posX -= 1;
            snake.add(new Point(posX, posY));
            requestLayout();

        } else if (lastDirection == KeyCode.RIGHT) {
            if (snake.size() >= snakeLength) {
                snake.remove(0);
            }
            posX += 1;
            snake.add(new Point(posX, posY));
            requestLayout();
        }
    }

    protected void layoutChildren() {
        super.layoutChildren();
        canvas.setWidth(gameSceneWidth - SNAKE_SIZE + 1);
        canvas.setHeight(gameSceneHeight - SNAKE_SIZE + 1);
        gc.setLineWidth(0.2);

        try {
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
            for (Point newPoint : apple) {
                gc.fillRect(SNAKE_SIZE + (SNAKE_SIZE * newPoint.x),
                        SNAKE_SIZE + (SNAKE_SIZE * newPoint.y),
                        SNAKE_SIZE,
                        SNAKE_SIZE);
            }
        } catch (ConcurrentModificationException e) {
        } catch (NullPointerException e) {
        }

        try {
            for (int i = 0; i <= gameSceneWidth / SNAKE_SIZE; i++) {
                gc.strokeLine(((i * SNAKE_SIZE) + SNAKE_SIZE),
                        SNAKE_SIZE,
                        (i * SNAKE_SIZE) + SNAKE_SIZE,
                        SNAKE_SIZE + (SNAKE_SIZE * gameSceneHeight / SNAKE_SIZE));
            }

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
