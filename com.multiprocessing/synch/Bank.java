package synch;

import java.util.concurrent.locks.*;

/**
 * A bank with a number of bank accounts that uses locks for serializing access.
 * @version 1.30 2004-08-01
 * @author Cay Horstmann
 */
public class Bank
{
   private final double[] accounts;
   private Lock bankLock;  //bankLock 锁对象
   private Condition sufficientFunds; //sufficientFunds 条件对象

   /**
    * Constructs the bank.
    * @param n the number of accounts
    * @param initialBalance the initial balance for each account
    */
   public Bank(int n, double initialBalance)
   {
      accounts = new double[n];
      for (int i = 0; i < accounts.length; i++)
         accounts[i] = initialBalance;
      bankLock = new ReentrantLock();
      //条件对象，也可以对其上锁，用于上锁条件较多情况下，设置不同条件 
      sufficientFunds = bankLock.newCondition();  
   }

   /**
    * Transfers money from one account to another.
    * @param from the account to transfer from
    * @param to the account to transfer to
    * @param amount the amount to transfer
    */
   public void transfer(int from, int to, double amount) throws InterruptedException
   {
      bankLock.lock();  //锁对象上锁，当某个线程获得锁，其它线程无法进入try方法块
      try
      {
         while (accounts[from] < amount)
            sufficientFunds.await();  //条件对象方法 await() 放弃该条件对象锁且需要等待唤醒 
         System.out.print(Thread.currentThread());
         accounts[from] -= amount;
         System.out.printf(" %10.2f from %d to %d", amount, from, to);
         accounts[to] += amount;
         System.out.printf(" Total Balance: %10.2f%n", getTotalBalance());
         //signal()随机选择某个线程唤醒，易死锁  signalAll()唤醒所有等待线程，重新抢占资源
         sufficientFunds.signalAll();
      }
      finally
      {
         bankLock.unlock();  //注意unlock锁对象
      }
   }

   /**
    * Gets the sum of all account balances.
    * @return the total balance
    */
   public double getTotalBalance()
   {
      bankLock.lock();  //同步上锁，返回总金额时不得对bank数组进行修改
      try
      {
         double sum = 0;

         for (double a : accounts)
            sum += a;

         return sum;
      }
      finally
      {
         bankLock.unlock();
      }
   }

   /**
    * Gets the number of accounts in the bank.
    * @return the number of accounts
    */
   public int size()
   {
      return accounts.length;
   }
}
