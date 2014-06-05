package com.vaavud.server.monitoring;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class MemoryMonitorJob implements Job {

	//-------------------------------------------------------------------------
	// Class fields
	//-------------------------------------------------------------------------

	private static final Logger logger = Logger.getLogger(MemoryMonitorJob.class);

	private static final long COLLECTION_TIME_INCREASE_THRESHOLD_MS = 30000;
	private static final long HEAP_DUMP_INTERVAL_MS = 60L * 60L * 1000L; // 1 hour
	
	private static final String COLLECTION_COUNT_KEY = "concurrentMarkSweepCollectionCount";
	private static final String COLLECTION_TIME_KEY = "concurrentMarkSweepCollectionTime";
	private static final String HEAP_DUMP_TIMESTAMP_KEY = "heapDumpTimestamp";
	
	//-------------------------------------------------------------------------
	// Instance methods
	//-------------------------------------------------------------------------

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			
			if (!"true".equalsIgnoreCase(context.getJobDetail().getJobDataMap().getString("hasPrinted"))) {
				context.getJobDetail().getJobDataMap().put("hasPrinted", "true");

				// first printing to log...
				
				for (GarbageCollectorMXBean garbageCollectorMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
					logger.info("GB." + garbageCollectorMXBean.getName() + ": " + Arrays.toString(garbageCollectorMXBean.getMemoryPoolNames()));
				}
			}
			
			Long concurrentMarkSweepCollectionCount = null;
			Long concurrentMarkSweepCollectionTime = null;
			
			MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
			logger.info(toString("Memory.Heap", memoryMXBean.getHeapMemoryUsage()));
			logger.info(toString("Memory.NonHeap", memoryMXBean.getNonHeapMemoryUsage()));
			
			for (MemoryPoolMXBean memoryPoolMXBean : ManagementFactory.getMemoryPoolMXBeans()) {
				String name = "Pool." + memoryPoolMXBean.getName() + " (" + (memoryPoolMXBean.getType() == MemoryType.HEAP ? "Heap" : "NonHeap") + ")";
				logger.info(toString(name, memoryPoolMXBean.getPeakUsage()));
				memoryPoolMXBean.resetPeakUsage();
			}
			
			for (GarbageCollectorMXBean garbageCollectorMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
				String name = "GB." + garbageCollectorMXBean.getName();
				logger.info(name + ": collectionCount=" + garbageCollectorMXBean.getCollectionCount() + ", collectionTime=" + garbageCollectorMXBean.getCollectionTime());
				
				// for concurrent mark-sweep collector, get collection count and time for further processing below...
				if ("ConcurrentMarkSweep".equals(garbageCollectorMXBean.getName())) {
					concurrentMarkSweepCollectionCount = garbageCollectorMXBean.getCollectionCount();
					concurrentMarkSweepCollectionTime = garbageCollectorMXBean.getCollectionTime();
				}
			}

			ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
			logger.info("ClassLoading: " +
					    "loaded=" + classLoadingMXBean.getLoadedClassCount() + ", " +
					    "totalLoaded=" + classLoadingMXBean.getTotalLoadedClassCount() + ", " +
					    "unloaded=" + classLoadingMXBean.getUnloadedClassCount());
			
			// analyze concurrent mark-sweep collection count and time to see if it going out of hand...
			if (concurrentMarkSweepCollectionCount != null && concurrentMarkSweepCollectionTime != null) {
				
				JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();

				// get old collection count and time
				Long oldConcurrentMarkSweepCollectionCount = jobDataMap.getString(COLLECTION_COUNT_KEY) == null ? null : jobDataMap.getLongFromString(COLLECTION_COUNT_KEY);
				Long oldConcurrentMarkSweepCollectionTime = jobDataMap.getString(COLLECTION_TIME_KEY) == null ? null :  jobDataMap.getLongFromString(COLLECTION_TIME_KEY);

				if (oldConcurrentMarkSweepCollectionCount != null && oldConcurrentMarkSweepCollectionTime != null) {
					
					// compute collection count increase and time increase
					long collectionCountIncrease = concurrentMarkSweepCollectionCount - oldConcurrentMarkSweepCollectionCount;
					long collectionTimeIncrease = concurrentMarkSweepCollectionTime - oldConcurrentMarkSweepCollectionTime;
					
					logger.info("ConcurrentMarkSweep: collectionCountIncrease=" + collectionCountIncrease + ", collectionTimeIncrease=" + collectionTimeIncrease);

					// see if we already took a heap dump and get the time at which that happened
					Date oldHeapDumpTimestamp = (Date) jobDataMap.get(HEAP_DUMP_TIMESTAMP_KEY);
					
					// take a heap dump if: (1) collection time threshold exceeded, (2) grace period has elapsed since last heap dump
					if (collectionTimeIncrease >= COLLECTION_TIME_INCREASE_THRESHOLD_MS &&
						(oldHeapDumpTimestamp == null || (new Date().getTime() - oldHeapDumpTimestamp.getTime() > HEAP_DUMP_INTERVAL_MS))) {
						
						logger.error("Garbage collection threshold exceeded");
					}
				}
				
				// store collection count and time
				context.getJobDetail().getJobDataMap().putAsString(COLLECTION_COUNT_KEY, concurrentMarkSweepCollectionCount);
				context.getJobDetail().getJobDataMap().putAsString(COLLECTION_TIME_KEY, concurrentMarkSweepCollectionTime);
			}
		}
		catch (RuntimeException e) {
			logger.error("Error getting JVM info: ", e);
		}
	}
	
	private static long toMB(long b) {
		return b / (1024*1024);
	}
	
	private static String toString(String name, MemoryUsage memoryUsage) {
		return String.format("%1$-35s %2$3d%%, init:%3$6dM, used:%4$6dM, committed:%5$6dM, max:%6$5dM",
				             name,
				             memoryUsage.getUsed() * 100L / memoryUsage.getMax(),
				             toMB(memoryUsage.getInit()),
				             toMB(memoryUsage.getUsed()),
				             toMB(memoryUsage.getCommitted()),
				             toMB(memoryUsage.getMax()));
	}
}
