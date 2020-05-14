d Systems Lab (ETH course)
===========
The aim of the project is to build and evaluate
the performance of complex computer and software
systems. 

The course offers the opportunity to 
bring together the knowledge and expertise 
acquired in different areas (networking, 
systems programming, parallel programming)
by building a distributed information system.

The system receives requests from the network,
it categorizes the requests and distributes them 
over the servers.

The network's workload  is generated with instances of memtier.
While the servers are run on memcached, a key-value store.

![Alt text](./myddleware_pic.png?raw=true "Title")

The system can be run with the following command 
```console
java -jar middleware-cflavia.jar -l localhost -p 11211 -m localhost:11212 -t 64 -s true
```

-jar: java jar file.

-l: IP address of the middleware.

-p: port used by the middleware.

-t: number of worker threads.

-s: sharded mode enabled.

-m: memcached servers addresses.

### Classes
1. MyMiddleware<br/>
This is the class that starts the system.<br/>
Based on a single-thread architecture, it is the
interface between the external network and my system.
It uses a selector to switch between multiple 
connections. After having initialized the thread pool 
and the shut down hook, it will register 
the selector with its own server-socket-channel. 
The selector will then be used to select channels 
from the network.<br/>
As soon as a channel is selected there are two 
possibilities:<br/>
   - The channel is ”Acceptable”<br/>
This case happens the first time the middleware 
selects the channel. In this case my
system will, first, accept the connection, 
second, register the new channel with the
selector and third, associate the channel with a 
new Task object. Therefore, for every
channel, a new Task object is created and assigned to the channel, this task object
will then be reused for the different requests.

   - The channel is ”Readable”<br/>
After a channel is accepted it will start sending data. 
This data is read, without
making distinctions between the different requests, 
and will be immediately put in
to the queue. 
However, it may happen that the clients sends 
data separately therefore
at the first read the requests is not completed. 
If this is the case, the specific socket-channel will
be put in a map, and the partial request will be 
associated to it. When the selector
will iterate and select that channel again, 
it will try to complete the request.

2. Task<br/>
This class is used for the request objects. 
Each client’s channel is associated
with a task's object that is reused for every request.
A task can be of two types: ”set” and ”get”.
The multi-get request is incorporated in the get request.

3. ThreadPool<br/>
The ThreadPool class is used to create all the worker
 threads and, most of all, to collect the statistics. Statistics are kept in memory as maps. In fact, for every needed record(e.g.
service time ), the map collects for each worker thread a list of values, one value every
second ( in my case the window’s size is one second). Once a value is collected it will be
added to the above mentioned list. At the end, when the middleware is shut down, all
the statistics are printed and averages are calculated. Since experiments are not too long,
I believe is not too expensive to save in memory all the informations instead of logging it
straightway.
Last but not least, ThreadPool provides a method to return the server that has to be
queried. This method is called by the worker threads to equally distribute the load in the
memcached servers.

4. CollectStatistic<br/>
CollectStatistis is a class used to instantiate the object used by the timer for collecting
statistics.

5. WorkerThread<br/>
A given number of worker threads are created by the thread pool when the system is
initialized. Each worker thread collects temporary statistics for the tasks processed in the
one-second window. Each worker thread retrieves, if available, a task from the blocking
queue, it will then call a method ”build task” to appropriately recognize the type of
request.<br/>
Based on the type it will process the request:
   - Set request<br/>
A set request needs to be sent to every memcached server. Without any further
processing, the request will be forwarded, and the thread will wait for the responses
from all the servers.
   - Get request<br/>
The differences in processing get requests relies only on the mode enabled, if it is
”sharded” or ”non sharded”. In fact, in case of a single key, the non-sharded mode
will be used. The non sharded method is adapted to correctly process both single-key
and multiple-key requests.<br/>

      - Non sharded<br/>
    This mode will first ask the thread pool which server has to be queried, then it
will send all the data to this server. Key-value pairs are collected in maps, that
will then be used to send back the data to the client. The choice of the server
to query is based on a simple round-robin implementation. The index of the
servers is incremented each time a worker thread asks the pool which server has
to be queried. When it exceeds the number of servers it is set back to zero.<br/>
    
      - Sharded<br/>
After having queried all the servers, it will iterate on them to collect the queried
data. These data will be saved in a map, that will then be used to retrieve all
the values and send those in the same order as requested.

   In the end the Worker Thread will send back the response through the client’s socket
channel.
