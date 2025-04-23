import java.util.*;

/**
 * Simulates 5 CPU scheduling algorithms for 25 randomly generated jobs.
 *
 * <p><b>Data Structures Used:</b></p>
 * <ul>
 *   <li><b>ArrayList: </b> Main job storage and initial sorting</li>
 *   <li><b>PriorityQueue: </b> Used in SJF/SRTF/HPF algorithms</li>
 *   <li><b>LinkedList: </b> Used as Queue in Round Robin</li>
 * </ul>
 * 
 * <p><b>Runtime Complexities:</b></p>
 * <ul>
 *   <li><b>Initialization:</b> O(n log n) for sorting</li>
 *   <li><b>FIFO:</b> O(n) processing</li>
 *   <li><b>SJF/HPF:</b> O(n log n) for priority queue operations</li>
 *   <li><b>SRTF:</b> O(n log n) with frequent scheduling</li>
 *   <li><b>Round Robin:</b> O(n*k) where k is time slices</li>
 * </ul>
 * 
 * @author Yutong Ye
 */
public class JobScheduler {
    
    /**
     * Represents a job with scheduling parameters.
     */
/**
 * Represents a job with scheduling parameters.
 */
static class Job implements Comparable<Job> {
    int arrTime;
    int cpuBurst;
    int priority;
    int exitTime;
    int turnAroundTime;
    int remainingTime;
    int jobId;
    static int nextId = 1;

    /**
     * Constructs a new job with specified parameters.
     * @param arrTime Arrival time of the job
     * @param cpuBurst CPU burst time required
     * @param priority Priority level (1-5)
     */
    public Job(int arrTime, int cpuBurst, int priority) {
        this.arrTime = arrTime;
        this.cpuBurst = cpuBurst;
        this.priority = priority;
        this.remainingTime = cpuBurst;
        this.jobId = nextId++;
    }

    /** 
     * Calculates the turnaround time for this job.
     */
    public void calculateTurnAroundTime() {
        this.turnAroundTime = this.exitTime - this.arrTime;
    }

    /**
     * Compares jobs by arrival time.
     * @param other The job to compare to
     * @return negative, zero, or positive integer
     */
    @Override 
    public int compareTo(Job other) {
        return Integer.compare(this.arrTime, other.arrTime);
    }

    /**
     * Creates a comparator that sorts jobs by CPU burst time.
     * @return Comparator for sorting by CPU burst
     */
    public static Comparator<Job> byCpuBurst() {
        return Comparator.comparingInt(job -> job.cpuBurst);
    }

    /**
     * Creates a comparator that sorts jobs by remaining time.
     * @return Comparator for sorting by remaining time
     */
    public static Comparator<Job> byRemainingTime() {
        return Comparator.comparingInt(job -> job.remainingTime);
    }

    /**
     * Creates a comparator that sorts jobs by priority.
     * @return Comparator for sorting by priority
     */
    public static Comparator<Job> byPriority() {
        return Comparator.comparingInt(job -> -job.priority); // Negative for descending order
    }

    /**
     * Resets the static job ID counter.
     */
    public static void resetIds() {
        nextId = 1;
    }

    /**
     * Returns a string representation of the job.
     * @return Formatted string showing job details
     */
    @Override
    public String toString() {
        return String.format("Job %2d: Arrival_Time=%3d, CPU_Time=%2d, Priority=%d, Exit_Time=%3d, Turnaround_Time=%3d",
                jobId, arrTime, cpuBurst, priority, exitTime, turnAroundTime);
    }
}

    private List<Job> jobs;
    private int timeQuantum = 2;
    private Random rand = new Random();



    /**
     * Initializes scheduler with random jobs.
     */
    public JobScheduler() {
        jobs = new ArrayList<>();
        for (int i = 0; i < 25; i++) {  
            jobs.add(new Job(
                rand.nextInt(250) + 1,
                rand.nextInt(14) + 2,
                rand.nextInt(5) + 1
            ));
        }
        Collections.sort(jobs);
    }


