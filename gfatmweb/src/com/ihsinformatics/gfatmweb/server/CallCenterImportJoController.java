/**
 * 
 */
package com.ihsinformatics.gfatmweb.server;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @author Shujaat
 *
 */
public class CallCenterImportJoController {

	private static Scheduler importScheduler;

	public static void main(String[] args) {

		CallCenterImportJoController importer = new CallCenterImportJoController();
		
		try {
		
			importer.createJobs();
		
		} catch (SchedulerException e1) {
			e1.printStackTrace();
		}
	}

	public void createJobs() throws SchedulerException {

		importScheduler = StdSchedulerFactory.getDefaultScheduler();
		JobDetail dailyJob  = JobBuilder.newJob(CallCenterImportJob.class)
				.withIdentity("dailyJob").build();

		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity("dailyJobExecute")
				.startNow()
				.withSchedule(CronScheduleBuilder.cronSchedule("0 0 22 ? * MON,TUE,WED,THU,FRI,SAT,SUN *"))
				.forJob("dailyJob")
				.build();
		importScheduler.scheduleJob(dailyJob, trigger);
		importScheduler.start();
	}
}
