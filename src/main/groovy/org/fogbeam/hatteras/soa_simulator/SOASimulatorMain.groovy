package org.fogbeam.hatteras.soa_simulator;

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.jms.core.JmsTemplate

class SOASimulatorMain
{	
	public static void main( String[] args ) throws Exception
	{	
		ApplicationContext appContext = new ClassPathXmlApplicationContext( "senderContext.xml" )
	
		JmsTemplate jmsTemplate = appContext.getBean( "jmsTemplate", JmsTemplate.class );
		
		// TODO: add ability to start multiple simulator threads here...
		
		SimulatorProcessingThread simulator = new SimulatorProcessingThread();
		simulator.jmsTemplate = jmsTemplate;
		
		Thread simThread = new Thread( simulator );
		
		simThread.start();
		
		Thread.sleep( 240000 );
		
		simulator.stopFlag = true;
		simThread.interrupt();
		
				
		System.out.println( "done" );
	}
}