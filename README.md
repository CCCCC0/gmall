# gmall
鼓励商城

gmall-user-web : serverPort = 1000
gmall-user-service : serverPort = 2000

gmall-manager-web:8082
gmall-manager-service:8072

gmall-item-web:8083

gmall-search-service:8074
gmall-search-web:8084

gmall-cart-service:8075
gmall-cart-web:8085

gmall-order-service:8076
gmall-order-web:8086

gmall-passport:单点登录系统  8087

gmall-pay:支付系统  8088

在linux上开启了 tomcat8.5  
在server.xml中最后一行加上了开启时并加载dubbo

在项目中使用了dubbo 加 zookeeper  调用远程服务(完成)

后台管理员基本完成
