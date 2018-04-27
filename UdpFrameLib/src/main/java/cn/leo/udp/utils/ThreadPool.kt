package cn.leo.udp.utils

import android.util.Log
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by ww on 2016/11/12
 */
object ThreadPool {
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
    private val CORE_POOL_SIZE = CPU_COUNT + 1
    private val MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1
    private const val KEEP_ALIVE = 5

    private val sThreadFactory = object : ThreadFactory {
        // 线程安全的递加操作
        private val mCount = AtomicInteger(1)

        override fun newThread(r: Runnable): Thread {
            return Thread(r, "Thread #" + mCount.getAndIncrement())
        }
    }

    /**
     * 超出线程池容量后的的排队队列,超出队列容量后将抛出异常
     */
    private val sPoolWorkQueue = LinkedBlockingQueue<Runnable>(16 * CPU_COUNT)
    /**
     * An [Executor] that can be used to execute tasks in parallel.
     */
    var THREAD_POOL_EXECUTOR: ThreadPoolExecutor? = null

    /**
     * 执行任务，当线程池处于关闭，将会创建新的线程池
     */
    @Synchronized
    fun execute(run: Runnable?) {
        if (run == null) {
            return
        }
        if (THREAD_POOL_EXECUTOR == null || THREAD_POOL_EXECUTOR!!.isShutdown) {
            // 参数说明
            // 当线程池中的线程小于mCorePoolSize，直接创建新的线程加入线程池执行任务
            // 当线程池中的线程数目等于mCorePoolSize，将会把任务放入任务队列sPoolWorkQueue中
            // 当sPoolWorkQueue中的任务放满了，将会创建新的线程去执行，
            // 但是当总线程数大于mMaximumPoolSize时，将会抛出异常，交给RejectedExecutionHandler处理
            // mKeepAliveTime是线程执行完任务后，且队列中没有可以执行的任务，存活的时间，后面的参数是时间单位
            // ThreadFactory是每次创建新的线程工厂

            THREAD_POOL_EXECUTOR = ThreadPoolExecutor(
                    CORE_POOL_SIZE,
                    MAXIMUM_POOL_SIZE,
                    KEEP_ALIVE.toLong(),
                    TimeUnit.SECONDS,
                    sPoolWorkQueue,
                    sThreadFactory,
                    RejectedExecutionHandler { _, _ -> Log.e("ThreadPool", "Over Queue!") })
            // 下面是cache类型 ，会无限开启线程，容易吃光资源
            //			 THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(0,
            //			 Integer.MAX_VALUE, 1L, TimeUnit.SECONDS,
            //			 new SynchronousQueue<Runnable>());
        }
        THREAD_POOL_EXECUTOR!!.execute(run)
    }

    /**
     * 取消线程池中某个还未执行的任务
     */
    @Synchronized
    fun cancel(run: Runnable) {
        if (THREAD_POOL_EXECUTOR != null && (!THREAD_POOL_EXECUTOR!!.isShutdown || THREAD_POOL_EXECUTOR!!.isTerminating)) {
            THREAD_POOL_EXECUTOR!!.remove(run)
        }
    }

    /**
     * 线程池队列中是否包含某个任务，正在执行的不算
     */
    @Synchronized
    operator fun contains(run: Runnable): Boolean {
        return if (THREAD_POOL_EXECUTOR != null && (!THREAD_POOL_EXECUTOR!!.isShutdown || THREAD_POOL_EXECUTOR!!.isTerminating)) {
            THREAD_POOL_EXECUTOR!!.queue.contains(run)
        } else {
            false
        }
    }

    /**
     * 立刻关闭线程池，停止所有任务，包括等待的任务。
     */
    @Synchronized
    fun stop() {
        if (THREAD_POOL_EXECUTOR != null && (!THREAD_POOL_EXECUTOR!!.isShutdown || THREAD_POOL_EXECUTOR!!.isTerminating)) {
            THREAD_POOL_EXECUTOR!!.shutdownNow()
        }
    }

    /**
     * 关闭线程池，不再接受新的任务。但已经加入的任务都将会被执行完毕才关闭
     */
    @Synchronized
    fun shutdown() {
        if (THREAD_POOL_EXECUTOR != null && (!THREAD_POOL_EXECUTOR!!.isShutdown || THREAD_POOL_EXECUTOR!!.isTerminating)) {
            THREAD_POOL_EXECUTOR!!.shutdown()
        }
    }
}
