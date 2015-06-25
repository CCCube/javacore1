package blockingQueue;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * @version 1.01 2012-01-26
 * @author Cay Horstmann
 */
public class BlockingQueueTest
{
   public static void main(String[] args)
   {
      Scanner in = new Scanner(System.in);
      System.out.print("Enter base directory (e.g. /usr/local/jdk1.6.0/src): ");
      String directory = in.nextLine();
      System.out.print("Enter keyword (e.g. volatile): ");
      String keyword = in.nextLine();

      final int FILE_QUEUE_SIZE = 10;   //阻塞队列的长度为10
      final int SEARCH_THREADS = 100;   // 搜索线程的数量为100

      BlockingQueue<File> queue = new ArrayBlockingQueue<>(FILE_QUEUE_SIZE);

      FileEnumerationTask enumerator = new FileEnumerationTask(queue, new File(directory));
      new Thread(enumerator).start();  // 生产者线程枚举搜索目录下所有的文件，将每个file对象插入到blockingQueue中
      // 新建100个搜索线程来进行搜索
      for (int i = 1; i <= SEARCH_THREADS; i++)
         new Thread(new SearchTask(queue, keyword)).start();
   }
}

/**
 * This task enumerates all files in a directory and its subdirectories.
 */
class FileEnumerationTask implements Runnable
{
   public static File DUMMY = new File("");  //虚拟file对象，用于终止搜索任务
   private BlockingQueue<File> queue;
   private File startingDirectory;

   /**
    * Constructs a FileEnumerationTask.
    * @param queue the blocking queue to which the enumerated files are added
    * @param startingDirectory the directory in which to start the enumeration
    */
   public FileEnumerationTask(BlockingQueue<File> queue, File startingDirectory)
   {
      this.queue = queue;
      this.startingDirectory = startingDirectory;
   }

   public void run()
   {
      try
      {
         enumerate(startingDirectory);
         queue.put(DUMMY);   //往阻塞队列加入虚拟file对象，用于终止搜索任务
      }
      catch (InterruptedException e)
      {
      }
   }

   /**
    * Recursively enumerates all files in a given directory and its subdirectories.
    * @param directory the directory in which to start
    */
   public void enumerate(File directory) throws InterruptedException
   {
      File[] files = directory.listFiles();
      for (File file : files)
      {
         if (file.isDirectory()) enumerate(file);  //递归操作目录
         else queue.put(file);
      }
   }
}

/**
 * This task searches files for a given keyword.
 */
class SearchTask implements Runnable
{
   private BlockingQueue<File> queue;
   private String keyword;

   /**
    * Constructs a SearchTask.
    * @param queue the queue from which to take files
    * @param keyword the keyword to look for
    */
   public SearchTask(BlockingQueue<File> queue, String keyword)
   {
      this.queue = queue;  //这儿的queue与生产者的queue为同一个对象
      this.keyword = keyword;
   }

   public void run()
   {
      try
      {
         boolean done = false;
         while (!done)
         {
            File file = queue.take();
            if (file == FileEnumerationTask.DUMMY)
            {
               queue.put(file);
               done = true;  //检测到虚拟file对象，终止搜索任务
            }
            else search(file);
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      catch (InterruptedException e)
      {
      }
   }

   /**
    * Searches a file for a given keyword and prints all matching lines.
    * @param file the file to search
    */
   public void search(File file) throws IOException
   {
      try (Scanner in = new Scanner(file))
      {
         int lineNumber = 0;
         while (in.hasNextLine())
         {
            lineNumber++;
            String line = in.nextLine();
            if (line.contains(keyword)) 
               System.out.printf("%s:%d:%s%n", file.getPath(), lineNumber, line);
         }
      }
   }
}
