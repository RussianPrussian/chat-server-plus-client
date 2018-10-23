# chat-server-plus-client
A practice project to get the hang of some ins and outs of Java Sockets plus multithreading

## Starting the server
Find the entry-point class (SimpleMessageService) in the SimpleMessageService project and run it as a java application

## Starting a client
Find the main class in the SimpleMessageClient (ClientFrame) and run it as a java application.

## Some other notes
The server uses an H2 database without any sort of persistence beyond the lifespan of the application. If you really
want to use the login mechanism, you need to switch out the h2 database for a real one. I used vanilla java to initiate
the DB connection, because it's hard these days to actually find time to learn some basics; jobs throw you into
the deep end with Spring/Hibernate/whatever other framework, where all the mechanics are abstracted away from you. I built this
thing as an exercise to gain more appreciation for those frameworks (and the work/frustration they save you) and to
help myself gain some insight into how we can work directly with Sockets/Threads instead of relying on Tomcat/Apache.

## What does the application actually do?
When you open a client, you'll be prompted to either set up a user-name or log into the server.
Unfortunately, you cannot go backwards, right now. You will then be asked to enter the user-name
of the person you wish to speak to. They will be asked whether they'd like to speak to you. If they agree,
you will have a (hopefully) solid chat connection. If the other party disconnects or you enter "EXIT", you will be able to
find a new partner to speak with.
