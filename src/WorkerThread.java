import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;


public class WorkerThread extends Thread {

	private BlockingQueue queue;
	private Socket[] mc_sockets;
	private int numServer;
	int countServer[];
	private ThreadPool parentPool;
	private char[] cbuf = new char[4098];
 	private Map<String, byte[]> multiple_get; //The key String is the key of the multiple get
 	private Map<String, String> key_textline = new HashMap<>(); //The key String is the key of the multiple get
 	private int throughput = 0;
	private Map<Integer, Long > serviceTime = new HashMap<>();
	private Double serviceT = 0.0;
 	private int numberOfSets = 0;
	private int numberOfGets = 0;
	private int numberOfMultiGets = 0;
	private Double waiting_time = 0.0;
	private Double TimeToParse = 0.0;
	private int misses;
	private int countTotal=0;
 	private BufferedReader[] line_in;
	private BufferedOutputStream[] out ;
 	private ByteBuffer byte_buf ;
	private final static Logger LOGGER = Logger.getLogger(WorkerThread.class.getName());
	private boolean readSharded;


	public WorkerThread(BlockingQueue queue, List<String> mcAddresses, ThreadPool parentPool, boolean readSharded) {
		this.queue = queue;
		this.parentPool = parentPool;
		byte_buf = ByteBuffer.allocateDirect(8192);
		mc_sockets = new Socket[mcAddresses.size()];
	    numServer = mc_sockets.length;
		this.readSharded=readSharded;
		line_in = new BufferedReader[mcAddresses.size()];
		out = new BufferedOutputStream[mcAddresses.size()];
		countServer = new int[numServer];

		LOGGER.setLevel(Level.SEVERE);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new SimpleFormatter());
		handler.setLevel(Level.SEVERE);
		LOGGER.addHandler(handler);
        misses = 0;
		try {
 				for (int i=0; i<mcAddresses.size();i++)
				{

 					mc_sockets[i] = new Socket( mcAddresses.get(i).split(":")[0],
                                        Integer.parseInt(mcAddresses.get(i).split(":")[1]));
  					line_in[i] = new BufferedReader(new InputStreamReader(mc_sockets[i].getInputStream()));
 					
					out[i] = new BufferedOutputStream(mc_sockets[i].getOutputStream());

				}
			} catch (UnknownHostException e) {

				LOGGER.log(Level.SEVERE,"Exception thrown: ",e);
				//e.printStackTrace();
			} catch (IOException e) {
 				// e.printStackTrace();
				LOGGER.log(Level.SEVERE,"Exception thrown: ",e);

		}
 		this.multiple_get = new HashMap<>(10);
		
 		


	}

	public void run() {
		String tmp_line;
		String tmp_data;
		Task task = null ;
		String endstring = "END\r\n";
		byte[] endst = endstring.getBytes();
		StringBuilder textToSend = new StringBuilder();
		String er_en ="\r\n";
		Long TimeToParseStart=null;
		while(true) {
			try {
				task = (Task) queue.take();
				task.setEndTime();
				task.buildTask();
				TimeToParseStart=System.nanoTime();

				//System.out.println("Waiting time: "+task.getWaitingTime());
				//System.out.println("Queue lenght: "+queue.size());
				//System.out.println(task.getTextLine());
				this.waiting_time = ((this.waiting_time*countTotal)+task.getWaitingTime())/(countTotal+1); // In Milliseconds

			} catch (InterruptedException e1) {
				LOGGER.log(Level.SEVERE,"Exception thrown: ",e1);
				//e1.printStackTrace();
			}
			if (task.getType().equals("get")) {

					String[] keys = task.getGetKeys();
					if (keys.length == 1)
						this.numberOfGets++;
					else
						this.numberOfMultiGets++;

				if ((readSharded) && (task.getGetKeys().length>1))
				{    //sharded mode on with number of gets greater than 1
					textToSend.setLength(0);
					//System.out.println("New request: "+task.getTextLine());
					try {
						double num = Math.round(keys.length / numServer);
						int key = 0;
						int i;


						for (i = 0; i < (numServer - 1); i++) { //First I write in n-1 servers a fixed, equal, number of keys
							countServer[i]++;
							textToSend.setLength(0);
							textToSend.append(task.getType());
							//out[i].write(task.getType().getBytes());
							for (int j = 0; j < num; j++) {    // I ask num key at each server, except the last one that can have num +/- 1
								textToSend.append(" ");
								textToSend.append(task.getGetKeys()[key++]);

								//out[order_to_query.get(i)].write((" " + task.getGetKeys()[key++]).getBytes()); //I use key to index the keys in the array given back by getGetKeys
							}
							textToSend.append(er_en);
							//System.out.println(textToSend);
							serviceTime.put(i, System.nanoTime());
							//System.out.println("Server: "+i+" --> send: "+textToSend);
							out[i].write(textToSend.toString().getBytes());
							out[i].flush();

						}


						//Do this just for the last one...Because you don't know exactly how many keys are left
						//Do this just for the last one...Because you don't know exactly how many keys are left

						textToSend.setLength(0);
						textToSend.append(task.getType());

					int numKeys = ((task.getGetKeys().length) - key);
					countServer[i]++;
					for (int j = 0; j < numKeys; j++) {
						textToSend.append(" ");
						textToSend.append(task.getGetKeys()[key++]);
					}
					textToSend.append(er_en);
					//System.out.println(textToSend);
					serviceTime.put(i, System.nanoTime());
					out[i].write(textToSend.toString().getBytes());
					out[i].flush();

					boolean service_flag;


						byte[] buf;
						int countCache = 0;
						for (i = 0; i < numServer; i++) { //For each server read all the keys they send back
							service_flag = true;
							while (!(tmp_line = line_in[i].readLine()).equals("END")) { //I don't know exactly how many keys they are sending
								//System.out.println("Server: "+i+"--> keys: "+tmp_line);
								if (service_flag) //Do this just the firs time to set the service Time
								{
									Long end = System.nanoTime();
									//System.out.println("ServiceTimeEnd: "+end);
									//System.out.println("Diff: "+(end-serviceTime.get(i)));
									serviceT = ((serviceT * countTotal) + (end - serviceTime.get(i))) / ((countTotal + 1));
									//System.out.println("service: "+serviceT+", countTot: "+countTotal);
									service_flag = false;
								}
								//System.out.println(tmp_line);
								key_textline.put(tmp_line.split(" ")[1], tmp_line + er_en); //key_textline is a vector where i save each request key
								// associated to each textLIne...eg.
								int offset = 0;
								int len_to_read = 4098;
								while((offset = offset + line_in[i].read(cbuf, offset, len_to_read)) != 4098)
								{
									//System.out.println("Offset: "+offset);
									len_to_read = 4098-offset;
									//System.out.println("Offset: "+offset+". len_to_read: "+len_to_read);

									//System.out.println("To read: "+len_to_read);
									len_to_read -= offset;

								}
								//System.out.println("offset: "+offset);
								//System.out.println("num server: "+i);
								//Cause the byte block ends with \r\n so it is god to read it all
								//System.out.println(tmp_data);
								buf = new String(cbuf).getBytes("UTF-8");
								multiple_get.put(tmp_line+ er_en, buf); //I will use this later to order the key-values
								countCache++;
							}
							//System.out.println(tmp_line);

						}
						misses += task.getGetKeys().length - countCache;


						String textline_of_key;
						for (String temp : task.getGetKeys()) {
							textline_of_key = this.key_textline.get(temp);
							if (textline_of_key != null) { //The server may even not send it back

								byte_buf.put(textline_of_key.getBytes());
								byte_buf.put(this.multiple_get.get(textline_of_key));
								//byte_buf.put((er_en).getBytes());
								byte_buf.flip();
								while (byte_buf.hasRemaining())
								{
									task.getChannel().write(byte_buf);

								}
								byte_buf.clear();


							}
						}

						byte_buf.put(endst);
						byte_buf.flip();
						while (byte_buf.hasRemaining())
						{
							task.getChannel().write(byte_buf);
						}
						byte_buf.clear();

					} catch (IOException e) {

						e.printStackTrace();
					}


				}
					else { // non sharded or single get

						int serverToQuery = parentPool.getNextServerToQuery(); //I just ask all the queries to one server
						Long endServiceTime;
						String textline_of_key;
						byte[] buf;
						countServer[serverToQuery] ++;
						serviceTime.put(serverToQuery, System.nanoTime());
						try {
							out[serverToQuery].write(task.get_request().getBytes());
							out[serverToQuery].flush();

						} catch (IOException e) {
							e.printStackTrace();
						}
						boolean service_flag = true;

						try {
							String tmp;
                            int countCache = 0;
							while (!(tmp = line_in[serverToQuery].readLine()).equals("END")) {    //Send the textline to the memtier client
								if (service_flag) {
									endServiceTime = System.nanoTime() - serviceTime.get(serverToQuery);
									serviceT = ((serviceT * countTotal) + endServiceTime ) / (countTotal + 1);
									service_flag = false;
								}
								key_textline.put(tmp.split(" ")[1], tmp + er_en); //key_textline is a vector where i save each request key
								// associated to each textLIne...eg.
								buf = line_in[serverToQuery].readLine().getBytes();
								multiple_get.put(tmp + er_en, buf); //I will use this later to order the key-values
								countCache++;

							}

							misses += task.getGetKeys().length - countCache;

							for (String temp : task.getGetKeys()) {
								textline_of_key = this.key_textline.get(temp);
								if (textline_of_key != null) { //The server may even not send it back
									byte_buf.put(textline_of_key.getBytes());
									byte_buf.put(this.multiple_get.get(textline_of_key));
									byte_buf.put((er_en).getBytes());
									byte_buf.flip();
									while (byte_buf.hasRemaining())
									{
										task.getChannel().write(byte_buf);
									}
									byte_buf.clear();


								}
							}

							byte_buf.put(endst);
							byte_buf.flip();
							while (byte_buf.hasRemaining())
							{
								task.getChannel().write(byte_buf);
							}
							byte_buf.clear();



						} catch (IOException e) {
							//e.printStackTrace();
							LOGGER.log(Level.SEVERE,"Exception thrown: ",e);

						}
					}
					this.throughput++;
					multiple_get.clear();
					key_textline.clear();

			}
				//END OF THE "GET" CASE

			else if (task.getType().equals("set")) {
						setRequest(task);
				}
			//System.out.println("Time to parse: "+ (System.nanoTime()-TimeToParseStart));
			TimeToParse = ((TimeToParse*countTotal)+ (System.nanoTime()-TimeToParseStart))/((countTotal+1));
			countTotal++;
			this.serviceTime.clear();
		}
		}



    public int getMisses()
    {
    	int tmp = misses;
    	misses=0;
    	return tmp;
    }


	public void setRequest(Task task){


		numberOfSets++;
		boolean flag = true;
		//  Now I check the response of each memcached server..
		String response = null;


		//Write the request in each channel
		try {
			for (int i = 0; i < mc_sockets.length; i++) {
				serviceTime.put(i, System.nanoTime());
                //line_out[i].write(task.getTextLine());    //First I send the textLine
				//line_out[i].flush();
				//data_out[i].write(task.getUnstructuredData()); //Then I send the unstructured data
				//data_out[i].flush();
				out[i].write(task.get_request().getBytes());
				out[i].flush();
			}
			Boolean not_stored = false ;
			Boolean error = false;
 			for (int i = 0; i < mc_sockets.length; i++) {
				response = line_in[i].readLine();
				while(response==null)
				{
					response = line_in[i].readLine();
				}
				//System.out.println("Service time: "+ (System.nanoTime() - serviceTime.get(i)));
 				serviceT = (((serviceT*countTotal)+(System.nanoTime() - serviceTime.get(i)))/(countTotal+1));
 				if (response.equals("NOT STORED"))

				{	not_stored=true;
					byte_buf.put((response+"\r\n").getBytes());
					byte_buf.flip();

					while (byte_buf.hasRemaining())
					{
						task.getChannel().write(byte_buf);
					}
					byte_buf.clear();

				} else if (!response.equals("STORED")) {

					LOGGER.log(Level.INFO,"A set request sent the following error message: ",response);
					error = true;
					byte_buf.put((response+"\r\n").getBytes());
					byte_buf.flip();

					while (byte_buf.hasRemaining())
					{
						task.getChannel().write(byte_buf);
					}
					byte_buf.clear();
				}
 			}
			//If all the servers stored it, then I can send a stored message..
			if ((not_stored==false)&&(error==false)) //Send stored msg
			{
				//System.out.println("Stored");
				this.throughput++;
				byte_buf.put((response+"\r\n").getBytes());
				byte_buf.flip();

				while (byte_buf.hasRemaining())
				{
					task.getChannel().write(byte_buf);
				}
				byte_buf.clear();

			}
		} catch (IOException e) {
			//e.printStackTrace();
			LOGGER.log(Level.SEVERE,"Exception thrown: ",e);

		}


	}






	public void shutDown() {
		for (Socket tmp_mc_sockets : mc_sockets) {
			try {
				tmp_mc_sockets.close();
			} catch (IOException e) {
				//e.printStackTrace();
				LOGGER.log(Level.SEVERE,"Exception thrown: ",e);

			}
		}
		
		for ( int i =0 ; i<this.mc_sockets.length;i++)
		{
			try {
 			     line_in[i].close();

			} catch (IOException e) {
				//e.printStackTrace();
				LOGGER.log(Level.SEVERE,"Exception thrown: ",e);
			}
		}
		return;

	}
	
	public int getThroughput() {
		int tmp = this.throughput;
		this.throughput = 0;
		return tmp;
	}

	
	public Double getServiceTimeAVG()
	{
		this.TimeToParse -= serviceT;
		Double tmp = this.serviceT/1000000;
		this.serviceT=0.0;
		this.countTotal=0;
		return tmp;
		
	}

	public Double getTimeToParse(){
		Double tmp = this.TimeToParse/1000000;
		this.TimeToParse=0.0;
		return tmp;
	}

	public  Double getWaitingTimeAVG()
	{
		Double tmp = this.waiting_time;
		this.waiting_time=0.0;
		this.countTotal=0;
		return tmp;
		
	}

	public  int getNumberOfSets() {
		int tmp = this.numberOfSets;
		this.numberOfSets = 0;
		return tmp;
 	}

	
	
	public  int getNumberOfGets() {
		int tmp = this.numberOfGets;
		this.numberOfGets = 0;
		return tmp;
	}
	
	public  int getNumberOfMultiGets() {
		int tmp = this.numberOfMultiGets;
		this.numberOfMultiGets = 0;
		return tmp;
	}


	public  int getServersQueried(int i) {
		//System.out.println(this.countServer[0]);
		int tmp = this.countServer[i];
		this.countServer[i] = 0;
		return tmp;
	}


}
 
