# Netty-QuickStudy
this is my quick study netty project




![img.png](img.png)
NIO的三大组件： channel buffer selector
1. channel: 就是我们建立的连接，也就是一个一个的通道
2. buffer: 就是通道上面传递的数据
3. selector: 监听通道的变化，什么时候可以读？什么时候可以写

![img_1.png](img_1.png)

netty所有组件
![img_2.png](img_2.png)
netty关键组件：
1. pipeline
2. eventLoop 
3. eventLoop Group
4. pipeline 中的handler

bootstrap 组件将上面的组件组合起来完成通信

![img_3.png](img_3.png)

![img_4.png](img_4.png)

![img_5.png](img_5.png)