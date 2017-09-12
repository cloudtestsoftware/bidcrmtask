package com.service.jobs;


import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cms.service.template.TemplateTable;


public class AutomationTaskService{
	
	static Log logger = LogFactory.getLog(AutomationTaskService.class);

    private final Timer timer = new Timer();

    private final AtomicLong jobsCounter = new AtomicLong();

    public final ExecutorService executor = Executors.newFixedThreadPool(10);

    private final LinkedList<AutomationEvent> events= new LinkedList<AutomationEvent>();
    
    private final Object lock = new Object();
    
    private static EventAutomationDao dao=new EventAutomationDao();

    private int maxJobCount = 1000;

    private long compactionIntervall = 5000;
   
   
    public static void main(String[] args){
    	
    	//executeJob();
    	
    	
    }
    
    public ExecutorService getExecutorService() {
    	return executor;
    }
    
    private final class AddProfilingEventCommand implements Runnable {

	private final AutomationEvent event;
	
	

	public AddProfilingEventCommand(final AutomationEvent event) {
	    this.event = event;
	}

	@Override
	public void run() {
	    // Add profiling event for compaction
	    synchronized (lock) {
	    	System.out.println("****Adding event***");
	    	events.add(event);
		//events.put(event.getId(), event.getTime());
	    }
	    jobsCounter.decrementAndGet();
	}

    }

    private final class CompactionTask extends TimerTask {
    
	@Override
	public void run() {
	    // do compaction
		
		AutomationEvent event=null;
	    synchronized (lock) {
	    	logger.info("--");   
			logger.info("START AutomationTask - waiting add profing events jobs : " + jobsCounter.get());
			logger.info("Events Size : " + events.size());
			
			//while(!events.isEmpty()){
			 event=events.poll();
			
		}
		if(event!=null){
			logger.info("Events key : " + event.getId());
			event.executeTask();
		}
		if(events.isEmpty()){
			
			executeJob();
			
		}
		
		//events.clear();
		logger.info("STOP AutomationTask - waiting add profing events jobs :" + jobsCounter.get());
		System.out.println("--");
		
	    }
	//}

    }

    public void start() {
	final CompactionTask task = new CompactionTask();
	timer.schedule(task, 1000, compactionIntervall);
    }

    public void stop() {
	timer.cancel();
	  executor.shutdown();
    }

    public void addProfilingEvent(AutomationEvent event) {
	if (jobsCounter.get() < maxJobCount) {
	    jobsCounter.incrementAndGet();
	    final AddProfilingEventCommand command = new AddProfilingEventCommand(
		    event);
	    executor.execute(command);
	   
	} else {
	    System.out.println("Too much Jobs !!!");
	}
    }

    public void setMaxJobCount(int maxJobCount) {
	this.maxJobCount = maxJobCount;
    }

    public int getMaxJobCount() {
	return maxJobCount;
    }

    public void setCompactionIntervall(long compactionIntervall) {
	this.compactionIntervall = compactionIntervall;
    }

    public long getCompactionIntervall() {
	return compactionIntervall;
    }

    public int getEventsCount() {
	synchronized (lock) {
	    return events.size();
	}
    }
    
    public long getJobsCount(){
	return jobsCounter.get();
    }
    
    public  void executeJob(){
        	
    		TemplateTable data=dao.getAutomationTasks();
    		if(data!=null && data.getRowCount()>0){
    			for(int i=0;i<data.getRowCount();i++){
    				AutomationEvent event= new AutomationEvent();
                	event.setId(data.getFieldValue("nextemailsettingid", i));
                	event.setTime(1);
                	event.setAutomationid(data.getFieldValue("automationid", i));
                	event.setAutomationname(data.getFieldValue("automationname", i));
                	event.setCampaignid(data.getFieldValue("automation2campaign", i));
                	event.setEmail(data.getFieldValue("email", i));
                	event.setChannelscode(data.getFieldValue("channelscode", i));
                	event.setEmailsubject(data.getFieldValue("emailsubject", i));
                	event.setStagecode(data.getFieldValue("stagecode", i));
                	event.setFirstname(data.getFieldValue("firstname", i));
                	event.setLastname(data.getFieldValue("lastname", i));
                	event.setWait(data.getFieldValue("wait", i));
                	event.setUrl(data.getFieldValue("url", i));
                	event.setEmailresponseid(data.getFieldValue("emailresponseid", i));
                	event.setGenuser(data.getFieldValue("genuser", i));
                	event.setGroupuser(data.getFieldValue("groupuser", i));
                	event.setNextemailsettingid(data.getFieldValue("nextemailsettingid", i));
                	event.setContenttype(data.getFieldValue("contenttype", i));
                	
                	addProfilingEvent(event);
                	
                	
    			}
    	
    		}
    		else{
    			try {
    				//this.notifyAll();
    				//this.wait(10000);
    				Thread.sleep(10000);
    				
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    		
		
    }
    		

	
}