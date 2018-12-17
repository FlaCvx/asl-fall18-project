import com.sun.org.apache.bcel.internal.generic.RET;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class
MyMiddleware {

	private final String myIP;
	private final int myPort;
	private Map<SocketChannel, String> client_channels = new HashMap<>(); //Here I store all the channels that need to sand unsturct. data
	private Map<SocketChannel, Task> client_task = new HashMap<>(); //Here I store all the channels that need to sand unsturct. data
	private final ThreadPool pool;
	private final static Logger LOGGER = Logger.getLogger(MyMiddleware.class.getName());
	private	Double timeBetweenTasks = 0.0;
	private ByteBuffer bBuffer;


	public MyMiddleware(String myIp, int myPort, List<String> mcAddresses, int numThreadsPTP, boolean readSharded) {
		this.myIP=myIp;
		this.myPort = myPort;
		pool = new ThreadPool(numThreadsPTP, mcAddresses, readSharded);
		LOGGER.setLevel(Level.SEVERE);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new SimpleFormatter());
		handler.setLevel(Level.SEVERE);
		LOGGER.addHandler(handler);
		this.bBuffer = ByteBuffer.allocate(8192);
	}

	public void run() {

		byte[] data = null;
		String request = "";
		Long timeBetweenTasksStart;
 		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run()
			{

				shutDownMW();

			}
		});
		try {
			boolean started = false;
			/* ServerSocketChannel is a channel that can listen for incoming TCP connections */
			ServerSocketChannel serverSocketChannel;

			/* create a selector that will by used for multiplexing. The selector registers the ServerSocketChannel as
			well as all SocketChannels that are created	*/
			Selector selector = Selector.open();
			
			/*I open the ServerSocketChannel and bind it with myIP and myPort */
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(new InetSocketAddress(myIP, myPort), 500);
			/*Now I make this channel non blocking...*/
			serverSocketChannel.configureBlocking(false);
				
			/*Register the selector with the ServerSocketChannel */
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			Iterator<SelectionKey> iterator;
			int bytesRead;
			int count=0;

			while(true)
			{	timeBetweenTasksStart = System.nanoTime();
				//System.out.println("Before the select op: "+System.currentTimeMillis());
  				selector.select();
				//System.out.println("After the select op: "+System.currentTimeMillis());

 				if (!started)
				{	pool.startCollection();
					started=true;
				}
 				iterator = selector.selectedKeys().iterator();
				
 				while(iterator.hasNext())
				{
 					SelectionKey tmpKey = iterator.next();
					if (tmpKey.isAcceptable())
					{	
						/* If I client wants to open a new connection */
 						SocketChannel client = serverSocketChannel.accept();
 						client.socket().setTcpNoDelay(true);
						client.configureBlocking(false);
						client.register(selector, SelectionKey.OP_READ );
						Task t = new Task();
						client_task.put(client, t);

					}
					else if ( tmpKey.isReadable()) {	 /*if (client_channels.get(tmpKey.channel())) { //It means that this channel still has to send unstruct. data
						
							if ( client_task.get(tmpKey.channel()).setrUnstructuredData()) *//* Task's method used to read the unstructured data*//*
							{	//The above method returns true if the Task has all the UnstructuredData 
								
								//Now I can logically remove this client_channel from the clients that still need to send data
								client_channels.put((SocketChannel) tmpKey.channel(), false); 
								
							}
						}
						else { //Is a channel that is sending a new request

							SocketChannel tmpChannel = (SocketChannel) tmpKey.channel();
							StringBuilder textLine = new StringBuilder(); *//*textLine is used to save the first line of the memtier client message *//*


							//Since I have to use a bytebuffer with the socketChannel and I need to read until "\r\n" I read byte per byte
							ByteBuffer bBuffer = ByteBuffer.allocate(1); // Used to read byte per byte until the first "\r\n"

							do{
							bBuffer.clear();
							bytesRead = tmpChannel.read(bBuffer);
							if ( bytesRead == -1 ) break;
							textLine.append(new String(bBuffer.array(),"UTF-8"));
							}while(!textLine.toString().endsWith("\r\n"));


						if (bytesRead != -1) {

							String[] textvec;//
							textvec= textLine.toString().split(" ");
							textvec[textvec.length-1] = textvec[textvec.length-1].replaceAll("\r\n","");
							//TODO: METTI ALTRI IGNORE CASE
								if (  textvec[0].equalsIgnoreCase("set"))
								{
									if ( textvec.length == 5) //There isn't the "noreply" parameter
									{
										//	constructor: public Task(String type, String key, String flags, int exptime, int bytes, boolean noreply, SocketChannel socketChannel )
										client_task.get(tmpChannel).setTask(textvec[0],textvec[1],textvec[2],Integer.parseInt(textvec[3]),Integer.parseInt(textvec[4]),false,tmpChannel);

										if ( !client_task.get(tmpChannel).setrUnstructuredData()) { //This means that there are more packets from where I have to read data
 									//Now I can logically put this client_channel with the clients that still need to send data for the set request...
									client_channels.put(tmpChannel, true); 
									
								}

										client_task.get(tmpChannel).setTextLine(textLine.substring(0));
										client_task.get(tmpChannel).setStart_time();
 										pool.execute(client_task.get(tmpChannel)); *//* Now the task is properly build, it can be put in the queue *//*


									}
							else if ( textvec.length == 6) //There is the "noreply" parameter
							{
								// Noreply is optional, I assume that here it is present...so I put true in the corresponding value of the constructor...
								client_task.get(tmpChannel).setTask(textvec[0],textvec[1],textvec[2],Integer.parseInt(textvec[3]),Integer.parseInt(textvec[4]),true,tmpChannel);
								if ( !client_task.get(tmpChannel).setrUnstructuredData() ) { //This means that there are more packets from where I have to read data
 									//Now I can logically put this client_channel with the clients that still need to send data for the set request...
									client_channels.put(tmpChannel, true); 
									
								}

								client_task.get(tmpChannel).setTextLine(textLine.substring(0));
								client_task.get(tmpChannel).setStart_time();
								pool.execute(client_task.get(tmpChannel));

							}

						}
						else if (textvec[0].equals("get") )
						{
							client_task.get(tmpChannel).getTask("get",tmpChannel, Arrays.stream(textvec).skip(1).toArray(String[]::new) );
							client_task.get(tmpChannel).setTextLine(textLine.substring(0));
							client_task.get(tmpChannel).setStart_time();
							pool.execute(client_task.get(tmpChannel));

						}
						
						else
						{
							*//*
							 * If the clients send a request that does not belong to one of these
								types, the worker thread has to record this event and discard data until a newline character
								is encountered.
								
							 *//*

							client_task.get(tmpChannel).discardTask("discard", tmpChannel);
							if ( !client_task.get(tmpChannel).setrUnstructuredData() ) { //This means that there are more packets from where I have to read data
								client_channels.put(tmpChannel, true); 
							}

						}
				 

   						
					}
						}
						*/


						SocketChannel tmpChannel = (SocketChannel) tmpKey.channel();
						bBuffer.clear();
						if (client_channels.containsKey(tmpKey.channel())) {
							int read = tmpChannel.read(bBuffer);
							if (read != -1 && read != 0) {
								data = new byte[read];
								System.arraycopy(bBuffer.array(), 0, data, 0, read);
								request = client_channels.get(tmpChannel) +(new String(data));
								if (request.charAt(request.length() - 1) != '\n')
									client_channels.put(tmpChannel, request);
									//System.out.println("Request size: "+request.length()+" --> "+request);
								else {
									client_channels.remove(tmpChannel);
									client_task.get(tmpChannel).set_Request(tmpChannel, request);
									pool.execute(client_task.get(tmpChannel));
								}

							}
						} else {
							int read = tmpChannel.read(bBuffer);
							if (read != -1 && read != 0) {
								data = new byte[read];
								System.arraycopy(bBuffer.array(), 0, data, 0, read);
								request = new String(data);
								if (request.charAt(request.length() - 1) != '\n')
									client_channels.put(tmpChannel, request);
								//System.out.println("Request size: "+request.length()+" --> "+request);
								else
								{
									client_task.get(tmpChannel).set_Request(tmpChannel, request);
									pool.execute(client_task.get(tmpChannel));
								}

							}
						}
					}
					
					iterator.remove();
					timeBetweenTasks = ((timeBetweenTasks)*count + (System.nanoTime() - timeBetweenTasksStart))/(++count);
				}

			}
			
			
		}
		catch (IOException e) {

			LOGGER.log(Level.SEVERE,"Exception thrown: ",e);
			//e.printStackTrace();
		}
	}


	public void shutDownMW()
	{
		pool.finishThreadPool((timeBetweenTasks/(1e6)));
		return;
	}
}
