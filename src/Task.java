import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Task {

	private String type;
	private String[] key;
	//private String flags;
	//private int exptime; //At maximum is 32 bit..
	private int numbytes;
	//private boolean noreply;
	private byte[] unstruct_data;
	private SocketChannel socketChannel;
	private String textLine;
	private String request;
	private int unstruct_filled;
	private int num_keys = 0;
	private long start_time;
	private long end_time;
	private final static Logger LOGGER = Logger.getLogger(Task.class.getName());
	private boolean init = false;


	public Task() {
		LOGGER.setLevel(Level.INFO);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new SimpleFormatter());
		handler.setLevel(Level.INFO);
		LOGGER.addHandler(handler);
		this.unstruct_filled = 0;
		key = new String[10];


	}

	public void set_Request(SocketChannel socket_channel, String request) {
		this.socketChannel = socket_channel;
		this.request = request;
		this.setStart_time();

	}

	public String get_request()
	{
		return this.request;
	}


	public void buildTask() {
		String[] textvec;//
		String[] textvec_line;
		textvec = request.split("\r\n");
		textvec_line = textvec[0].split(" ");
		if (textvec_line[0].equalsIgnoreCase("set")) {
			if (textvec_line.length == 5) //There isn't the "noreply" parameter
			{
				//	constructor: public Task(String type, String key, String flags, int exptime, int bytes, boolean noreply, SocketChannel socketChannel )
				this.setTask(textvec_line[0], textvec_line[1], textvec_line[2], Integer.parseInt(textvec_line[3]),
						Integer.parseInt(textvec_line[4]), false);
				this.unstruct_data = textvec[1].getBytes();

				this.setTextLine(textvec[0]);

			} else if (textvec_line.length == 6) //There is the "noreply" parameter
			{
				this.setTask(textvec_line[0], textvec_line[1], textvec_line[2], Integer.parseInt(textvec_line[3]), Integer.parseInt(textvec_line[4]), true);
				this.unstruct_data = textvec[1].getBytes();

				this.setTextLine(textvec[0]);
			}

		} else if (textvec_line[0].equals("get")) {
			this.getTask("get", Arrays.stream(textvec_line).skip(1).toArray(String[]::new));
			this.setTextLine(textvec[0]);

		} else {
			this.discardTask("discard");
			this.unstruct_data = textvec[1].getBytes();
			}

		}



	/*Set TASK*/
	public void setTask(String type, String key, String flags, int exptime, int bytes, boolean noreply ) {
		if (!init)
		{
			this.unstruct_data=new byte[bytes];
			init = true;
		}
		this.type=type;
		this.key[0] = key; /*Cause key[] can also be used for multi-get operations */
		num_keys = 1;
		//this.flags=flags;
		//this.exptime=exptime;
		//this.noreply=noreply;
		this.numbytes = bytes;
		this.unstruct_filled=0;

	}

	
	/*Get TASK constructor */
	public void getTask (String type, String... keys)
	{
		this.type=type;
		this.key = keys;
		num_keys = keys.length;
	}
	
	/*Discard TASK constructor*/
	public void discardTask (String type)
	{
		this.type=type;
	}
	
	public String getType() {
		return type;
	}
 
	public boolean setrUnstructuredData( )
	{	int bytesRead = 0;
		boolean flag = true;
		if (this.type.equals("discard")) {
			LOGGER.log(Level.INFO,"A request was discarded.");
			// TODO: BASTA LA READLINE IMPORTANTE!!! MODIFICARE!!!
			ByteBuffer buf_data = ByteBuffer.allocate(1);
			try {
				do { 
					buf_data.clear();
					bytesRead = socketChannel.read(buf_data);
					if (bytesRead == 0) {
						flag=false;
						break;
					}
				} while (!new String(buf_data.array(),"UTF-8").equals("\n"));
					
			}
			catch (IOException e) {
				LOGGER.log(Level.SEVERE,"Exception thrown: ",e);
				//e.printStackTrace();
			}
			return flag;
		}
		
		else {


			ByteBuffer buf_data = ByteBuffer.allocate(numbytes-this.unstruct_filled);
			try {
				bytesRead = socketChannel.read(buf_data);
			//	System.out.println(new String(buf_data.array(),"UTF-8"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				LOGGER.log(Level.SEVERE,"Exception thrown: ",e);
			}

			System.arraycopy(buf_data.array(),0,this.unstruct_data, this.unstruct_filled, bytesRead);

			this.unstruct_filled += bytesRead;

			if ( this.unstruct_filled < numbytes) return false; //Still missing bytes
			else return true;
		}
	}


	public byte[] getUnstructuredData()
	{
		return this.unstruct_data;
	}
	
	
	public SocketChannel getChannel() {
		return socketChannel;
	}



	public String[] getGetKeys() {

		return Arrays.stream(key).limit(this.num_keys).toArray(String[]::new) ;
	}

	
	public long getWaitingTime() {
		return (end_time - start_time);
	}


	public void setStart_time() {
		this.start_time = System.currentTimeMillis();
	}

	public void setEndTime() {
		this.end_time = System.currentTimeMillis();
	}


	public void setTextLine(String string) {
		this.textLine = string;

	}
	
	public String getTextLine( ) {
		return this.textLine;
		
	}


}
