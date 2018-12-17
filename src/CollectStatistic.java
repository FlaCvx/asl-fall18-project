import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CollectStatistic extends TimerTask {

	ThreadPool pool;

	
	
	
	public CollectStatistic(ThreadPool threadPool) {
		this.pool = threadPool;
	}

	@Override
	public synchronized void run() {

		pool.collectAvgThroughput();
		//System.out.println("Aerage Throughput: "+tmp);
		//this.pool.avg_throughput.add(tmp);

		pool.collectQueueLenght();
		//System.out.println("Queue lenght: "+queue_temp);
		//this.pool.queue_length.add(queue_temp);

		pool.collectWaitingTime();
		//System.out.println("Average waiting time : "+tmp);
		//this.pool.avg_waiting_time.add(tmp);

		pool.collectServiceTime();
		//System.out.println("Average service time : "+tmp);
		//this.pool.avg_service_time.add(tmp);

		pool.collectParseTime();

		pool.collectNumberOfSets();
		//System.out.println("Number of sets until now : " + number);
		//this.pool.number_sets.add(number);

		pool.collectNumberOfGets();
		//System.out.println("Number of gets until now: " + number);
		//this.pool.number_gets.add(number);

		pool.collectNumberOfMulti_Gets();
		//System.out.println("Number of gets until now: " + number);
		//this.pool.number_multiple_gets.add(number);

        pool.collectMisses();

	}
	
	public void logStat() {

        pool.printAllStatisticsPerThread();

        pool.printAllAggregatedStatistics();

    }
}
