package com.service.jobs;


import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cms.service.template.TemplateTable;


public class EmailListTaskService{

	static Log logger = LogFactory.getLog(EmailListTaskService.class);
	
    private final Timer timer = new Timer();

    private final AtomicLong jobsCounter = new AtomicLong();

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private final Set<String> emailresponse = new HashSet<String>();

    private final LinkedList<EmailListEvent> events= new LinkedList<EmailListEvent>();
    
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

	private final EmailListEvent event;
	
	

	public AddProfilingEventCommand(final EmailListEvent event) {
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
	    EmailListEvent event;
	    synchronized (lock) {
	    logger.info("--");   
		logger.info("START EmailListEvent Task - waiting add profing events jobs : " + jobsCounter.get());
		logger.info("Events Size : " + events.size());
		
			event=events.poll();
		}
		
	    if(event!=null){
	    	try {
	    		logger.info("Events key : " + event.getId());
				event.executeEmailTask();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
		
	   
		if(events.isEmpty()){
			
			executeJob();
			
		}
		
		
		logger.info("STOP EmailListEvent - waiting add profing events jobs :" + jobsCounter.get());
		logger.info("--");
		
	    }
	

    }

    public void start() {
	final CompactionTask task = new CompactionTask();
	timer.schedule(task, 1000, compactionIntervall);
    }

    public void stop() {
	timer.cancel();
	  executor.shutdown();
    }

    public void addProfilingEvent(EmailListEvent event) {
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
        	
    		TemplateTable data=dao.getEmailListReadyToSend();
    		if(data!=null && data.getRowCount()>0){
    			for(int i=0;i<data.getRowCount();i++){
    				EmailListEvent event= new EmailListEvent();
                	event.setId(data.getFieldValue("objid", i));
                	event.setTime(1);
                	event.setCampaignid(data.getFieldValue("emailsetting2campaign", i));
                	event.setContactlistid(data.getFieldValue("emailsetting2contactlist", i));
                	event.setEmaillistid(data.getFieldValue("contactlist2emaillist", i));
                	event.setEmailsettingid(data.getFieldValue("objid", i));
                	event.setUsername(data.getFieldValue("genuser", i));
                	event.setGroupuser(data.getFieldValue("groupuser", i));
                	event.setStagecode(data.getFieldValue("stagecode", i));
                	event.setContenttype(data.getFieldValue("contenttype", i));
                	event.setChannelscode(data.getFieldValue("channelscode", i));
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