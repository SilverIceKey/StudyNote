# Activity启动模式  
- standard  
    默认。系统始终会在目标任务中创建新的 Activity 实例，并向其传送 Intent。
- singleTop  
    如果目标任务的顶部已存在 Activity 实例，则系统会通过调用该实例的 onNewIntent() 方法向其传送 Intent，而非创建新的 Activity 实例。
- singleTask  
    系统会在新任务的根位置创建 Activity 并向其传送 Intent。不过，如果已存在 Activity 实例，则系统会调用该实例的 onNewIntent() 方法（而非创建新的 Activity 实例），向其传送 Intent。
- singleInstance  
    与“singleTask"”相同，只是系统不会将任何其他 Activity 启动到包含实例的任务中。该 Activity 始终是其任务中的唯一 Activity。  
# 堆栈清除原理  
清除返回堆栈  
如果用户离开任务较长时间，系统会清除任务中除根 Activity 以外的所有 Activity。当用户再次返回到该任务时，只有根 Activity 会恢复。系统之所以采取这种行为方式是因为，经过一段时间后，用户可能已经放弃了之前执行的操作，现在返回任务是为了开始某项新的操作。  
  
您可以使用一些 Activity 属性来修改此行为：  
  
alwaysRetainTaskState  
如果在任务的根 Activity 中将该属性设为 "true"，则不会发生上述默认行为。即使经过很长一段时间后，任务仍会在其堆栈中保留所有 Activity。  
clearTaskOnLaunch  
如果在任务的根 Activity 中将该属性设为 "true"，那么只要用户离开任务再返回，堆栈就会被清除到只剩根 Activity。也就是说，它与 alwaysRetainTaskState 正好相反。用户始终会返回到任务的初始状态，即便只是短暂离开任务也是如此。  
finishOnTaskLaunch  
该属性与 clearTaskOnLaunch 类似，但它只会作用于单个 Activity 而非整个任务。它还可导致任何 Activity 消失，包括根 Activity。如果将该属性设为 "true"，则 Activity 仅在当前会话中归属于任务。如果用户离开任务再返回，则该任务将不再存在。