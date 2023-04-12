// javac -cp /priv/simonisv/Git/disco/disco-java-agent/disco-java-agent/build/libs/disco-java-agent-0.13.0.jar ~/Java/TransactionTest.java
// java -javaagent:/priv/simonisv/Git/disco/disco-java-agent/disco-java-agent/build/libs/disco-java-agent-0.13.0.jar -cp /home/ANT.AMAZON.COM/simonisv/Java -Xlog:class+load+redefine TransactionTest
//
// /share/software/Java/corretto-17/bin/java -javaagent:/priv/simonisv/Git/disco/disco-java-agent/disco-java-agent/build/libs/disco-java-agent-0.13.0.jar=loggerfactory=software.amazon.disco.agent.reflect.logging.StandardOutputLoggerFactory:extraverbose -cp /home/ANT.AMAZON.COM/simonisv/Java -Xlog:class+load+redefine TransactionTest
//
// /share/software/Java/corretto-11/bin/java -jar ./disco-java-agent-instrumentation-preprocess/build/libs/disco-java-agent-instrumentation-preprocess-0.13.0.jar --verbose --jdkSupport /share/software/Java/corretto-11/ --agentPath /tmp/disco-java-agent-0.13.0.jar --sourcePaths /tmp/disco-java-agent-0.13.0.jar --outputDir /tmp/out
//

package io.simonis.contextpropagation;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ArrayBlockingQueue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import software.amazon.disco.agent.reflect.concurrent.TransactionContext;

public class DiscoTransactionContext {

  static class MyExecutor extends Thread implements Executor {
    private volatile boolean shutdown = false;
    final ArrayBlockingQueue<Runnable> tasks = new ArrayBlockingQueue<>(1);
    public MyExecutor() {
      super("MyExecutor Thread");
      start();
    }
    public void execute(Runnable r) {
      try {
        tasks.put(r);
      } catch (InterruptedException ie) {
        System.out.println(ie);
      }
    }
    public void run() {
      while (!shutdown) {
        try {
          Runnable r = tasks.take();
          r.run();
        } catch (InterruptedException ie) {
          return;
        }
      }
    }
    public void shutdown() {
      shutdown = true;
    }
  }

  static class MyInvocationHandler implements InvocationHandler {
    private volatile boolean shutdown = false;
    final ArrayBlockingQueue<Runnable> tasks = new ArrayBlockingQueue<>(1);
    final Thread executor;
    public MyInvocationHandler() {
      executor = new Thread("MyInvocationHandler Thread") {
          public void run() {
            while (!shutdown) {
              try {
                Runnable r = tasks.take();
                r.run();
              } catch (InterruptedException ie) {
                return;
              }
            }
          }
        };
      executor.start();
    }
    public Object invoke(Object proxy, Method method, Object[] args) {
      Runnable r = (Runnable)args[0];
      try {
        tasks.put(r);
      } catch (InterruptedException ie) {
        System.out.println(ie);
      }
      return null;
    }
    public void shutdown() {
      shutdown = true;
    }
  }

  private static void printContext() {
    System.out.println(Thread.currentThread().getId() + ":" +
                       Thread.currentThread().getName() + ":" +
                       TransactionContext.getMetadata("Hey"));
  }

  private static void threadWithRunnable(Runnable r) {
    Thread t = new Thread(r, "Thread with Runnable");
    t.start();
  }

  private static void threadWithRun(Runnable r) {
    Thread t = new Thread("Thread with run()") {
        public void run() {
          r.run();
        }
      };
    t.start();
  }

  public static void main(String[] args) {
    Runnable r = new Runnable() {
        public void run() {
          printContext();
        }
      };

    ExecutorService es = Executors.newFixedThreadPool(1);
    es.execute(r);

    MyExecutor me = new MyExecutor();
    me.execute(r);

    MyInvocationHandler ih = new MyInvocationHandler();
    // For JDK 8 use -Dsun.misc.ProxyGenerator.saveGeneratedFiles=true to dump the proxy class
    // For JDK 9+ use -Djdk.proxy.ProxyGenerator.saveGeneratedFiles=true to dump the proxy class
    Executor pe = (Executor)Proxy.newProxyInstance(Executor.class.getClassLoader(), new Class<?>[] { Executor.class }, ih);
    pe.execute(r);

    List<Integer> il = List.of(1, 2, 3, 4);
    il.parallelStream().forEach(i -> printContext());

    int context = TransactionContext.create();
    System.out.println("TransactionContext = " + context);
    TransactionContext.putMetadata("Hey", "Dude");

    printContext();

    il.parallelStream().forEach(i -> printContext());

    threadWithRunnable(r);

    threadWithRun(r);

    es.execute(r);
    es.shutdown();

    me.execute(r);
    me.shutdown();

    pe.execute(r);
    ih.shutdown();
  }
}
