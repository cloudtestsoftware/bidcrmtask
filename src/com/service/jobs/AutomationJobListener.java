package com.service.jobs;


import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import cms.service.app.ServiceController;



public class AutomationJobListener implements ServletContextListener {
	
	
	private AutomationTask myThread = null;
	//javax.servlet.ServletConfig conf;
    public void contextInitialized(ServletContextEvent conf) {
    	
    	ServiceController.contextPath=conf.getServletContext().getRealPath("WEB-INF");
        if ((myThread == null) || (!myThread.isAlive())) {
            myThread = new AutomationTask();
            myThread.start();
        }
    }

    public void contextDestroyed(ServletContextEvent sce){
        try {
        	ArrayList <ExecutorService> services=myThread.getExecutorList();
        	for(ExecutorService service: services) {
        		service.shutdown();
        	}
        	
            myThread.interrupt();
        } catch (Exception ex) {
        }
        
    }

}
