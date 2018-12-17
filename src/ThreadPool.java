import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;


public class ThreadPool {


	private int numThreadsPTP;
	private final WorkerThread[] threads;
	private final BlockingQueue<Task> queue;
	private List<String> mcAddresses;
	private int actualServerToQuery;
	private List<Integer> VectorToQuery;
	private Double avgtime_between_tasks=0.0;
	private int[][] ServerQueriedByWT;


	private CollectStatistic collectStat;

	//THESE ARE USED TO SAVE THE x SECONDS STATISTICS..
	private Map<WorkerThread,List<Integer>> avg_throughput = new HashMap<>();
	private List<Integer> queue_length = new LinkedList<>();
	private Map<WorkerThread,List<Integer>> misses = new HashMap<>();
	private Map<WorkerThread,List<Double>> avg_waiting_time = new HashMap<>();
	private Map<WorkerThread,List<Double>> avg_service_time = new HashMap<>();
	private Map<WorkerThread,List<Double>> avg_parse_time = new HashMap<>();
	private Map<WorkerThread,List<Integer>> number_sets = new HashMap<>();
	private Map<WorkerThread,List<Integer>> number_gets = new HashMap<>();
	private Map<WorkerThread,List<Integer>> number_multiple_gets = new HashMap<>();
	private Timer timer1;

	public ThreadPool(int numThreadsPTP, List<String> mcAddresses, boolean readSharded) {

		this.numThreadsPTP = numThreadsPTP;
		threads = new WorkerThread[this.numThreadsPTP];
		this.queue = new LinkedBlockingQueue<>();
		this.mcAddresses = mcAddresses;
		timer1 = new Timer();
		VectorToQuery = new LinkedList<>();
		for (int i = 0; i < this.mcAddresses.size(); i++) {
			VectorToQuery.add(i);


		}

		for (int i = 0; i < this.numThreadsPTP; i++) {
			threads[i] = new WorkerThread(queue, mcAddresses, this,readSharded);
			threads[i].start();
		}

		for ( WorkerThread tmp: threads)
		{
			this.avg_throughput.put(tmp, new LinkedList<>());
			this.avg_waiting_time.put(tmp, new LinkedList<>());
			this.avg_service_time.put(tmp, new LinkedList<>());
			this.avg_parse_time.put(tmp, new LinkedList<>());
			this.number_sets.put(tmp, new LinkedList<>());
			this.number_gets.put(tmp, new LinkedList<>());
			this.number_multiple_gets.put(tmp, new LinkedList<>());
			this.misses.put(tmp,new LinkedList<>());
		}

		collectStat = new CollectStatistic(this);
		this.actualServerToQuery = -1;
	}

	public void startCollection()
	{
		timer1.schedule(collectStat, 0, 1000);

	}

	public synchronized int getNextServerToQuery() //It implements the round robin algorithm.
	{
		actualServerToQuery++;
		if (actualServerToQuery == mcAddresses.size()) {
			this.actualServerToQuery = 0;
		}
		return actualServerToQuery;
	}



	public void execute(Task task) {

		synchronized (queue) {
			//System.out.println("Prima di add "+System.currentTimeMillis());
			queue.add(task);
			//System.out.println("Dopo di add "+System.currentTimeMillis());

			queue.notify();
		}
	}

	public void finishThreadPool(Double average_time_between_tasks) {
		ServerQueriedByWT = new int[this.threads.length][this.mcAddresses.size()];

		this.avgtime_between_tasks=average_time_between_tasks;
		for (int i = 0; i < this.numThreadsPTP; i++) {
			for (int j = 0 ; j< this.mcAddresses.size(); j++)
				ServerQueriedByWT[i][j] = threads[i].getServersQueried(j);
		}

		for (int i = 0; i < this.numThreadsPTP; i++) {

			threads[i].shutDown();
		}
		this.collectStat.logStat();

		return;

	}


	public List<Integer> getRandomCombOfServerToQuery() {

		Collections.shuffle(VectorToQuery);
		return VectorToQuery;
	}




	public void collectAvgThroughput() {
		for (WorkerThread tmp : threads) {
			this.avg_throughput.get(tmp).add(tmp.getThroughput());
			//System.out.println("Throughput for thread-i: "+this.throughtput.get(tmp));
		}
	}



	public void collectQueueLenght() {

		this.queue_length.add(this.queue.size());
	}

	public void collectServiceTime() {
		for (WorkerThread tmp : threads) {
			this.avg_service_time.get(tmp).add(tmp.getServiceTimeAVG());
		}

	}


