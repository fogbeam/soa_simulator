package org.fogbeam.hatteras.soa_simulator;

import groovy.xml.XmlUtil

import javax.jms.JMSException
import javax.jms.Message
import javax.jms.Session

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate

class SOASimulatorMain
{	
	public static void main( String[] args ) throws Exception
	{	
		ApplicationContext appContext = new FileSystemXmlApplicationContext( "conf/senderContext.xml" )
	
		JmsTemplate jmsTemplate = appContext.getBean( "jmsTemplate", JmsTemplate.class );
		
		// TODO: add ability to start multiple simulator threads here...
		
		SimulatorProcessingThread simulator = new SimulatorProcessingThread();
		simulator.jmsTemplate = jmsTemplate;
		
		Random random = new Random();

		while( true )
		{
			Thread simThread = new Thread( simulator );
		
			simThread.start();
		
			Thread.sleep( 240000 );
		
			simulator.stopFlag = true;
			simThread.interrupt();

			// random pause 
			int pause = random.nextInt(45)+15;
			long pauseMillis = pause*60*1000;
			Thread.sleep(pauseMillis);
					
		}
			
		System.out.println( "done" );
	}
}