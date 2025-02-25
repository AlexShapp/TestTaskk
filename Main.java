import java.io.*;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final String FILE_NAME = "numbers.txt";
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        Thread evenWriter = new Thread(new EvenNumberWriter());
        Thread oddWriter = new Thread(new OddNumberWriter());
        Thread reader = new Thread(new FileReaderTask());

        evenWriter.start();
        oddWriter.start();
        reader.start();

        // Останавливаем потоки через 5 секунд для демонстрации
        try {
            Thread.sleep(5000);
            evenWriter.interrupt();
            oddWriter.interrupt();
            reader.interrupt();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Главный поток был прерван", e);
            Thread.currentThread().interrupt();
        }
    }

    static class EvenNumberWriter implements Runnable {
        private final Random random = new Random();

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                int number = random.nextInt(50) * 2;
                writeToFile(number);
                sleep();
            }
            logger.info("Четный поток завершен.");
        }
    }

    static class OddNumberWriter implements Runnable {
        private final Random random = new Random();

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                int number = random.nextInt(50) * 2 + 1;
                writeToFile(number);
                sleep();
            }
            logger.info("Нечетный поток завершен.");
        }
    }

    static class FileReaderTask implements Runnable {
        private long lastReadPosition = 0;

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                lock.lock();
                try (RandomAccessFile raf = new RandomAccessFile(FILE_NAME, "r")) {
                    raf.seek(lastReadPosition);
                    String line;
                    while ((line = raf.readLine()) != null) {
                        System.out.println("Прочитано: " + line);
                        lastReadPosition = raf.getFilePointer();
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Ошибка при чтении файла", e);
                } finally {
                    lock.unlock();
                }
                sleep();
            }
            logger.info("Читающий поток завершен.");
        }
    }

    private static void writeToFile(int number) {
        lock.lock();
        try (FileWriter writer = new FileWriter(FILE_NAME, true)) {
            writer.write(number + "\n");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка при записи в файл", e);
        } finally {
            lock.unlock();
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Поток был прерван во время сна", e);
            Thread.currentThread().interrupt();
        }
    }
}
