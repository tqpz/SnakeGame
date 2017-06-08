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

 class GameScene extends Pane {
    private final static int SNAKE_SIZE = 10;
    private final static int GAME_SCENE_WIDTH = 800;
    private final static int GAME_SCENE_HEIGHT = 600;
    private static int animationSpeed;
    private int snakeLength;
    private int lastScore;
    private AnimationTimer timer;
    private Canvas canvas;
    private GraphicsContext gc;
    private ArrayList<Point> snake;
    private ArrayList<Point> apple;
    private KeyCode lastDirection;
    private boolean dead;
    private boolean isRunning;
    private int posX;
    private int posY;

    GameScene() {
        animationSpeed = 70;
        snakeLength = 1;
        canvas = new Canvas(GAME_SCENE_WIDTH, GAME_SCENE_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        dead = false;
        isRunning = false;

        getStyleClass().add("game-scene");

        getChildren().add(canvas);
        isRunning = false;

        posX = (int) (Math.random()*79);
        posY = (int) (Math.random()*59);

        canvas.setWidth(GAME_SCENE_WIDTH);
        canvas.setHeight(GAME_SCENE_HEIGHT);

        snake = new ArrayList<>(snakeLength);
        snake.add(new Point(posX, posY));
        apple = new ArrayList<>(0);
        addApples(7);

        timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= animationSpeed * 1_000_000) {
                    requestLayout();
                    updateScene();
                    lastUpdate = now;
                    isRunning = true;
                }
            }
        };
    }

    private void updateScene() {
        if (!dead) {
            boolean[][] board = new boolean[GAME_SCENE_WIDTH / SNAKE_SIZE][GAME_SCENE_HEIGHT / SNAKE_SIZE];
            try {
                for (int i = 0; i < snake.size(); i++) {
                    board[snake.get(i).x + 1][snake.get(i).y + 1] = true;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                timer.stop();
                showDeadPopup();
            } catch (NullPointerException ignored) {
            }

            try {
                for (int i = 0; i < apple.size(); i++) {
                    if (snake.get(snake.size() - 1).equals(apple.get(i))) {
                        snakeLength += 4;
                        animationSpeed--;
                        apple.remove(i);
                        addApples(1);
                    }
                }
                move();
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    private Point generateApple() {
        int x = (int) (Math.random() * (canvas.getWidth() - SNAKE_SIZE) / SNAKE_SIZE);
        int y = (int) (Math.random() * (canvas.getHeight() - SNAKE_SIZE) / SNAKE_SIZE);
        return new Point(x, y);
    }

    private void addApples(int numof) {
        for (int i = 0; i < numof; i++) {
            apple.add(generateApple());
        }
    }

    public void resetAll() {
        timer.stop();
        setInitialState();
        requestLayout();
        dead = false;
    }

    private void showDeadPopup() {
        dead = true;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        lastScore = (snakeLength - 1) / 4;
        alert.setTitle("Score");
        alert.setHeaderText("You died!");
        alert.setContentText("Your score: " + lastScore);
        alert.show();
    }

    private void move() {
        if (!dead) {
            Set<Point> set = new HashSet<>(snake);

            if (set.size() < snake.size()) {
                showDeadPopup();
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

    private void drawCanvasFrame() {
        gc.strokeLine(0, canvas.getHeight(), canvas.getWidth(), canvas.getHeight());
        gc.strokeLine(canvas.getWidth(), 0, canvas.getWidth(), canvas.getHeight());
        gc.strokeLine(0, 0, 0, canvas.getHeight());
        gc.strokeLine(0, 0, canvas.getWidth(), 0);
    }

    private void drawSnake() {
        try {
            gc.setFill(Color.GREENYELLOW);
            for (Point newPoint : snake) {
                gc.fillRect(SNAKE_SIZE + (SNAKE_SIZE * newPoint.x),
                        SNAKE_SIZE + (SNAKE_SIZE * newPoint.y),
                        SNAKE_SIZE,
                        SNAKE_SIZE);
            }
        } catch (ConcurrentModificationException | NullPointerException ignored) {
        }
    }

    private void drawApple() {
        try {
            Image img = new Image("img/apple.png");
            for (Point newPoint : apple) {
                gc.drawImage(img, SNAKE_SIZE + ((SNAKE_SIZE * newPoint.x) - 3), SNAKE_SIZE + (SNAKE_SIZE * newPoint.y) - 3);

            }
        } catch (ConcurrentModificationException | NullPointerException ignored) {
        }
    }

    private void drawGrid() {
        try {
            for (int i = 0; i < canvas.getWidth() / SNAKE_SIZE; i++) {
                gc.strokeLine(((i * SNAKE_SIZE) + SNAKE_SIZE),
                        0,
                        (i * SNAKE_SIZE) + SNAKE_SIZE,
                        SNAKE_SIZE + (SNAKE_SIZE * GAME_SCENE_HEIGHT / SNAKE_SIZE));
            }

            for (int i = 0; i < canvas.getHeight() / SNAKE_SIZE - 1; i++) {
                gc.strokeLine(0,
                        ((i * SNAKE_SIZE) + SNAKE_SIZE),
                        SNAKE_SIZE * (GAME_SCENE_WIDTH / SNAKE_SIZE),
                        ((i * SNAKE_SIZE) + SNAKE_SIZE));
            }

        } catch (NullPointerException e) {
        }
    }


    private void setInitialState() {
        if (isRunning) {
            timer.stop();
            isRunning = false;
        }
        isRunning = false;

        posX = (int) (Math.random()*79);
        posY = (int) (Math.random()*59);

        snake = new ArrayList<>(snakeLength);
        snake.add(new Point(posX, posY));
        apple = new ArrayList<>(0);
        addApples(7);

        dead = false;
        animationSpeed = 70;
        snakeLength = 1;
    }

    public KeyCode getLastDirection() {
        return lastDirection;
    }

    public void setLastDirection(KeyCode lastDirection) {
        this.lastDirection = lastDirection;
    }

    public AnimationTimer getTimer() {
        return timer;
    }

    public int getLastScore() {
        return lastScore;
    }

    public boolean isDead() {
        return dead;
    }
}
