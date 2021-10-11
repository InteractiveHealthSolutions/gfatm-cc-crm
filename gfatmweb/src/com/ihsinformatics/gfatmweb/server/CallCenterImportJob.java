/**
 * 
 */
package com.ihsinformatics.gfatmweb.server;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author Shujaat
 *
 */
public class CallCenterImportJob implements Job {
	private Logger logger;

	public CallCenterImportJob() {

		logger = Logger.getLogger(CallCenterWebService.class);
	}
	@Override
	public void execute(JobExecutionContext jobContext)
			throws JobExecutionException {

		CallCenterWebService webService = new CallCenterWebService();
		logger.error(webService.precondition());

	}

}
