package com.example.asynchronousprogrammingone;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import java.util.LinkedList;
import java.util.Queue;

public class Main extends Application {
    private Buffer buffer;
    private Queue<Thread> producerThreads;
    private Queue<Thread> consumerThreads;
    private boolean running;
    private Rectangle bufferRect;
    private Text bufferText;
    private Rectangle producerRect;
    private Text producerText;
    private Rectangle consumerRect;
    private Text consumerText;
    private String producerName = "Производитель";
    private String consumerName = "Потребитель";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SHOP");

        buffer = new Buffer(5);
        producerThreads = new LinkedList<>();
        consumerThreads = new LinkedList();
        running = false;

        VBox root = new VBox(10);
        HBox buttons = new HBox(10);

        Button startProducersButton = new Button("Запустить производителей");
        Button stopProducersButton = new Button("Остановить производителей");
        Button startConsumersButton = new Button("Запустить потребителей");
        Button stopConsumersButton = new Button("Остановить потребителей");

        startProducersButton.setOnAction(e -> startProducers());
        stopProducersButton.setOnAction(e -> stopProducers());
        startConsumersButton.setOnAction(e -> startConsumers());
        stopConsumersButton.setOnAction(e -> stopConsumers());

        buttons.getChildren().addAll(startProducersButton, stopProducersButton, startConsumersButton, stopConsumersButton);

        bufferRect = new Rectangle(200, 20);
        bufferText = new Text("Buffer");
        bufferText.setFill(Color.BLACK);

        producerRect = new Rectangle(50, 20);
        producerRect.setFill(Color.RED);
        producerText = new Text(producerName + " ожидает");
        producerText.setFill(Color.BLACK);

        consumerRect = new Rectangle(50, 20);
        consumerRect.setFill(Color.RED);
        consumerText = new Text(consumerName + " ожидает");
        consumerText.setFill(Color.BLACK);

        VBox bufferView = new VBox(10);
        bufferView.getChildren().addAll(bufferText, bufferRect, producerText, producerRect, consumerText, consumerRect);

        root.getChildren().addAll(buttons, bufferView);
        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.show();
    }

    private void startProducers() {
        running = true;
        Thread producer = new Thread(() -> {
            while (running) {
                try {
                    int item = (int) (Math.random() * 100);
                    buffer.produce(item);
                    updateBufferView(producerText, producerName, "добавляет товар");
                    Thread.sleep((long) (Math.random() * 1000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        producer.start();
        producerThreads.add(producer);
        producerName += " " + producerThreads.size();
        updateBufferView(producerText, producerName, "ожидает");
    }

    private void stopProducers() {
        running = false;
        for (Thread producerThread : producerThreads) {
            producerThread.interrupt();
        }
        producerThreads.clear();
        updateBufferView(producerText, producerName, "ожидает");
    }

    private void startConsumers() {
        Thread consumer = new Thread(() -> {
            while (true) {
                try {
                    int item = buffer.consume();
                    updateBufferView(consumerText, consumerName, "покупает товар");
                    Thread.sleep((long) (Math.random() * 1000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        consumer.start();
        consumerThreads.add(consumer);
        consumerName += " " + consumerThreads.size();
        updateBufferView(consumerText, consumerName, "ожидает");
    }

    private void stopConsumers() {
        for (Thread consumerThread : consumerThreads) {
            consumerThread.interrupt();
        }
        consumerThreads.clear();
        updateBufferView(consumerText, consumerName, "ожидает");
    }

    private void updateBufferView(Text text, String name, String message) {
        Platform.runLater(() -> {
            synchronized (buffer) {
                bufferRect.setWidth(buffer.size() * 40);
                bufferText.setText("Buffer (Размер: " + buffer.size() + ")");

                // Производители
                if (buffer.size() < 5) {
                    producerRect.setFill(Color.GREEN); // Зеленый, если активен
                } else {
                    producerRect.setFill(Color.RED); // Красный, если ожидает
                }

                // Потребители
                if (buffer.size() > 0) {
                    consumerRect.setFill(Color.GREEN); // Зеленый, если активен
                } else {
                    consumerRect.setFill(Color.RED); // Красный, если ожидает
                }

                text.setText(name + " " + message);
            }
        });
    }

    private static class Buffer {
        private final int capacity;
        private final Queue<Integer> buffer = new LinkedList<>();

        public Buffer(int capacity) {
            this.capacity = capacity;
        }

        public synchronized void produce(int item) throws InterruptedException {
            while (buffer.size() >= capacity) {
                wait();
            }

            buffer.offer(item);
            notifyAll();
        }

        public synchronized int consume() throws InterruptedException {
            while (buffer.isEmpty()) {
                wait();
            }

            int item = buffer.poll();
            notifyAll();
            return item;
        }

        public synchronized int size() {
            return buffer.size();
        }
    }
}
