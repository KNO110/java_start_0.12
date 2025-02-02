import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Выберите задание (1, 2, 3, 4):");
        int choice = scanner.nextInt();
        scanner.nextLine();
        switch(choice) {
            case 1: task1(); break;
            case 2: task2(); break;
            case 3: task3(); break;
            case 4: task4(); break;
            default: System.out.println("Неверный выбор.");
        }
    }
    // 1
    static void task1() throws Exception {
        final int size = 10;
        int[] array = new int[size];
        CountDownLatch latch = new CountDownLatch(1);
        int[] sumHolder = new int[1];
        double[] avgHolder = new double[1];
        Thread fillThread = new Thread(() -> {
            Random rand = new Random();
            for (int i = 0; i < size; i++) {
                array[i] = rand.nextInt(100);
            }
            latch.countDown();
        });
        Thread sumThread = new Thread(() -> {
            try { latch.await(); } catch (InterruptedException e) {}
            int sum = 0;
            for (int num : array) sum += num;
            sumHolder[0] = sum;
        });
        Thread avgThread = new Thread(() -> {
            try { latch.await(); } catch (InterruptedException e) {}
            int sum = 0;
            for (int num : array) sum += num;
            avgHolder[0] = (double) sum / size;
        });
        fillThread.start();
        sumThread.start();
        avgThread.start();
        fillThread.join();
        sumThread.join();
        avgThread.join();
        System.out.println("Массив: " + Arrays.toString(array));
        System.out.println("Сумма элементов массива: " + sumHolder[0]);
        System.out.println("Среднее арифметическое: " + avgHolder[0]);
    }
    // 2
    static void task2() throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите путь к файлу:");
        String filePath = scanner.nextLine();
        File file = new File(filePath);
        CountDownLatch latch = new CountDownLatch(1);
        final int countNumbers = 10;
        Thread fillFileThread = new Thread(() -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                Random rand = new Random();
                for (int i = 0; i < countNumbers; i++) {
                    int num = rand.nextInt(10) + 1;
                    writer.write(String.valueOf(num));
                    writer.newLine();
                }
            } catch (IOException e) {}
            latch.countDown();
        });
        final int[] primeCountHolder = new int[1];
        final int[] factorialCountHolder = new int[1];
        Thread primeThread = new Thread(() -> {
            try { latch.await(); } catch (InterruptedException e) {}
            List<Integer> numbers = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        numbers.add(Integer.parseInt(line.trim()));
                    } catch (NumberFormatException ex) {}
                }
            } catch (IOException e) {}
            List<Integer> primes = new ArrayList<>();
            for (int num : numbers) {
                if (isPrime(num)) primes.add(num);
            }
            primeCountHolder[0] = primes.size();
            File primeFile = new File(file.getParent(), "primes.txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(primeFile))) {
                for (int prime : primes) {
                    writer.write(String.valueOf(prime));
                    writer.newLine();
                }
            } catch (IOException e) {}
        });
        Thread factorialThread = new Thread(() -> {
            try { latch.await(); } catch (InterruptedException e) {}
            List<Integer> numbers = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        numbers.add(Integer.parseInt(line.trim()));
                    } catch (NumberFormatException ex) {}
                }
            } catch (IOException e) {}
            List<String> factorials = new ArrayList<>();
            for (int num : numbers) {
                long fact = factorial(num);
                factorials.add(num + "! = " + fact);
            }
            factorialCountHolder[0] = numbers.size();
            File factorialFile = new File(file.getParent(), "factorials.txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(factorialFile))) {
                for (String s : factorials) {
                    writer.write(s);
                    writer.newLine();
                }
            } catch (IOException e) {}
        });
        fillFileThread.start();
        primeThread.start();
        factorialThread.start();
        fillFileThread.join();
        primeThread.join();
        factorialThread.join();
        System.out.println("Файл заполнен " + countNumbers + " числами.");
        System.out.println("Найдено " + primeCountHolder[0] + " простых чисел. Результаты записаны в файл primes.txt");
        System.out.println("Вычислены факториалы для " + factorialCountHolder[0] + " чисел. Результаты записаны в файл factorials.txt");
    }
    static boolean isPrime(int n) {
        if(n < 2) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if(n % i == 0) return false;
        }
        return true;
    }
    static long factorial(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++) result *= i;
        return result;
    }
    // 3
    static void task3() throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите путь к существующей директории:");
        String sourceDirPath = scanner.nextLine();
        System.out.println("Введите путь к новой директории:");
        String destDirPath = scanner.nextLine();
        File sourceDir = new File(sourceDirPath);
        File destDir = new File(destDirPath);
        DirectoryCopier copier = new DirectoryCopier(sourceDir, destDir);
        Thread copyThread = new Thread(copier);
        copyThread.start();
        copyThread.join();
        System.out.println("Копирование завершено. Скопировано файлов: " + copier.filesCopied + ", директорий: " + copier.dirsCopied);
    }
    static class DirectoryCopier implements Runnable {
        File source;
        File dest;
        int filesCopied = 0;
        int dirsCopied = 0;
        DirectoryCopier(File source, File dest) {
            this.source = source;
            this.dest = dest;
        }
        public void run() {
            copyDir(source, dest);
        }
        void copyDir(File src, File dst) {
            if(src.isDirectory()) {
                if(!dst.exists()) {
                    dst.mkdirs();
                    dirsCopied++;
                }
                File[] files = src.listFiles();
                if(files != null) {
                    for(File file : files) {
                        copyDir(file, new File(dst, file.getName()));
                    }
                }
            } else {
                try {
                    Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    filesCopied++;
                } catch (IOException e) {}
            }
        }
    }
    // 4
    static void task4() throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите путь к директории для поиска файлов:");
        String dirPath = scanner.nextLine();
        System.out.println("Введите слово для поиска:");
        String searchWord = scanner.nextLine();
        File dir = new File(dirPath);
        File mergedFile = new File("merged.txt");
        FileMerger merger = new FileMerger(dir, searchWord, mergedFile);
        Thread mergerThread = new Thread(merger);
        ForbiddenWordRemover removerRunnable = new ForbiddenWordRemover(mergedFile, new File("forbidden.txt"), new File("merged_cleaned.txt"), mergerThread);
        Thread removerThread = new Thread(removerRunnable);
        mergerThread.start();
        removerThread.start();
        mergerThread.join();
        removerThread.join();
        System.out.println("Найдено и объединено файлов: " + merger.filesMerged);
        System.out.println("Удалено " + removerRunnable.totalReplacements + " запрещенных слов. Результат записан в файл merged_cleaned.txt");
    }
    static class FileMerger implements Runnable {
        File dir;
        String word;
        File mergedFile;
        int filesMerged = 0;
        FileMerger(File dir, String word, File mergedFile) {
            this.dir = dir;
            this.word = word;
            this.mergedFile = mergedFile;
        }
        public void run() {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(mergedFile))) {
                mergeFiles(dir, writer);
            } catch (IOException e) {}
        }
        void mergeFiles(File directory, BufferedWriter writer) throws IOException {
            File[] files = directory.listFiles();
            if(files != null) {
                for(File file : files) {
                    if(file.isDirectory()) {
                        mergeFiles(file, writer);
                    } else {
                        if(containsWord(file, word)) {
                            filesMerged++;
                            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                                String line;
                                while((line = reader.readLine()) != null) {
                                    writer.write(line);
                                    writer.newLine();
                                }
                            } catch (IOException e) {}
                        }
                    }
                }
            }
        }
        boolean containsWord(File file, String word) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while((line = reader.readLine()) != null) {
                    if(line.contains(word)) return true;
                }
            } catch (IOException e) {}
            return false;
        }
    }
    static class ForbiddenWordRemover implements Runnable {
        File sourceFile;
        File forbiddenFile;
        File outputFile;
        Thread waitFor;
        int totalReplacements = 0;
        ForbiddenWordRemover(File sourceFile, File forbiddenFile, File outputFile, Thread waitFor) {
            this.sourceFile = sourceFile;
            this.forbiddenFile = forbiddenFile;
            this.outputFile = outputFile;
            this.waitFor = waitFor;
        }
        public void run() {
            if(waitFor != null) {
                try { waitFor.join(); } catch (InterruptedException e) {}
            }
            List<String> forbiddenWords = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(forbiddenFile))) {
                String line;
                while((line = reader.readLine()) != null) {
                    forbiddenWords.add(line.trim());
                }
            } catch (IOException e) {}
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile))) {
                String line;
                while((line = reader.readLine()) != null) {
                    content.append(line).append(System.lineSeparator());
                }
            } catch (IOException e) {}
            String modified = content.toString();
            for(String word : forbiddenWords) {
                int count = 0;
                while(modified.contains(word)) {
                    modified = modified.replace(word, "");
                    count++;
                }
                totalReplacements += count;
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                writer.write(modified);
            } catch (IOException e) {}
        }
    }
}
