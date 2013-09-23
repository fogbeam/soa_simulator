package org.fogbeam.hatteras.soa_simulator;

import org.springframework.jms.core.MessageCreator;

public interface SubMessageCreator extends MessageCreator
{
	public void setData( final String data );
}