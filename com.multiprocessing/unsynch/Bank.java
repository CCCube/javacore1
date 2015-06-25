package unsynch;

/**
 * A bank with a number of bank accounts.
 * @version 1.30 2004-08-01
 * @author Cay Horstmann
 */
public class Bank
{
   private final double[] accounts;  //账户数量

   /**
    * Constructs the bank.
    * @param n the number of accounts
    * @param initialBalance the initial balance for each account
    */
   public Bank(int n, double initialBalance)
   {
      accounts = new double[n];
      for (int i = 0; i < accounts.length; i++)
         accounts[i] = initialBalance;  //每个账户都赋予了同样的初始金额
   }

   /**
    * Transfers money from one account to another.
    * @param from the account to transfer from
    * @param to the account to transfer to
    * @param amount the amount to transfer
    */
   public void transfer(int from, int to, double amount)  //选中转出账户和转入账户，金额
   {
      if (accounts[from] < amount) return;  //转出账户没钱啦，直接return
      System.out.print(Thread.currentThread());
      accounts[from] -= amount;
      System.out.printf(" %10.2f from %d to %d", amount, from, to);
      
      // 这里并发时是会出现bug的，被另一个线程抢占了CPU，导致数据不一致现象发生
      // 可以看到线程并发时，金额总数可能不为10000
      
      accounts[to] += amount;
      System.out.printf(" Total Balance: %10.2f%n", getTotalBalance());
   }

   /**
    * Gets the sum of all account balances.
    * @return the total balance
    */
   public double getTotalBalance()  //并发访问为对bank数组上锁，也有bug
   {
      double sum = 0;

      for (double a : accounts)
         sum += a;

      return sum;
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