    private void resetJobs() {
        Job.resetIds();  
        for (Job job : jobs) {
            job.remainingTime = job.cpuBurst;
            job.exitTime = 0;
            job.turnAroundTime = 0;
        }
    }

/**
 * Runs the First In First Out (FIFO) scheduling algorithm.
 * 
 * <p><b>Description:</b> Processes jobs in the order of their arrival time.
 * Each job runs to completion before the next one starts.</p>
 * 
 * <p><b>Usage:</b> Suitable for simple scheduling scenarios where job order matters.
 * Fair but may result in high turnaround time for short jobs stuck behind long ones.</p>
 * 
 * <p><b>Time Complexity:</b> O(n)</p>
 */
    public void runFIFO() {
        resetJobs();
        int currentTime = 0;
        for (Job job : jobs) {
            currentTime = Math.max(currentTime, job.arrTime);
            currentTime += job.remainingTime;
            job.remainingTime = 0;
            job.exitTime = currentTime;
            job.calculateTurnAroundTime();
        }
    }

/**
 * Runs the Shortest Job First (SJF) scheduling algorithm.
 * 
 * <p><b>Description:</b> Selects the job with the shortest CPU burst time from the ready queue.
 * Non preemptive: once a job starts, it runs to completion.</p>
 * 
 * <p><b>Usage:</b> Optimal for minimizing average turnaround time in batch processing.
 * Not suitable for real time systems. Long jobs may suffer starvation.</p>
 * 
 * <p><b>Time Complexity:</b> O(n log n) due to priority queue operations.</p>
 */
    public void runSJF() {
        resetJobs();
        PriorityQueue<Job> queue = new PriorityQueue<>(Job.byCpuBurst());
        List<Job> jobList = new ArrayList<>(jobs);
        List<Job> completedJobs = new ArrayList<>();
        int currentTime = 0;

        while (!jobList.isEmpty() || !queue.isEmpty()) {
            while (!jobList.isEmpty() && jobList.get(0).arrTime <= currentTime) {
                queue.add(jobList.remove(0));
            }
            if (queue.isEmpty()) {
                currentTime = jobList.get(0).arrTime;
                continue;
            }
            Job currentJob = queue.remove();
            currentTime += currentJob.remainingTime;
            currentJob.exitTime = currentTime;
            currentJob.calculateTurnAroundTime();
            completedJobs.add(currentJob);
        }
        jobs = completedJobs;
    }

/**
 * Runs the Shortest Remaining Time First (SRTF) scheduling algorithm.
 * 
 * <p><b>Description:</b> Preemptive version of SJF. At every unit of time,
 * the job with the smallest remaining execution time is selected.</p>
 * 
 * <p><b>Usage:</b> Ideal when short response time is important.
 * Frequent context switches. May cause starvation for longer jobs.</p>
 * 
 * <p><b>Time Complexity:</b> O(n log n) with frequent scheduling decisions.</p>
 */
    public void runSRTF() {
        resetJobs();
        PriorityQueue<Job> queue = new PriorityQueue<>(Job.byRemainingTime());
        List<Job> jobList = new ArrayList<>(jobs);
        List<Job> completedJobs = new ArrayList<>();
        int currentTime = 0;

        while (!jobList.isEmpty() || !queue.isEmpty()) {
            while (!jobList.isEmpty() && jobList.get(0).arrTime <= currentTime) {
                queue.add(jobList.remove(0));
            }
            if (queue.isEmpty()) {
                currentTime = jobList.get(0).arrTime;
                continue;
            }
            Job currentJob = queue.remove();
            currentJob.remainingTime--;
            currentTime++;
            if (currentJob.remainingTime == 0) {
                currentJob.exitTime = currentTime;
                currentJob.calculateTurnAroundTime();
                completedJobs.add(currentJob);
            } else {
                queue.add(currentJob);
            }
        }
        jobs = completedJobs;
    }

/**
 * Runs the Highest Priority First (HPF) scheduling algorithm.
 * 
 * <p><b>Description:</b> Always selects the job with the highest priority value.
 * Non preemptive: the current job runs until it finishes.</p>
 * 
 * <p><b>Usage:</b> Useful when some jobs are more critical than others.
 * Can lead to starvation of lower priority jobs if higher ones keep arriving.</p>
 * 
 * <p><b>Time Complexity:</b> O(n log n) due to priority queue.</p>
 */
    public void runHPF() {
        resetJobs();
        PriorityQueue<Job> queue = new PriorityQueue<>(Job.byPriority());
        List<Job> jobList = new ArrayList<>(jobs);
        List<Job> completedJobs = new ArrayList<>();
        int currentTime = 0;

        while (!jobList.isEmpty() || !queue.isEmpty()) {
            while (!jobList.isEmpty() && jobList.get(0).arrTime <= currentTime) {
                queue.add(jobList.remove(0));
            }
            if (queue.isEmpty()) {
                currentTime = jobList.get(0).arrTime;
                continue;
            }
            Job currentJob = queue.remove();
            currentTime += currentJob.remainingTime;
            currentJob.exitTime = currentTime;
            currentJob.calculateTurnAroundTime();
            completedJobs.add(currentJob);
        }
        jobs = completedJobs;
    }
/**
 * Runs the Round Robin (RR) scheduling algorithm.
 * 
 * <p><b>Description:</b> Each job gets a fixed time quantum, default is 2 units.
 * Jobs cycle through the queue until completion. Preemptive by design.</p>
 * 
 * <p><b>Usage:</b> Fair and responsive for time sharing systems or multi-user environments.
 * Avoids starvation, but may increase average turnaround time if quantum is too small.</p>
 * 
 * <p><b>Time Complexity:</b> O(n * k), where k is the number of time slices.</p>
 */

