package com.xiaohunao.heaven_destiny_moment.common.automation;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AutomationThreadManager {
    private static final AutomationThreadManager INSTANCE = new AutomationThreadManager();

    private static class AutomationThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        
        AutomationThreadFactory(String prefix) {
            namePrefix = prefix;
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            t.setDaemon(true); // 设置为守护线程，不阻止JVM退出
            return t;
        }
    }
    
    // 创建一个有界线程池，避免创建过多线程
    private final ExecutorService triggerExecutor;
    
    // 用于存储待处理的任务，以便在必要时同步执行
    private final ConcurrentLinkedQueue<Runnable> pendingTasks = new ConcurrentLinkedQueue<>();
    
    private AutomationThreadManager() {
        int corePoolSize = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        triggerExecutor = new ThreadPoolExecutor(
            corePoolSize,                    // 核心线程数
            corePoolSize,                    // 最大线程数
            60L, TimeUnit.SECONDS,           // 空闲线程存活时间
            new LinkedBlockingQueue<>(1000),
            new AutomationThreadFactory("Automation-Worker-"),
            new ThreadPoolExecutor.CallerRunsPolicy() // 如果队列满了，在调用者线程执行
        );
    }
    
    public static AutomationThreadManager getInstance() {
        return INSTANCE;
    }
    
//    // 关闭线程池的方法，应在mod卸载时调用
//    public void shutdown() {
//        triggerExecutor.shutdown();
//        try {
//            if (!triggerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
//                triggerExecutor.shutdownNow();
//            }
//        } catch (InterruptedException e) {
//            triggerExecutor.shutdownNow();
//            Thread.currentThread().interrupt();
//        }
//    }
    
    // 提交任务到线程池
    public void submitTask(Runnable task) {
        triggerExecutor.submit(task);
    }
    
    // 在主线程执行所有待处理的任务
    public void executePendingTasks() {
        Runnable task;
        while ((task = pendingTasks.poll()) != null) {
            task.run();
        }
    }
    
    // 添加需要在主线程执行的任务
    public void addPendingTask(Runnable task) {
        pendingTasks.offer(task);
    }
}