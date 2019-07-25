package com.consoledeployserver.util;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * 线程池测试
 */
@Slf4j
public class ExecutorTest {


    public static void main(String[] args) {
//        t1();
//        t2();
//        t3();
        t4();
    }

    private static void t4() {
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(20);
        BlockingQueue<Runnable> blockingQueue2 = new ArrayBlockingQueue<>(1);
        BlockingQueue<Runnable> blockingQueue3 = new ArrayBlockingQueue<>(2);
        BlockingQueue<Runnable> blockingQueue4 = new LinkedBlockingQueue<Runnable>();

        Queue<Runnable> queue = new LinkedList<Runnable>();

//        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 5, 0, TimeUnit.MILLISECONDS, blockingQueue2);

        // new SynchronousQueue<>() 进来的任务不会放到队列中，超过core线程，直接生成新的线程，直到超过max，抛出RejectedExecutionException异常
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 5, 0, TimeUnit.MILLISECONDS, new SynchronousQueue<>());

        for (int i = 1; i <= 7; i++){
            try {
                threadPoolExecutor.execute(() -> {
                    System.out.println(Thread.currentThread().getName() + "正在运行....");
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }catch (RejectedExecutionException e){
                System.out.println("拒绝该任务" + i);
            }
        }

        threadPoolExecutor.shutdown();
    }

    private static void t3() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Future<String>> resultList = new ArrayList<>();

        //创建10个任务并执行
        for (int i = 0; i < 10; i++){
            //使用ExecutorService执行Callable类型的任务，并将结果保存在future变量中
            Future<String> future = executorService.submit(new TaskWithResult(i));
            //将任务执行结果存储到List中
            resultList.add(future);
        }

        //遍历任务的结果
        for (Future<String> fs : resultList){
            try{
                while(!fs.isDone());//Future返回如果没有完成，则一直循环等待，直到Future返回完成
                System.out.println(fs.get());     //打印各个线程（任务）执行的结果
            }catch(InterruptedException e){
                e.printStackTrace();
            }catch(ExecutionException e){
                e.printStackTrace();
            }finally{
                //启动一次顺序关闭，执行以前提交的任务，但不接受新任务
                executorService.shutdown();
            }
        }
    }

    private static void t2() {
        ExecutorService executorService3 = Executors.newCachedThreadPool();
      ExecutorService executorService2 = Executors.newFixedThreadPool(5);
      ExecutorService executorService = Executors.newSingleThreadExecutor();    // 单线程线程池，其他任务放入队列中
        for (int i = 0; i < 5; i++){
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName() + "线程被调用了。");
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            System.out.println("************* a" + i + " *************");
        }
        executorService.shutdown();
    }

    private static void t1() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);
        log.info("开始执行...");
        scheduledExecutorService.schedule(
                () -> {
                    log.info("test...");
                },
                3,
                TimeUnit.SECONDS);

        log.info("线程池是否关闭：" + scheduledExecutorService.isTerminated());
        scheduledExecutorService.shutdown();
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("线程池是否关闭：" + scheduledExecutorService.isTerminated());
        if (!scheduledExecutorService.isTerminated()){
            // 等待上次任务执行完成如果没有超过3秒等待到3秒，超过了3秒就立即开始下一次的任务，不用再等
            scheduledExecutorService.scheduleAtFixedRate(
                    () -> {
                        log.info("test2...");
                        try {
                            log.info("睡眠中....");
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    },
                    3,
                    3,
                    TimeUnit.SECONDS);

            // 等待上次任务执行完成再等待3秒才开始下一次的任务
            scheduledExecutorService.scheduleWithFixedDelay(
                    () -> {
                        log.info("test3...");
                        try {
                            log.info("睡眠中....");
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    },
                    3,
                    3,
                    TimeUnit.SECONDS);
        }
    }
}

class TaskWithResult implements Callable<String>{
    private int id;

    public TaskWithResult(int id){
        this.id = id;
    }

    /**
     * 任务的具体过程，一旦任务传给ExecutorService的submit方法，
     * 则该方法自动在一个线程上执行
     */
    public String call() throws Exception {
        System.out.println("call()方法被自动调用！！！    " + Thread.currentThread().getName());
        //该返回结果将被Future的get方法得到
        return "call()方法被自动调用，任务返回的结果是：" + id + "    " + Thread.currentThread().getName();
    }
}