### 简介

Reactor是一种应用于服务器端开发的设计模式，基于事件驱动形式。

一般服务器而言，主要有以下几个基本结构

> Read request
>
> Decode request
>
> Process service
>
> Encode reply
>
> send reply

传统的I/O形式，通过一个线程来处理一个连接，开销过大。

目前流行采用的Java NIO方式可以基于单个线程的Reactor处理多个请求，大大节省系统资源

以下是基于单个Reactor、单线程处理方式。单个Reactor、多线程的处理方式

### 单个Reactor,单线程形式

Reactor职责主要如下

> 绑定相应接口
>
> 监听基本请求
>
> 调用Acceptor来接受请求
>
> 调用dispatch来处理基本请求

#### 绑定相应接口

直接调用`ServerSocketChannel`类来绑定接口

#### 监听基本请求

首先进行注册

```java
SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
```

`register`第二个参数为**interest集合**，通过`Selector`来监听`Channel`感兴趣的事件，可以监听以下四种事件

> Connect
>
> Accept
>
> Read
>
> Write

```java
SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
selectionKey.attach(new Acceptor(serverSocketChannel, selector));
```

其中`attch`方式将`Acceptor`类绑定于`SelectionKey`实例上，`Acceptor`主要用于接受请求

注册监视事件之后，通过`Selector`的具体方法来获取相应注册`SelectionKey`信息

监听请求主要是`Selector`类中的`select`方法，其中`select`具体实现是在`SelectorImp`类中，`Selector`中只是定义基本的抽象方法。

`selector`建立在JNI之上，底层还是利用了C，主要用于监听各种连接，目前了解仅此而已。

在`select`方法返回值不为0时候，之后调用`selector.selectedKeys()`获取到相应连接事件(即在`register`中注册的相应interests集合)的`SelectionKey`

```java
Iterator keyIterator = selectedKeys.iterator();
while(keyIterator.hasNext()) {
    SelectionKey key = keyIterator.next();
    if(key.isAcceptable()) {
        // a connection was accepted by a ServerSocketChannel.
    } else if (key.isConnectable()) {
        // a connection was established with a remote server.
    } else if (key.isReadable()) {
        // a channel is ready for reading
    } else if (key.isWritable()) {
        // a channel is ready for writing
    }
    keyIterator.remove();
}
```

#### 调用dispatch

```java
for (SelectionKey selectedItem: selected) {
  printKeyInfo(selectedItem);
  dispatch(selectedItem);
}
```



```java
private void dispatch(SelectionKey selectionKey) {
    Runnable runnable = (Runnable) selectionKey.attachment();
    if (runnable != null) runnable.run();
}
```

`selectionKey`绑定的为`Acceptor`类，attachment返回的即为`Acceptor`的具体实例，每一个事件的处理与具体实现分离开来



####  调用Acceptor来接受请求

demo中展示的为

```java
try {
  SocketChannel channel = serverSocketChannel.accept();
  if (channel != null) {
    new Handler(selector, channel);
  }
} catch (IOException exception) {
  exception.printStackTrace();
}
```

`Acceptor`中目前是调用`ScoketChannel.accept`来接受请求，当接受请求后的`channel`存在，即接受成功时候，将具体的事件交由`Handler`去处理，`Handler`具体处理事件的业务需求



单个Reactor，单线程形式仅仅适用于学习，不适用于生产环境

### 单个Reactor,多线程形式

目前仅仅是将Handler部分将原有的单个线程转变为线程池形式，即单个Reactor分发事件之后，每个事件处理有独立的线程。而非再一个线程上进行处理。

但是单个Reactor也有局限性，目前比较成熟方案是主从Reactor，多线程模式。

Netty中是有具体实现，目前Java NIO成熟的框架



### 此Demo中遗留问题

1.TCP 粘包问题

2.客户连接突然中断以后，没有相应标志位去检测，程序不具有健壮性

3.利用output.hasRemaining方式判断是否连接中断有局限性，本应该无限循环发送信息的，在某一步卡住。关键代码块为以下

```java
if (!output.hasRemaining()) {
    System.out.println("ssssss" +new String(output.array()));
    selectionKey.cancel();
    return;
}
```

4. 3中打印的output具体显示有问题，显示为具体的空白框



### 启动方式

1.启动server中的Controller,再启动client中的Controller。

多线程和单线程模式共用一个client端