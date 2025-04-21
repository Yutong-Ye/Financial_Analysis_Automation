import java.util.*;
import java.util.concurrent.TimeUnit;

class Job implements Comparable<Job> {
    protected int arrTime;
    protected int cpuBurst;
    protected int priority;
    protected int exitTime;
    protected int turnAroundTime;
    protected int remainingTime;
    protected int jobId;

    private static int nextId = 1;

    public Job(int arrTime, int cpuBurst, int priority) {
        this.arrTime = arrTime;
        this.cpuBurst = cpuBurst;
        this.priority = priority;
        this.remainingTime = cpuBurst;
        this.jobId = nextId++;
    }

    @Override
    public int compareTo(Job other) {
        return Integer.compare(this.arrTime, other.arrTime);
    }

    public static Comparator<Job> byCpuBurst() {
        return Comparator.comparingInt(j -> j.cpuBurst);
    }

    public static Comparator<Job> byRemainingTime() {
        return Comparator.comparingInt(j -> j.remainingTime);
    }

    public static Comparator<Job> byPriority() {
        return Comparator.comparingInt(j -> j.priority);
    }

    public void calculateTurnAroundTime() {
        this.turnAroundTime = this.exitTime - this.arrTime;
    }

    @Override
    public String toString() {
        return String.format("Job %2d: AT=%3d, CB=%2d, P=%d, ET=%3d, TAT=%3d",
                jobId, arrTime, cpuBurst, priority, exitTime, turnAroundTime);
    }
}

public class JobScheduler {
    private List<Job> jobs;
    private int timeQuantum;

    public JobScheduler(int numJobs, int maxArrivalTime, int maxCpuBurst, int maxPriority) {
        Random rand = new Random();
        jobs = new ArrayList<>();
        for (int i = 0; i < numJobs; i++) {
            int arrTime = rand.nextInt(maxArrivalTime) + 1;
            int cpuBurst = rand.nextInt(maxCpuBurst - 1) + 2;
            int priority = rand.nextInt(maxPriority) + 1;
            jobs.add(new Job(arrTime, cpuBurst, priority));
        }
        Collections.sort(jobs); // Sort by arrival time initially
        timeQuantum = 2; // Default time quantum for Round Robin
    }

    public void setTimeQuantum(int quantum) {
        this.timeQuantum = quantum;
    }

    public void resetJobs() {
        for (Job job : jobs) {
            job.remainingTime = job.cpuBurst;
            job.exitTime = 0;
            job.turnAroundTime = 0;
        }
    }

    public void printJobs() {
        System.out.println("Initial Job Information:");
        System.out.println("ID | Arrival | CPU Burst | Priority");
        for (Job job : jobs) {
            System.out.printf("%2d | %7d | %9d | %8d\n",
                    job.jobId, job.arrTime, job.cpuBurst, job.priority);
        }
        System.out.println();
    }

    public void runFIFO() {
        resetJobs();
        int currentTime = 0;
        for (Job job : jobs) {
            if (currentTime < job.arrTime) {
                currentTime = job.arrTime;
            }
            currentTime += job.remainingTime;
            job.remainingTime = 0;
            job.exitTime = currentTime;
            job.calculateTurnAroundTime();
        }
    }

    public void runShortestJob() {
        resetJobs();
        int currentTime = 0;
        PriorityQueue<Job> queue = new PriorityQueue<>(Job.byCpuBurst());
        List<Job> completedJobs = new ArrayList<>();
        List<Job> jobList = new ArrayList<>(jobs);

        while (!jobList.isEmpty() || !queue.isEmpty()) {
            // Add arrived jobs to queue
            while (!jobList.isEmpty() && jobList.get(0).arrTime <= currentTime) {
                queue.add(jobList.remove(0));
            }

            if (queue.isEmpty()) {
                currentTime = jobList.get(0).arrTime;
                continue;
            }

            Job currentJob = queue.poll();
            currentTime += currentJob.remainingTime;
            currentJob.remainingTime = 0;
            currentJob.exitTime = currentTime;
            currentJob.calculateTurnAroundTime();
            completedJobs.add(currentJob);
        }

        jobs = completedJobs;
    }