    public void runRoundRobin() {
        resetJobs();
        Queue<Job> queue = new LinkedList<>();
        List<Job> jobList = new ArrayList<>(jobs);
        List<Job> completedJobs = new ArrayList<>();
        int currentTime = 0;

        while (!jobList.isEmpty() || !queue.isEmpty()) {
            while (!jobList.isEmpty() && jobList.get(0).arrTime <= currentTime) {
                queue.add(jobList.remove(0));
            }
            if (queue.isEmpty()) {
                currentTime = jobList.get(0).arrTime;
                continue;
            }
            Job currentJob = queue.remove();
            int timeSlice = Math.min(timeQuantum, currentJob.remainingTime);
            currentJob.remainingTime -= timeSlice;
            currentTime += timeSlice;
            if (currentJob.remainingTime == 0) {
                currentJob.exitTime = currentTime;
                currentJob.calculateTurnAroundTime();
                completedJobs.add(currentJob);
            } else {
                queue.add(currentJob);
            }
        }
        jobs = completedJobs;
    }


    /**
     * Prints initial job information.
     */
    public void printJobs() {
        System.out.println("Initial Job Information:");
        System.out.println("ID | Arrival | CPU Burst | Priority");
        for (Job job : jobs) {
            System.out.printf("%2d | %7d | %9d | %8d\n",
                    job.jobId, job.arrTime, job.cpuBurst, job.priority);
        }
        System.out.println();
    }

    /**
     * Prints scheduling results and calculates performance metrics.
     *
     * @param algorithmName Name of the scheduling algorithm
     * @return A Result object containing the average turnaround time and throughput
     */
    public Result printResults(String algorithmName) {
        System.out.println(algorithmName + " Results:");

        int totalTurnAround = 0;
        int maxExitTime = 0;

        for (Job job : jobs) {
            System.out.println(job);
            totalTurnAround += job.turnAroundTime;
            if (job.exitTime > maxExitTime) {
                maxExitTime = job.exitTime;
            }
        }

        double avgTurnAround = jobs.size() > 0 ? (double) totalTurnAround / jobs.size() : 0;
        double throughput = maxExitTime > 0 ? (double) jobs.size() / maxExitTime : 0;

        System.out.printf("Average Turnaround Time: %.2f\n", avgTurnAround);
        System.out.printf("Throughput: %.4f jobs/unit\n", throughput);
        System.out.println();

        return new Result(algorithmName, avgTurnAround, throughput);
    }


/**
 * Holds the performance results of a scheduling algorithm.
 */
    static class Result {
        String algorithm;
        double avgTurnaround;
        double throughput;

        public Result(String algorithm, double avgTurnaround, double throughput) {
            this.algorithm = algorithm;
            this.avgTurnaround = avgTurnaround;
            this.throughput = throughput;
        }
}

    /**
     * Entry point for the job scheduling simulation.
     * @param args Command line arguments 
     */
    public static void main(String[] args) {
        JobScheduler scheduler = new JobScheduler();
        scheduler.printJobs();

        List<Result> results = new ArrayList<>();

        scheduler.runFIFO();
        results.add(scheduler.printResults("FIFO"));

        scheduler.runSJF();
        results.add(scheduler.printResults("Shortest Job First"));

        scheduler.runSRTF();
        results.add(scheduler.printResults("Shortest Remaining Time First"));

        scheduler.runHPF();
        results.add(scheduler.printResults("Highest Priority First"));

        scheduler.runRoundRobin();
        results.add(scheduler.printResults("Round Robin"));

        System.out.println("Summary:");
        System.out.println("-----------------------------------------------------------");
        System.out.printf("%-25s | %-16s | %s\n", "Algorithm", "Avg Turnaround", "Throughput");
        System.out.println("-----------------------------------------------------------");

        for (Result r : results) {
            System.out.printf("%-25s | %16.2f | %9.4f\n", r.algorithm, r.avgTurnaround, r.throughput);
        }

        System.out.println("-----------------------------------------------------------");
    }

}