	public void collectParseTime() {
		for (WorkerThread tmp : threads) {
			this.avg_parse_time.get(tmp).add(tmp.getTimeToParse());
		}

	}


	public void collectWaitingTime() {
		for (WorkerThread tmp : threads) {
			this.avg_waiting_time.get(tmp).add(tmp.getWaitingTimeAVG());
		}
	}


	public void collectNumberOfSets() {
		for (WorkerThread tmp : threads) {
			this.number_sets.get(tmp).add(tmp.getNumberOfSets());
		}

	}

	public void collectMisses(){
		for (WorkerThread tmp:threads)
		{
			this.misses.get(tmp).add(tmp.getMisses());
		}
	}


		public void collectNumberOfGets () {
			for (WorkerThread tmp:threads)
			{
				this.number_gets.get(tmp).add(tmp.getNumberOfGets());
			}

		}

		public void collectNumberOfMulti_Gets () {
			for (WorkerThread tmp:threads)
			{
				this.number_multiple_gets.get(tmp).add(tmp.getNumberOfMultiGets());
			}

		}


		public void printAllStatisticsPerThread() {
		int j = 0;
			DecimalFormat df2 = new DecimalFormat(".##");
			DecimalFormat df4 = new DecimalFormat(".####");


			for (WorkerThread tmp: threads) {
			System.out.println("\nThread "+ j++ );
			System.out.println("Throughput          Waiting time          Service time          Time to parse          Sets          Gets          Multi-gets          Misses");
			String indent1 = "                    "; //20 spaces between throughput and waitin
			String indent2 = "                      "; //22 spaces between waitin and service
			String indent3 = "                      "; //22 spaces between service and time to parse
			String indent4 = "                       "; //23 sapces between time to prase and set
			String indent5 = "              "; //14 spaces between sets and gets
			String indent6 = "              "; //14 spaces between get and multi-gets
			String indent7 = "                    " ; //20 spaces...
				int count = this.avg_throughput.get(tmp).size();

				for (int i = 0; i < count; i++) {
					if (avg_throughput.get(tmp).get(i) != 0) {
						String thro = df2.format(avg_throughput.get(tmp).get(i));
						String wait_time = df4.format(avg_waiting_time.get(tmp).get(i));
						String service_time = df4.format(avg_service_time.get(tmp).get(i));
						String parse_time = df4.format(avg_parse_time.get(tmp).get(i));
						String numsets = df2.format(number_sets.get(tmp).get(i));
						String numgets = df2.format(number_gets.get(tmp).get(i));
						String nummultigets = df2.format(number_multiple_gets.get(tmp).get(i));
						String nummisses = df2.format(misses.get(tmp).get(i));
						System.out.println((thro + indent1.substring(0, 20 - thro.length()))
								+ (wait_time + indent2.substring(0, 22 - wait_time.length()))
								+ (service_time + indent3.substring(0, 22 - service_time.length()))
								+ (parse_time + indent4.substring(0, 23 - parse_time.length()))
								+ (numsets + indent5.substring(0, 14 - numsets.length()))
								+ (numgets + indent6.substring(0, 14 - numgets.length()))
								+ (nummultigets + indent7.substring(0, 20 - nummultigets.length()))
								+ (nummisses));
					}
				}

			for (int s = 0; s < mcAddresses.size(); s++)
				System.out.println("Server "+s+", get requests: "+this.ServerQueriedByWT[j-1][s]);
			}
		}