    public void runShortestRemainingJob() {
        resetJobs();
        int currentTime = 0;
        PriorityQueue<Job> queue = new PriorityQueue<>(
                Comparator.comparingInt((Job j) -> j.remainingTime)
                        .thenComparingInt(j -> j.arrTime));
        List<Job> jobList = new ArrayList<>(jobs);
        List<Job> completedJobs = new ArrayList<>();

        while (!jobList.isEmpty() || !queue.isEmpty()) {
            // Add arrived jobs to queue
            while (!jobList.isEmpty() && jobList.get(0).arrTime <= currentTime) {
                queue.add(jobList.remove(0));
            }

            if (queue.isEmpty()) {
                currentTime = jobList.get(0).arrTime;
                continue;
            }

            Job currentJob = queue.poll();
            int timeSlice = 1;
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

    public void runHighestPriority() {
        resetJobs();
        int currentTime = 0;
        PriorityQueue<Job> queue = new PriorityQueue<>(
                Comparator.comparingInt((Job j) -> -j.priority) // Higher priority first
                        .thenComparingInt(j -> j.arrTime));
        List<Job> jobList = new ArrayList<>(jobs);
        List<Job> completedJobs = new ArrayList<>();

        while (!jobList.isEmpty() || !queue.isEmpty()) {
            // Add arrived jobs to queue
            while (!jobList.isEmpty() && jobList.get(0).arrTime <= currentTime) {
                queue.add(jobList.remove(0));
            }

            if (queue.isEmpty()) {
                currentTime = jobList.get(0).arrTime;
                continue;
            }

            Job currentJob = queue.poll();
            currentTime += currentJob.remainingTime;
            currentJob.remainingTime = 0;
            currentJob.exitTime = currentTime;
            currentJob.calculateTurnAroundTime();
            completedJobs.add(currentJob);
        }

        jobs = completedJobs;
    }

    public void runRoundRobin() {
        resetJobs();
        int currentTime = 0;
        Queue<Job> queue = new LinkedList<>();
        List<Job> jobList = new ArrayList<>(jobs);
        List<Job> completedJobs = new ArrayList<>();

        while (!jobList.isEmpty() || !queue.isEmpty()) {
            // Add arrived jobs to queue
            while (!jobList.isEmpty() && jobList.get(0).arrTime <= currentTime) {
                queue.add(jobList.remove(0));
            }

            if (queue.isEmpty()) {
                currentTime = jobList.get(0).arrTime;
                continue;
            }

            Job currentJob = queue.poll();
            int timeSlice = Math.min(timeQuantum, currentJob.remainingTime);
            currentJob.remainingTime -= timeSlice;
            currentTime += timeSlice;

            if (currentJob.remainingTime == 0) {
                currentJob.exitTime = currentTime;
                currentJob.calculateTurnAroundTime();
                completedJobs.add(currentJob);
            } else {
                // Add any new jobs that arrived during this time slice
                while (!jobList.isEmpty() && jobList.get(0).arrTime <= currentTime) {
                    queue.add(jobList.remove(0));
                }
                queue.add(currentJob);
            }
        }

        jobs = completedJobs;
    }

    public void printResults(String algorithmName) {
        System.out.println(algorithmName + " Results:");
        for (Job job : jobs) {
            System.out.println(job);
        }
        
        double avgTurnAround = jobs.stream().mapToInt(j -> j.turnAroundTime).average().orElse(0);
        int totalCpuBurst = jobs.stream().mapToInt(j -> j.cpuBurst).sum();
        int makespan = jobs.stream().mapToInt(j -> j.exitTime).max().orElse(0);
        double throughput = (double)jobs.size() / makespan;
        
        System.out.printf("Average Turnaround Time: %.2f\n", avgTurnAround);
        System.out.printf("Throughput: %.4f jobs per unit time\n", throughput);
        System.out.println();
    }

    public static void main(String[] args) {
        JobScheduler scheduler = new JobScheduler(25, 250, 15, 5);
        scheduler.printJobs();

        // Run FIFO
        scheduler.runFIFO();
        scheduler.printResults("FIFO");

        // Run Shortest Job
        scheduler.runShortestJob();
        scheduler.printResults("Shortest Job First");

        // Run Shortest Remaining Job
        scheduler.runShortestRemainingJob();
        scheduler.printResults("Shortest Remaining Time First");

        // Run Highest Priority
        scheduler.runHighestPriority();
        scheduler.printResults("Highest Priority First");

        // Run Round Robin
        scheduler.setTimeQuantum(2);
        scheduler.runRoundRobin();
        scheduler.printResults("Round Robin (Quantum=2)");
    }
}