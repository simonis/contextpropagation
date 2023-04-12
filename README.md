## Testing context propagation with [Disco's](https://github.com/awslabs/disco) `TransactionContext`


```ShellSession
$ mvn clean package
$ java -jar target/context-propagation-full.jar
15:MyExecutor Thread:null
14:pool-1-thread-1:null
16:MyInvocationHandler Thread:null
TransactionContext = 0
1:main:Dude
17:Thread with Runnable:null
14:pool-1-thread-1:null
18:Thread with run():null
15:MyExecutor Thread:null
16:MyInvocationHandler Thread:null
$ java -javaagent:target/dependency/disco-java-agent-0.13.0.jar -jar target/context-propagation-full.jar
14:pool-1-thread-1:null
15:MyExecutor Thread:null
16:MyInvocationHandler Thread:null
TransactionContext = 0
1:main:Dude
16:MyInvocationHandler Thread:null
15:MyExecutor Thread:Dude
17:Thread with Runnable:Dude
14:pool-1-thread-1:Dude
18:Thread with run():Dude
```

To get full debug logging run with:

```
$ java -javaagent:target/dependency/disco-java-agent-0.13.0.jar=loggerfactory=software.amazon.disco.agent.reflect.logging.StandardOutputLoggerFactory:extraverbose -Xlog:class+load+redefine -jar target/context-propagation-full.jar
...
[software.amazon.disco.agent.DiscoAgentTemplate] DiSCo(Core) passing arguments to ExecutorInterceptor to process
[software.amazon.disco.agent.DiscoAgentTemplate] DiSCo(Core) passing arguments to ForkJoinPoolInterceptor to process
[software.amazon.disco.agent.DiscoAgentTemplate] DiSCo(Core) passing arguments to ForkJoinTaskInterceptor to process
[software.amazon.disco.agent.DiscoAgentTemplate] DiSCo(Core) passing arguments to ForkJoinTaskSubclassInterceptor to process
[software.amazon.disco.agent.DiscoAgentTemplate] DiSCo(Core) passing arguments to ThreadInterceptor to process
[software.amazon.disco.agent.DiscoAgentTemplate] DiSCo(Core) passing arguments to ThreadSubclassInterceptor to process
[software.amazon.disco.agent.DiscoAgentTemplate] DiSCo(Core) passing arguments to ScheduledFutureTaskInterceptor to process
[software.amazon.disco.agent.DiscoAgentTemplate] DiSCo(Core) passing arguments to ThreadPoolInterceptor to process
...
[software.amazon.disco.agent.interception.InterceptionListener] DiSCo(Core) software.amazon.disco.agent.concurrent.ThreadPoolInterceptor discovered java.util.concurrent.ThreadPoolExecutor (after loading)
[software.amazon.disco.agent.interception.InterceptionListener] DiSCo(Core) software.amazon.disco.agent.concurrent.ThreadPoolInterceptor transforming java.util.concurrent.ThreadPoolExecutor (after loading)
[software.amazon.disco.agent.interception.InterceptionListener] DiSCo(Core) software.amazon.disco.agent.concurrent.ThreadPoolInterceptor completed java.util.concurrent.ThreadPoolExecutor(after loading)
[162,429s][info][redefine,class,load] redefined name=java.util.concurrent.ThreadPoolExecutor, count=3 (avail_mem=246024K)
...
[software.amazon.disco.agent.interception.InterceptionListener] DiSCo(Core) software.amazon.disco.agent.concurrent.ThreadSubclassInterceptor discovered io.simonis.contextpropagation.DiscoTransactionContext$1 (before loading)
[software.amazon.disco.agent.interception.InterceptionListener] DiSCo(Core) software.amazon.disco.agent.concurrent.ThreadSubclassInterceptor transforming io.simonis.contextpropagation.DiscoTransactionContext$1 (before loading)
[software.amazon.disco.agent.interception.InterceptionListener] DiSCo(Core) software.amazon.disco.agent.concurrent.ThreadSubclassInterceptor completed io.simonis.contextpropagation.DiscoTransactionContext$1(before loading)
...
[software.amazon.disco.agent.interception.InterceptionListener] DiSCo(Core) software.amazon.disco.agent.concurrent.ThreadSubclassInterceptor discovered java.lang.invoke.VarHandleLongs$FieldInstanceReadWrite (before loading)
[software.amazon.disco.agent.interception.InterceptionListener] DiSCo(Core) software.amazon.disco.agent.concurrent.ExecutorInterceptor transforming io.simonis.contextpropagation.DiscoTransactionContext$MyExecutor (before loading)
[software.amazon.disco.agent.interception.InterceptionListener] DiSCo(Core) software.amazon.disco.agent.concurrent.ExecutorInterceptor completed io.simonis.contextpropagation.DiscoTransactionContext$MyExecutor(before loading)
...
```
