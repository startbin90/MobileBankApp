# MobileBankApp
It is a simple simulated mobile banking software system on androidOS platform. This system(client and server) is developed based on Java language, and conforms Client-Server framework. 
-	The purpose of making this simulated app is totally out of interests on mobile software development and the wish to implementing known knowledge from college into real practice.
- This simulated mobile banking app mocks several basic services provided by mobile banking app on the market, such as account register and login, transfer and payee maintenance.
- The repository contains complete development documents, such as [Development manual (Chinese version)](简单模拟手机银行开发手册.docx) (development process of demand statement, analysis of demands, overall design and detailed design) and complete source code.
- Source code includes client app codes, client-server message transmission layer, server side codes and codes for building the database, all of which is quite suitable for beginners to discuss with each other. 
  - Client side shows programming methods and framworks based on androidOS, such as the use of Acticity class, Fragment class and the separation of UI/Main thread and worker thread. 
  - Client-server message transmission layer implements message encapsulation, splitting and some related transmission mechanisms based on TCP/IP protocol.
## DEMO
- [Demo (PDF file)](Demo.pdf)
- [Demo (Word file)](mobileBankingDemo.docx)
## Contents in repository
- [Android App (Folder: MobileBankApp)](MobileBankApp)
  - [Activity files](MobileBankApp/app/src/main/java/com/example/davychen/mobileBankApp/)
  - [Layout files](MobileBankApp/app/src/main/res/layout/)
- [Server (Folder: server)](server/)
  - [Source code](server/src/)
  - [Database schema](server/accountSchema.ddl)
- [Development manual (Chinese version)](简单模拟手机银行开发手册.docx)
# MobileBankApp(中文版)
这是一个基于安卓的简单模拟手机银行软件系统。本系统客户端和服务器端采用Java语言开发，符合C-S(客户端服务器)技术架构。
- 制作这个模拟小软件的目的纯粹是出于对手机软件开发的兴趣同时也希望能把在大学里学到的知识应用到实践中。
- 这个模拟的手机银行软件模拟了生活中手机银行的基本功能，比如手机银行注册和登录，转账和收款人名册管理。
- 本栏目提供了完整的app开发项目文档，其中[简单模拟手机银行开发手册](简单模拟手机银行开发手册.docx)包含了需求说明、需求分析、总体设计、详细设计等技术文档，以及完整的源代码。
- 源代码文档包括了手机端app代码、交易通讯层代码、服务器代码和建立数据库代码，适合初学者借鉴和交流。
  - 手机端展示了基于安卓平台app源代码的基本框架和方式方法，例如Activity类、Fragment类和UI与其他线程的分离；
  - 交易通讯层实现了基于TCP/IP协议下的交易报文的封装、拆分、传输机制；后台服务器采用线程为app提供交易服务。
## 演示
- [演示 (PDF 文件)](Demo.pdf)
- [演示 (Word 文件)](mobileBankingDemo.docx)
## Repository 内容介绍
- [安卓软件本体代码 (所在文件夹: MobileBankApp)](MobileBankApp)
  - [活动(Activity)等 主要代码](MobileBankApp/app/src/main/java/com/example/davychen/mobileBankApp/)
  - [布局(Layout) 代码](MobileBankApp/app/src/main/res/layout/)
- [服务器 (文件夹: server)](server/)
  - [服务器主要代码 Source code](server/src/)
  - [数据库ddl文件 Database schema](server/accountSchema.ddl)
- [简单模拟手机银行开发手册](简单模拟手机银行开发手册.docx)

