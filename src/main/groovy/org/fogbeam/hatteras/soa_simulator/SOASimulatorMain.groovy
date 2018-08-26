package org.fogbeam.hatteras.soa_simulator;

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.context.support.FileSystemXmlApplicationContext;
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
		
		Random random = new Random();

		while( true )
		{
			Thread simThread = new Thread( simulator );
		
			simThread.start();
		
			// increase this initial delay so that (hopefully)
			// our first subscription will be loaded and waiting before we
			// send the first batch of messages.
			Thread.sleep( 720000 );
		
			simulator.stopFlag = true;
			simThread.interrupt();

			// random pause 
			int pause = random.nextInt(45)+15;
			long pauseMillis = pause*60*1000;
			System.out.println( "delay until next message batch: " + pauseMillis + " milliseconds");
			Thread.sleep(pauseMillis);
					
		}
			
		System.out.println( "done" );
	}
}