		public void printAllAggregatedStatistics() {

			DecimalFormat df2 = new DecimalFormat(".##");
			DecimalFormat df4 = new DecimalFormat(".####");

			Integer TotNumGets = this.number_gets.values().stream().flatMap(t->t.stream()).collect(Collectors.summingInt(t->t))
								+ this.number_multiple_gets.values().stream().flatMap(t->t.stream()).collect(Collectors.summingInt(t->t));
			Double missRatio = misses.values().stream().flatMap(t->t.stream()).collect(Collectors.summingInt(t->t)) /(TotNumGets.doubleValue()) ;
			System.out.println();
			System.out.println("Aggregated Statistics for a 1 seconds window of the entire middleware: ");
			System.out.println("Throughput          QueueLength          Waiting time          Service time          Time to parse          Sets          Gets          Multi-gets          Misses");
			int count = queue_length.size();
			String indent1 = "                    "; //20 spaces between throughput and queue lengj
			String indent2 = "                     "; //21 spaces between queue lenght and waiting
			String indent3 = "                      "; //22 spaces between waitin and service
			String indent4 = "                      "; //22 spaces between service and time to parse
			String indent5 = "                       "; //23 spaces between time to parse and set
			String indent6 = "              "; //14 spaces between sets and gets
			String indent7 = "              "; //14 spaces between get and multi-gets
			String indent8 = "                    "; //20 spaces between multi and misses

			List<Double> aggregate_thr = new LinkedList<>();
			List<Double> aggregate_waiting = new LinkedList<>();
			List<Double> aggregate_serviceTime = new LinkedList<>();
			List<Double> aggregate_parseTime = new LinkedList<>();
			List<Double> aggregate_numSets= new LinkedList<>();
			List<Double> aggregate_numGets= new LinkedList<>();
			List<Double> aggregate_numMultigets= new LinkedList<>();
			List<Double> aggregate_misses =  new LinkedList<>();

			Map<WorkerThread,List<Integer>> tmp_avg_throughput = new HashMap<>(avg_throughput);
			//List<Integer> tmp_queue_length = new LinkedList<>(queue_length);
			Map<WorkerThread,List<Integer>> tmp_misses = new HashMap<>(misses);
			Map<WorkerThread,List<Double>> tmp_avg_waiting_time = new HashMap<>(avg_waiting_time);
			Map<WorkerThread,List<Double>> tmp_avg_service_time = new HashMap<>(avg_service_time);
			Map<WorkerThread,List<Double>> tmp_avg_parse_time = new HashMap<>(avg_parse_time);
			Map<WorkerThread,List<Integer>> tmp_number_sets = new HashMap<>(number_sets);
			Map<WorkerThread,List<Integer>> tmp_number_gets = new HashMap<>(number_gets);
			Map<WorkerThread,List<Integer>> tmp_number_multiple_gets = new HashMap<>(number_multiple_gets);




			for (int i = 0; i< count ; i++) {
				aggregate_thr.add(tmp_avg_throughput.values().stream().map(t -> t.remove(0)).collect(Collectors.summingInt(t->t)).doubleValue());
				aggregate_waiting.add(tmp_avg_waiting_time.values().stream().map(t -> t.remove(0)).collect(Collectors.averagingDouble(t->t)));
				aggregate_serviceTime.add(tmp_avg_service_time.values().stream().map(t -> t.remove(0)).collect(Collectors.averagingDouble(t->t)));
				aggregate_parseTime.add(tmp_avg_parse_time.values().stream().map(t -> t.remove(0)).collect(Collectors.averagingDouble(t->t)));
				aggregate_numSets.add(tmp_number_sets.values().stream().map(t -> t.remove(0)).collect(Collectors.summingInt(t->t)).doubleValue());
				aggregate_numGets.add(tmp_number_gets.values().stream().map(t -> t.remove(0)).collect(Collectors.summingInt(t->t)).doubleValue());
				aggregate_numMultigets.add(tmp_number_multiple_gets.values().stream().map(t -> t.remove(0)).collect(Collectors.summingInt(t->t)).doubleValue());
				aggregate_misses.add(tmp_misses.values().stream().map(t -> t.remove(0)).collect(Collectors.summingInt(t->t)).doubleValue());


			}
			for (int i =0;i<count;i++)
			{
				if (aggregate_thr.get(i)==0)
				{
					aggregate_thr.remove(i);
					aggregate_waiting.remove(i);
					aggregate_serviceTime.remove(i);
					aggregate_parseTime.remove(i);
					aggregate_numSets.remove(i);
					aggregate_numGets.remove(i);
					aggregate_numMultigets.remove(i);
					aggregate_misses.remove(i);
					i--;

				}
				count = aggregate_thr.size();

			}

			//aggregate_thr.remove(0);
			//aggregate_waiting.remove(0);
			//aggregate_serviceTime.remove(0);
			//aggregate_parseTime.remove(0);
			//aggregate_numSets.remove(0);
			//aggregate_numGets.remove(0);
			//aggregate_numMultigets.remove(0);
			//aggregate_misses.remove(0);
			//queue_length.remove(0);
			count = queue_length.size();

			count = aggregate_misses.size();
			List<Double> TOTresponseTime = new LinkedList<>();
			for (int i = 0; i < count; i++) {

				TOTresponseTime.add(aggregate_serviceTime.get(i) + aggregate_waiting.get(i) + aggregate_parseTime.get(i) + this.avgtime_between_tasks);
				//System.out.println(TOTresponseTime.get(i));
			}


			for (int i = 0; i < count; i++) {
				String thro=df2.format(aggregate_thr.get(i));
				String queuel = df2.format(queue_length.get(i));
				String wait_time = df4.format(aggregate_waiting.get(i));
				String service_time = df4.format(aggregate_serviceTime.get(i));
				String parse_time = df4.format(aggregate_parseTime.get(i));
				String numsets=df2.format(aggregate_numSets.get(i));
				String numgets=df2.format(aggregate_numGets.get(i));
				String nummultigets = df2.format(aggregate_numMultigets.get(i));
				String nummisses = df2.format(aggregate_misses.get(i));
				System.out.println((thro+indent1.substring(0,20-thro.length()))
						+(queuel+indent2.substring(0,21-queuel.length()))
						+(wait_time+indent3.substring(0,22-wait_time.length()))
						+(service_time+indent4.substring(0,22-service_time.length()))
						+(parse_time + indent5.substring(0,23-parse_time.length()))
						+(numsets+indent6.substring(0,14-numsets.length()))
						+(numgets+indent7.substring(0,14-numgets.length()))
						+(nummultigets + indent8.substring(0, 20-nummultigets.length()))
						+(nummisses));
			}

			/*
			System.out.println("Aggregated Statistics normalized for one second window: ");

			for (int i = 0; i < count; i++) {
				String thro=df2.format(aggregate_thr.get(i)/3);
				String queuel = df2.format(queue_length.get(i));
				String wait_time = df2.format(aggregate_waiting.get(i));
				String service_time = df2.format(aggregate_serviceTime.get(i));
				String numsets=df2.format(aggregate_numSets.get(i)/3);
				String numgets=df2.format(aggregate_numGets.get(i)/3);
				String nummultigets = df2.format(aggregate_numMultigets.get(i)/3);
				String nummisses = df2.format(aggregate_misses.get(i)/3);
				System.out.println((thro+indent1.substring(0,20-thro.length()))
						+(queuel+indent2.substring(0,21-queuel.length()))
						+(wait_time+indent3.substring(0,22-wait_time.length()))
						+(service_time+indent4.substring(0,22-service_time.length()))
						+(numsets+indent5.substring(0,14-numsets.length()))
						+(numgets+indent6.substring(0,14-numgets.length()))
						+(nummultigets + indent7.substring(0, 20-nummultigets.length()))
						+(nummisses));
			}


		/*System.out.println("The throughput statistics are based on \"stable\" values, without considering the first and the last one.");
		pool.avg_throughput.stream().limit(pool.avg_throughput.size()-1).skip(1).forEach(new Consumer<Long>() {
			@Override
			public void accept(Long aLong) {
				System.out.println(aLong);
			}
		});*/

			if (aggregate_misses.size() > 2) {
				Double average_th = aggregate_thr.stream().collect(Collectors.averagingDouble(t -> t)) ;
				Double average_queue = queue_length.stream().collect(Collectors.averagingInt(t -> t));
				Double average_wait = aggregate_waiting.stream().collect(Collectors.averagingDouble(t -> t));
				Double average_service = aggregate_serviceTime.stream().collect(Collectors.averagingDouble(t -> t));
				Double average_parse = aggregate_parseTime.stream().collect(Collectors.averagingDouble(t->t));
				Double average_sets = aggregate_numSets.stream().collect(Collectors.averagingDouble(t -> t));
				Double average_gets = aggregate_numGets.stream().collect(Collectors.averagingDouble(t -> t));
				Double average_multi = aggregate_numMultigets.stream().collect(Collectors.averagingDouble(t -> t));
				Double average_Numbmisses = aggregate_misses.stream().collect(Collectors.averagingDouble(t->t));

				double std_thr, std_queue, std_waiting, std_service, std_parse, std_sets, std_gets, std_multi, std_misses;

				std_thr = Math.sqrt((aggregate_thr.stream().limit(aggregate_thr.size() - 1).skip(1)
						.map(t -> t - average_th)
						.map(t -> t * t)
						.reduce(0.0, (a, b) -> a + b)
				) / (aggregate_thr.size()));

				std_queue = Math.sqrt((queue_length.stream().limit(queue_length.size() - 1).skip(1)
						.map(t -> t - average_queue)
						.map(t -> t * t)
						.reduce(0.0, (a, b) -> a + b)
				) / (queue_length.size()));

				std_waiting = Math.sqrt((aggregate_waiting.stream().limit(aggregate_waiting.size() - 1).skip(1)
						.map(t -> t - average_wait)
						.map(t -> t * t)
						.reduce(0.0, (a, b) -> a + b)
				) / (aggregate_waiting.size()));

				std_service = Math.sqrt((aggregate_serviceTime.stream().limit(aggregate_serviceTime.size() - 1).skip(1)
						.map(t -> t - average_service)
						.map(t -> t * t)
						.reduce(0.0, (a, b) -> a + b)
				) / (aggregate_serviceTime.size()));

				std_parse = Math.sqrt((aggregate_parseTime.stream().limit(aggregate_parseTime.size()-1).skip(1)
						.map(t -> t - average_parse )
						.map( t -> t*t )
						.reduce(0.0, (a,b) -> a+b)
				) / (aggregate_parseTime.size()));


				std_sets = Math.sqrt((aggregate_numSets.stream().limit(aggregate_numSets.size() - 1).skip(1)
						.map(t -> t - average_sets)
						.map(t -> t * t)
						.reduce(0.0, (a, b) -> a + b)
				) / (aggregate_numSets.size()));

				std_gets = Math.sqrt((aggregate_numGets.stream().limit(aggregate_numGets.size() - 1).skip(1)
						.map(t -> t - average_gets)
						.map(t -> t * t)
						.reduce(0.0, (a, b) -> a + b)
				) / (aggregate_numGets.size()));

				std_multi = Math.sqrt((aggregate_numMultigets.stream().limit(aggregate_numMultigets.size() - 1).skip(1)
						.map(t -> t - average_multi)
						.map(t -> t * t)
						.reduce(0.0, (a, b) -> a + b)
				) / (aggregate_numMultigets.size()));


				std_misses = Math.sqrt((aggregate_misses.stream().limit(aggregate_misses.size() - 1).skip(1)
						.map(t -> t - average_Numbmisses)
						.map(t -> t * t)
						.reduce(0.0, (a, b) -> a + b)
				) / (aggregate_misses.size()));

				System.out.println("The following results are aggregated for all threads, normalized for one second");
				System.out.println("Throughput(ops/sec):  (avg)" + df2.format(average_th) + ",  (std)" + df2.format(std_thr));
				System.out.println("QueueLength: (avg)" + df2.format(average_queue) + ",  (std)" + df2.format(std_queue));
				System.out.println("WaitingTime(msec): (avg)" + df4.format(average_wait) + ",  (std)" + df4.format(std_waiting));
				System.out.println("ServiceTime(msec): (avg)" + df4.format(average_service) + ",  (std)" + df4.format(std_service));
				System.out.println("ParseTime(msec): (avg)" + df4.format(average_parse) + ", (std)"+df4.format(std_parse));
				System.out.println("Sets(ops/sec): 		 (avg)" + df2.format(average_sets) + ",  (std)" + df2.format(std_sets));
				System.out.println("Gets(ops/sec): 		 (avg)" + df2.format(average_gets) + ",  (std)" + df2.format(std_gets));
				System.out.println("Multi-gets(ops/sec):  (avg)" + df2.format(average_multi) + ",  (std)" + df2.format(std_multi));
				System.out.println("Misses(ops/sec):      (avg)" + df2.format(average_Numbmisses) + ",  (std)" + df2.format(std_misses));

				System.out.println("Miss ratio: " + df4.format(missRatio));
				System.out.println("Avg time between two tasks: "+df4.format(this.avgtime_between_tasks));
				//Histogram

				System.out.println();
				System.out.println("Tot response time: average_waitingTime + average_serviceTime + average_parseTime + average_timeBetweenTwoTasks\n" );



				Double max = Collections.max(TOTresponseTime);
				Double min = Collections.min(TOTresponseTime);
				System.out.println("max response time : "+ df4.format(max));
				System.out.println("min response time: "+ df4.format(min));
				System.out.println();
				int num_bins = (int) Math.round(((max - min) / ((max - min) / 10)));
				int[] histo = new int[num_bins + 1];

				for (Double tmp : TOTresponseTime) {
					histo[(int) (Math.round((tmp - min) / ((max - min) / 10)))]++;
				}

				Double step = min;
				for (int i = 0; i < num_bins + 1; i++) {

					System.out.print(df4.format(step) + ":   ");
					for (int j = 0; j < histo[i]; j++)
						System.out.print("#");
					System.out.println();
					step += ((max - min) / 10);
				}

			} else
				System.out.println("Not enough data for calculating statistics...");


		}
		}



