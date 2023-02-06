import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Account {
    private int balance = 0;
    private Lock lock = new ReentrantLock(true);

    public Account(){}
    public Account(int initialBalance) {
        balance = initialBalance;
        lock = new ReentrantLock(true);
    }

    public void deposit(int amount){
        balance += amount;
    }
    public void safeDeposit(int amount) {
        lock.lock();
        try {
            balance += amount;
        } finally {
            lock.unlock();
        }
    }

    public void withdraw(int amount){
        balance -= amount;
    }
    public void safeWithdraw(int amount) {
        lock.lock();
        try {
            balance -= amount;
        } finally {
            lock.unlock();
        }
    }

    public int getBalance() {
        return balance;
    }
}

class LatchExample {
    static int finalBalance = 100;
    public static void main(String[] args) {
        Account account = new Account(100);

        CountDownLatch latch = new CountDownLatch(2);

        Runnable depositTask = () -> {
            account.safeDeposit(50);
            System.out.println("Deposit task finished");
            latch.countDown();
        };

        Runnable withdrawTask = () -> {
            account.safeWithdraw(150);
            System.out.println("Withdraw task finished");
            latch.countDown();
        };

        new Thread(depositTask).start();
        new Thread(withdrawTask).start();

        try {
            latch.await();
            finalBalance = account.getBalance();
            System.out.println("Final balance: " + finalBalance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class BarrierExample {
    static int finalBalance = 100;
    public static void main(String[] args) {
        Account account = new Account(100);

        CyclicBarrier barrier = new CyclicBarrier(3, () -> {
            System.out.println("Both transactions finished!");
        });

        Runnable depositTask = () -> {
            account.safeDeposit(50);
            System.out.println("Deposit task finished");
            try {
                barrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        Runnable withdrawTask = () -> {
            account.safeWithdraw(150);
            System.out.println("Withdraw task finished");
            try {
                barrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        new Thread(depositTask).start();
        new Thread(withdrawTask).start();

        try {
            barrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finalBalance = account.getBalance();
        System.out.println("Final balance: " + finalBalance);
    }
}