import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.awt.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Mateusz on 29.03.2017.
 */
public class GameScene extends Pane {
    private final static int SNAKE_SIZE = 10;
    private static int ANIMATION_SPEED = 30;
    boolean crash = false;
    private int snakeLength = 1;
    private int gameSceneWidth;
    private int gameSceneHeight;
    private AnimationTimer timer;
    private Canvas canvas = new Canvas(gameSceneWidth, gameSceneHeight);
    private GraphicsContext gc = canvas.getGraphicsContext2D();
    private ArrayList<Point> snake;
    private ArrayList<Point> apple;
    private KeyCode lastDirection;
    private boolean dead = false;
    private boolean isRunning = false;
    private int posX = 10;
    private int posY = 10;

    public GameScene() {
        setInitialState();
        getChildren().add(canvas);

        timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= ANIMATION_SPEED * 1_000_000) {
                    requestLayout();
                    updateScene();
                    lastUpdate = now;
                    isRunning = true;
                }
            }
        };
    }

    public AnimationTimer getTimer() {
        return timer;
    }

    public void setInitialState() {
        if (isRunning) {
            timer.stop();
            isRunning = false;
        }
        isRunning = false;

        posX = 25;
        posY = 25;

        gameSceneHeight = 600;
        gameSceneWidth = 800;
        canvas.setWidth(gameSceneWidth);
        canvas.setHeight(gameSceneHeight);

        snake = new ArrayList<>(snakeLength);
        snake.add(new Point(posX, posY));
        apple = new ArrayList<>(0);
        apple.add(generateApple());

        dead = false;
        ANIMATION_SPEED = 30;
        snakeLength = 100;
    }

    public KeyCode getLastDirection() {
        return lastDirection;
    }

    public void setLastDirection(KeyCode lastDirection) {
        this.lastDirection = lastDirection;
    }

    public void updateScene() {
        if (!dead) {
            boolean[][] cellsBoard = new boolean[gameSceneWidth / SNAKE_SIZE][gameSceneHeight / SNAKE_SIZE];
            try {
                for (int i = 0; i < snake.size(); i++) {
                    cellsBoard[snake.get(i).x + 1][snake.get(i).y + 1] = true;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                timer.stop();
                dead = true;
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Score");
                alert.setHeaderText("You died!");
                alert.setContentText("Your score: " + (snakeLength - 1));
                alert.show();

            } catch (NullPointerException e) {
                System.out.println("Nullpointer at thread start");
            }

            try {
                if (snake.get(snake.size() - 1).equals(apple.get(0))) {
                    snakeLength++;
                    ANIMATION_SPEED--;
                    apple.clear();
                    apple.add(generateApple());
                }
                move();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Point generateApple() {
        int x = (int) (Math.random() * (canvas.getWidth() - SNAKE_SIZE) / SNAKE_SIZE);
        int y = (int) (Math.random() * (canvas.getHeight() - SNAKE_SIZE) / SNAKE_SIZE);
        return new Point(x, y);
    }

    public void resetAll() {
        setInitialState();
        requestLayout();
        crash = false;
        //timer.start();
    }

    private void move() {

        if (!crash) {
            Set<Point> set = new HashSet<>(snake);

            if (set.size() < snake.size()) {
                crash = true;
            }

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
    }

    public void drawCanvasFrame() {
        gc.strokeLine(0, canvas.getHeight(), canvas.getWidth(), canvas.getHeight());
        gc.strokeLine(canvas.getWidth(), 0, canvas.getWidth(), canvas.getHeight());
        gc.strokeLine(0, 0, 0, canvas.getHeight());
        gc.strokeLine(0, 0, canvas.getWidth(), 0);
    }

    private void drawSnake() {
        try {
            gc.setFill(Color.GREEN);
            for (Point newPoint : snake) {
                gc.fillRect(SNAKE_SIZE + (SNAKE_SIZE * newPoint.x),
                        SNAKE_SIZE + (SNAKE_SIZE * newPoint.y),
                        SNAKE_SIZE,
                        SNAKE_SIZE);
            }
        } catch (ConcurrentModificationException e) {
        } catch (NullPointerException e) {
        }
    }

    private void drawApple() {
        try {
            Image img = new Image("img/apple.png");
            for (Point newPoint : apple) {
                gc.drawImage(img, SNAKE_SIZE + ((SNAKE_SIZE * newPoint.x) - 3), SNAKE_SIZE + (SNAKE_SIZE * newPoint.y) - 3);

            }
        } catch (ConcurrentModificationException e) {
        } catch (NullPointerException e) {
        }
    }

    private void drawGrid() {
        try {
            for (int i = 0; i < canvas.getWidth() / SNAKE_SIZE; i++) {
                gc.strokeLine(((i * SNAKE_SIZE) + SNAKE_SIZE),
                        0,
                        (i * SNAKE_SIZE) + SNAKE_SIZE,
                        SNAKE_SIZE + (SNAKE_SIZE * gameSceneHeight / SNAKE_SIZE));
            }

            for (int i = 0; i < canvas.getHeight() / SNAKE_SIZE - 1; i++) {
                gc.strokeLine(0,
                        ((i * SNAKE_SIZE) + SNAKE_SIZE),
                        SNAKE_SIZE * (gameSceneWidth / SNAKE_SIZE),
                        ((i * SNAKE_SIZE) + SNAKE_SIZE));
            }

        } catch (NullPointerException e) {
        }
    }

    protected void layoutChildren() {
        super.layoutChildren();
        gc.clearRect(0, 0, getWidth(), getHeight());
        gc.setLineWidth(0.2);

        drawCanvasFrame();
        drawSnake();
        drawApple();
        drawGrid();
    }
}
