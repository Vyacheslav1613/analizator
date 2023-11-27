import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final int MAX_QUEUE_SIZE = 100;
    private static final int MAX_LETTER_SIZE = 100000;
    private static final int MAX_STRING_SIZE = 10000;
    private static final BlockingQueue<String> queueA = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);
    private static final BlockingQueue<String> queueB = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);
    private static final BlockingQueue<String> queueC = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);
    private static final AtomicInteger countA = new AtomicInteger(0);
    private static final AtomicInteger countB = new AtomicInteger(0);
    private static final AtomicInteger countC = new AtomicInteger(0);
    private static volatile boolean isRunning = true;

    public static void main(String[] args) throws InterruptedException {
        Thread generatorThread = new Thread(() -> {
            for (int i = 0; i < MAX_STRING_SIZE; i++) {
                String text = generateText("abc", MAX_LETTER_SIZE);
                if (text.contains("a")) {
                    try {
                        queueA.put(text);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (text.contains("b")) {
                    try {
                        queueB.put(text);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (text.contains("c")) {
                    try {
                        queueC.put(text);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            isRunning = false;
        });

        Thread analyzTextA = new Thread(() -> {
            while (isRunning || !queueA.isEmpty()) {
                try {
                    count(queueA.take(), 'a');
                } catch (InterruptedException e) {
                    return;
                }
            }
        });

        Thread analyzTextB = new Thread(() -> {
            while (isRunning || !queueB.isEmpty()) {
                try {
                    count(queueB.take(), 'b');
                } catch (InterruptedException e) {
                    return;
                }
            }
        });

        Thread analyzTextC = new Thread(() -> {
            while (isRunning || !queueC.isEmpty()) {
                try {
                    count(queueC.take(), 'c');
                } catch (InterruptedException e) {
                    return;
                }
            }
        });

        generatorThread.start();
        analyzTextA.start();
        analyzTextB.start();
        analyzTextC.start();

        generatorThread.join();
        analyzTextA.interrupt();
        analyzTextB.interrupt();
        analyzTextC.interrupt();

        analyzTextA.join();
        analyzTextB.join();
        analyzTextC.join();

        System.out.println("Самое большое количество букв А: " + countA);
        System.out.println("Самое большое количество букв B: " + countB);
        System.out.println("Самое большое количество букв C: " + countC);
    }

    private static void count(String text, char letter) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == letter) {
                count++;
            }
        }
        if (letter == 'a') {
            if (count > countA.get()) {
                countA.getAndAdd(count);
            }
        } else if (letter == 'b') {
            if (count > countB.get()) {
                countB.getAndAdd(count);
            }
        } else if (letter == 'c') {
            if (count > countC.get()) {
                countC.getAndAdd(count);
            }
        }
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
}