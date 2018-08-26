package org.fogbeam.hatteras.soa_simulator;

import javax.jms.JMSException
import javax.jms.Message
import javax.jms.Session

public class SimulatorProcessingThread implements Runnable
{
	boolean stopFlag = false;
	def jmsTemplate;
	
	def customerNames = ["XJR374298":"Plutonium Candles, Inc", 
						 "P97R8423X":"Magnesium Sprockets, LLC", 
						 "37XJ9BR73":"Titanium Masks, Inc",
						 "AL9999999":"Aluminum Hoses, LLC" ];	
	
	def regions = [ "west", "north", "south", "east"];
	
	def catalogIds = [ "XY933333", 
					   "BZ724345",
					   "9K111397",
					   "0000398A"];
	
	def items = [ "MK231249":7.49, 
				  "MK232167":13.98, 
				  "LQ788888":6.32, 
				  "LQ899999":11.99, 
				  "OOO73332":17.93];
	
	
	@Override
	public void run()
	{
		
		Random random = new Random();
		
		Set customerKeys = customerNames.keySet();
		Set itemKeys = items.keySet();
		
		
		SubMessageCreator msgCreator = new SubMessageCreator() {
			
			private String data;
			
			@Override
			public void setData( String data )
			{
				this.data = data;
			}
			
			public Message createMessage(Session session) throws JMSException
			{
				return session.createTextMessage( this.data );
			}
		};
		
		// TODO: send one initial batch of messages with fixed set of values so the demo will have
		// some deterministic characteristics on initial load
	
		// send AddRFQ message
		String fileName = "messages/AddRFQ.xml";
		File fXmlFile = new File(fileName);
		
		
		def ns = new groovy.xml.Namespace("http://www.openapplications.org/oagis/9", '');
		def root = new XmlParser().parse(fXmlFile);
		
		// AddRFQ\DataArea\RFQ\RFQHeader\CustomerParty\Name
		// AddRFQ\DataArea\RFQ\RFQHeader\CustomerParty\PartyIDs\ID
		// AddRFQ\DataArea\RFQ\RFQHeader\CustomerParty\Location\UserArea\Region
		
		def customerParty = root[ns.DataArea][0][ns.RFQ][0][ns.RFQHeader][0][ns.CustomerParty];
						
		def name = customerParty[ns.Name]
		name[0].value = "Boxer Steel";
		
		
		def customerId = customerParty[ns.PartyIDs][0][ns.ID][0];
		customerId.value = "CUS729897";
	
		def region = customerParty[ns.Location][0][ns.UserArea][0][ns.SalesRegion][0];
		region.value = "west";
					
		// AddRFQ\DataArea\RFQ\RFQHeader\LastModificationDateTime
		// AddRFQ\DataArea\RFQ\RFQHeader\DocumentDateTime
		def rfqHeader = root[ns.DataArea][0][ns.RFQ][0][ns.RFQHeader][0];
		def lastModificationDateTime = rfqHeader[ns.LastModificationDateTime][0];
		def documentDateTime = rfqHeader[ns.DocumentDateTime][0];
		
		lastModificationDateTime.value = now;
		documentDateTime.value = now;
		
		
		// AddRFQ\DataArea\RFQ\RFQHeader\CatalogReference\DocumentID\ID
		// AddRFQ\DataArea\RFQ\RFQLine\Item\ItemID\ID
		int randCatalogIndex = random.nextInt( 4 );
		String catalogId = catalogIds[randCatalogIndex];
		
		int randItemIndex = random.nextInt( items.size() );
		String itemKey = itemKeys[randItemIndex];
		
					
		def catalogIdElem = root[ns.DataArea][0][ns.RFQ][0][ns.RFQHeader][0][ns.CatalogReference][0][ns.DocumentID][0][ns.ID][0];
		def itemIdElem = root[ns.DataArea][0][ns.RFQ][0][ns.RFQLine][0][ns.Item][0][ns.ItemID][0][ns.ID][0];
	
		catalogIdElem.value = catalogId;
		itemIdElem.value = itemKey;
		
		String rfqDocId = java.util.UUID.randomUUID().toString();
		
		// AddRFQ\DataArea\RFQ\RFQHeader\DocumentID\ID
		def documentIdElem = root[ns.DataArea][0][ns.RFQ][0][ns.RFQHeader][0][ns.DocumentID][0][ns.ID][0];
		documentIdElem.value = rfqDocId;
		
		// AddRFQ\DataArea\RFQ\RFQLine\Quantity
		def lineQuantity = root[ns.DataArea][0][ns.RFQ][0][ns.RFQLine][0][ns.Quantity][0];
		
		int rfqLineQuantity = random.nextInt( 30 ) + 1;
		lineQuantity.value = rfqLineQuantity;
	
		
		def writer = new StringWriter()
		new XmlNodePrinter(new PrintWriter(writer)).print(root)
		def xmlString = writer.toString()
	
	
		msgCreator.setData( xmlString.trim() );
		
		jmsTemplate.send( "foobar", msgCreator );
		
		System.out.println( "sent AddRFQ" );

		
	
	
		while( stopFlag != true )
		{
			
			// generate customer / order data						
			int randKeyIndex = random.nextInt( 4 );
			String customerKey = customerKeys[randKeyIndex];
			String customerName = customerNames[customerKey];
			Date now = new Date(); 
			int randRegionKey = random.nextInt( 4 );
			String randRegion = regions[randRegionKey];
			
			
			// send AddRFQ message
			fileName = "messages/AddRFQ.xml";
			fXmlFile = new File(fileName);
			
			
			ns = new groovy.xml.Namespace("http://www.openapplications.org/oagis/9", '');
			root = new XmlParser().parse(fXmlFile);
			
			// AddRFQ\DataArea\RFQ\RFQHeader\CustomerParty\Name
			// AddRFQ\DataArea\RFQ\RFQHeader\CustomerParty\PartyIDs\ID
			// AddRFQ\DataArea\RFQ\RFQHeader\CustomerParty\Location\UserArea\Region
			
			customerParty = root[ns.DataArea][0][ns.RFQ][0][ns.RFQHeader][0][ns.CustomerParty];
							
			name = customerParty[ns.Name]
			name[0].value = customerName;
			
			
			customerId = customerParty[ns.PartyIDs][0][ns.ID][0];
			customerId.value = customerKey;

			region = customerParty[ns.Location][0][ns.UserArea][0][ns.SalesRegion][0];
			region.value = randRegion;
						
			// AddRFQ\DataArea\RFQ\RFQHeader\LastModificationDateTime
			// AddRFQ\DataArea\RFQ\RFQHeader\DocumentDateTime
			rfqHeader = root[ns.DataArea][0][ns.RFQ][0][ns.RFQHeader][0];
			lastModificationDateTime = rfqHeader[ns.LastModificationDateTime][0];
			documentDateTime = rfqHeader[ns.DocumentDateTime][0];
			
			lastModificationDateTime.value = now;
			documentDateTime.value = now;
			
			
			// AddRFQ\DataArea\RFQ\RFQHeader\CatalogReference\DocumentID\ID
			// AddRFQ\DataArea\RFQ\RFQLine\Item\ItemID\ID
			randCatalogIndex = random.nextInt( 4 );
			catalogId = catalogIds[randCatalogIndex];
			
			randItemIndex = random.nextInt( items.size() );
			itemKey = itemKeys[randItemIndex];
			
						
			catalogIdElem = root[ns.DataArea][0][ns.RFQ][0][ns.RFQHeader][0][ns.CatalogReference][0][ns.DocumentID][0][ns.ID][0];
			itemIdElem = root[ns.DataArea][0][ns.RFQ][0][ns.RFQLine][0][ns.Item][0][ns.ItemID][0][ns.ID][0];

			catalogIdElem.value = catalogId;
			itemIdElem.value = itemKey;
			
			rfqDocId = java.util.UUID.randomUUID().toString();
			
			// AddRFQ\DataArea\RFQ\RFQHeader\DocumentID\ID
			documentIdElem = root[ns.DataArea][0][ns.RFQ][0][ns.RFQHeader][0][ns.DocumentID][0][ns.ID][0];
			documentIdElem.value = rfqDocId;
			
			// AddRFQ\DataArea\RFQ\RFQLine\Quantity			
			lineQuantity = root[ns.DataArea][0][ns.RFQ][0][ns.RFQLine][0][ns.Quantity][0];
			
			rfqLineQuantity = random.nextInt( 30 ) + 1;
			lineQuantity.value = rfqLineQuantity;

			
			writer = new StringWriter()
			new XmlNodePrinter(new PrintWriter(writer)).print(root)
			xmlString = writer.toString()
			
			
			msgCreator.setData( xmlString.trim() );
			
			jmsTemplate.send( "foobar", msgCreator );
			
			System.out.println( "sent AddRFQ" );
			
			
			
			// random pause
			int pauseInterval = random.nextInt( 25000 ) + 5001;
			try
			{
				Thread.sleep( pauseInterval );
			}
			catch( InterruptedException e )
			{
				if( stopFlag == true )
				{
					break;
				}	
			}	
			
			// send AcknowledgeRFQ message
			fileName = "messages/AcknowledgeRFQ.xml";
			fXmlFile = new File(fileName);
			
			root = new XmlParser().parse(fXmlFile);
			
			
			// AcknowledgeRFQ/DataArea/RFQ/RFQHeader/CustomerParty/PartyIDs/ID
			def customerPartyIdElem = root[ns.DataArea][0][ns.RFQ][0][ns.RFQHeader][0][ns.CustomerParty][0][ns.PartyIDs][0][ns.ID][0];
			customerPartyIdElem.value = customerKey;
			
			
			// AcknowledgeRFQ/DataArea/RFQ/RFQHeader/DocumentID/ID
			documentIdElem = root[ns.DataArea][0][ns.RFQ][0][ns.RFQHeader][0][ns.DocumentID][0][ns.ID][0];
			documentIdElem.value = rfqDocId;
			
		
			writer = new StringWriter()
			new XmlNodePrinter(new PrintWriter(writer)).print(root)
			xmlString = writer.toString()
			
			msgCreator.setData( xmlString.trim() );
			
			jmsTemplate.send( "foobar", msgCreator );

			System.out.println( "sent AcknowledgeRFQ" );
		
			// random pause
			pauseInterval = random.nextInt( 25000 ) + 5001;
			try
			{
				Thread.sleep( pauseInterval );
			}
			catch( InterruptedException e )
			{
				if( stopFlag == true )
				{
					break;
				}
			}	
			
			// send RespondRFQ message
			fileName = "messages/RespondRFQ.xml";
			fXmlFile = new File(fileName);
			
			root = new XmlParser().parse(fXmlFile);
			
			// RespondRFQ/DataArea/RFQ/RFQHeader/CustomerParty/PartyIDs/ID
			customerPartyIdElem = root[ns.DataArea][0][ns.RFQ][0][ns.RFQHeader][0][ns.CustomerParty][0][ns.PartyIDs][0][ns.ID][0];
			customerPartyIdElem.value = customerKey;
			
			
			// RespondRFQ/DataArea/RFQ/RFQHeader/DocumentID/ID
			documentIdElem = root[ns.DataArea][0][ns.RFQ][0][ns.RFQHeader][0][ns.DocumentID][0][ns.ID][0];
			documentIdElem.value = rfqDocId;
			
			
			// RespondRFQ\DataArea\RFQ\RFQLine\Quantity
			// RespondRFQ\DataArea\RFQ\RFQLine\UnitPrice\Amount
			double itemPrice = items[itemKey];
			def unitPriceElem = root[ns.DataArea][0][ns.RFQ][0][ns.RFQLine][0][ns.UnitPrice][0][ns.Amount][0];
			unitPriceElem.value = itemPrice;
			
			// RespondRFQ\DataArea\RFQ\RFQHeader\ExtendedAmount
			// RespondRFQ\DataArea\RFQ\RFQHeader\TotalAmount

			def extendedAmount = root[ns.DataArea][0][ns.RFQ][0][ns.RFQHeader][0][ns.ExtendedAmount][0];
			def totalAmount = root[ns.DataArea][0][ns.RFQ][0][ns.RFQHeader][0][ns.TotalAmount][0];
			
			double extendedAmountValue = itemPrice * rfqLineQuantity;
			double totalAmountValue = extendedAmountValue * 1.10; // add tax

			totalAmount.value = totalAmountValue;
			extendedAmount.value = extendedAmountValue;
			
			
			writer = new StringWriter()
			new XmlNodePrinter(new PrintWriter(writer)).print(root)
			xmlString = writer.toString()
			
			msgCreator.setData( xmlString.trim() );
			
			jmsTemplate.send( "foobar", msgCreator );
			
			System.out.println( "sent RespondRFQ" );
			
			
			// random pause
			pauseInterval = random.nextInt( 25000 ) + 5001;
			try
			{
				Thread.sleep( pauseInterval );
			}
			catch( InterruptedException e )
			{
				if( stopFlag == true )
				{
					break;
				}
			}
			
			
			// send AddPurchaseOrder message
			fileName = "messages/AddPurchaseOrder.xml";
			fXmlFile = new File(fileName);
			
			root = new XmlParser().parse(fXmlFile);
			
			
			// populate AddPurchaseOrder...
			// AddPurchaseOrder\DataArea\PurchaseOrder\PurchaseOrderHeader\CustomerParty\PartyIDs\ID
			customerPartyIdElem = root[ns.DataArea][0][ns.PurchaseOrder][0][ns.PurchaseOrderHeader][0][ns.CustomerParty][0][ns.PartyIDs][0][ns.ID][0];
			customerPartyIdElem.value = customerKey;
			
			// AddPurchaseOrder\DataArea\PurchaseOrder\PurchaseOrderHeader\CatalogReference\DocumentID\ID
			catalogIdElem = root[ns.DataArea][0][ns.PurchaseOrder][0][ns.PurchaseOrderHeader][0][ns.CatalogReference][0][ns.DocumentID][0][ns.ID][0];
			catalogIdElem.value = catalogId;
			
			// AddPurchaseOrder\DataArea\PurchaseOrder\PurchaseOrderHeader\ExtendedAmount
			extendedAmount = root[ns.DataArea][0][ns.PurchaseOrder][0][ns.PurchaseOrderHeader][0][ns.ExtendedAmount][0];
			extendedAmount.value = extendedAmountValue;
			
			// AddPurchaseOrder\DataArea\PurchaseOrder\PurchaseOrderHeader\TotalAmount
			totalAmount = root[ns.DataArea][0][ns.PurchaseOrder][0][ns.PurchaseOrderHeader][0][ns.TotalAmount][0];
			totalAmount.value = totalAmountValue;
			
			// AddPurchaseOrder\DataArea\PurchaseOrder\PurchaseOrderHeader\QuoteReference\DocumentID\ID
			def quoteReferenceIDelem = root[ns.DataArea][0][ns.PurchaseOrder][0][ns.PurchaseOrderHeader][0][ns.QuoteReference][0][ns.DocumentID][0][ns.ID][0];
			quoteReferenceIDelem.value = rfqDocId;
			
			// AddPurchaseOrder\DataArea\PurchaseOrder\PurchaseOrderLine\Item\ItemID
			def purchaseOrderLineItemIdElem = root[ns.DataArea][0][ns.PurchaseOrder][0][ns.PurchaseOrderLine][0][ns.Item][0][ns.ItemID][0];
			purchaseOrderLineItemIdElem.value = itemKey;
			
			// AddPurchaseOrder\DataArea\PurchaseOrder\PurchaseOrderLine\Quantity
			def purchaseOrderLineQuantityElem = root[ns.DataArea][0][ns.PurchaseOrder][0][ns.PurchaseOrderLine][0][ns.Quantity][0];
			purchaseOrderLineQuantityElem.value = itemPrice;
			
			def poDocIdElem = root[ns.DataArea][0][ns.PurchaseOrder][0][ns.PurchaseOrderHeader][0][ns.DocumentID][0][ns.ID][0];
			String purchaseOrderDocId = java.util.UUID.randomUUID().toString();
			poDocIdElem.value = purchaseOrderDocId;

			
			writer = new StringWriter()
			new XmlNodePrinter(new PrintWriter(writer)).print(root)
			xmlString = writer.toString()
			
			msgCreator.setData( xmlString.trim() );
			
			jmsTemplate.send( "foobar", msgCreator );
			
			System.out.println( "sent AddPurchaseOrder" );
			
			
			// random pause
			pauseInterval = random.nextInt( 25000 ) + 5001;
			try
			{
				Thread.sleep( pauseInterval );
			}
			catch( InterruptedException e )
			{
				if( stopFlag == true )
				{
					break;
				}
			}
			
			
			// send AcknowledgePurchaseOrder message
			fileName = "messages/AcknowledgePurchaseOrder.xml";
			fXmlFile = new File(fileName);
			
			root = new XmlParser().parse(fXmlFile);
			
			// populate AcknowledgePurchaseOrder
			// AcknowledgePurchaseOrder\DataArea\PurchaseOrder\PurchaseOrderHeader\CustomerParty\PartyIDs\ID
			customerPartyIdElem = root[ns.DataArea][0][ns.PurchaseOrder][0][ns.PurchaseOrderHeader][0][ns.CustomerParty][0][ns.PartyIDs][0][ns.ID][0];
			customerPartyIdElem.value = customerKey;
			
			// AcknowledgePurchaseOrder\DataArea\PurchaseOrder\PurchaseOrderHeader\CatalogReference\DocumentID\ID
			catalogIdElem = root[ns.DataArea][0][ns.PurchaseOrder][0][ns.PurchaseOrderHeader][0][ns.CatalogReference][0][ns.DocumentID][0][ns.ID][0];
			catalogIdElem.value = catalogId;
			
			// AcknowledgePurchaseOrder\DataArea\PurchaseOrder\PurchaseOrderHeader\ExtendedAmount
			extendedAmount = root[ns.DataArea][0][ns.PurchaseOrder][0][ns.PurchaseOrderHeader][0][ns.ExtendedAmount][0];
			extendedAmount.value = extendedAmountValue;
			
			// AcknowledgePurchaseOrder\DataArea\PurchaseOrder\PurchaseOrderHeader\TotalAmount
			totalAmount = root[ns.DataArea][0][ns.PurchaseOrder][0][ns.PurchaseOrderHeader][0][ns.TotalAmount][0];
			totalAmount.value = totalAmountValue;
			
			// AcknowledgePurchaseOrder\DataArea\PurchaseOrder\PurchaseOrderHeader\QuoteReference\DocumentID\ID
			quoteReferenceIDelem = root[ns.DataArea][0][ns.PurchaseOrder][0][ns.PurchaseOrderHeader][0][ns.QuoteReference][0][ns.DocumentID][0][ns.ID][0];
			quoteReferenceIDelem.value = rfqDocId;
			
			// AcknowledgePurchaseOrder\DataArea\PurchaseOrder\PurchaseOrderLine\Item\ItemID
			purchaseOrderLineItemIdElem = root[ns.DataArea][0][ns.PurchaseOrder][0][ns.PurchaseOrderLine][0][ns.Item][0][ns.ItemID][0];
			purchaseOrderLineItemIdElem.value = itemKey;
			
			// AcknowledgePurchaseOrder\DataArea\PurchaseOrder\PurchaseOrderLine\Quantity
			purchaseOrderLineQuantityElem = root[ns.DataArea][0][ns.PurchaseOrder][0][ns.PurchaseOrderLine][0][ns.Quantity][0];
			purchaseOrderLineQuantityElem.value = itemPrice;
			
			
			writer = new StringWriter()
			new XmlNodePrinter(new PrintWriter(writer)).print(root)
			xmlString = writer.toString()
			
			msgCreator.setData( xmlString.trim() );
			
			jmsTemplate.send( "foobar", msgCreator );
			
			System.out.println( "sent AcknowledgePurchaseOrder" );
			
			
			
			// random pause
			pauseInterval = random.nextInt( 25000 ) + 5001;
			try
			{
				Thread.sleep( pauseInterval );
			}
			catch( InterruptedException e )
			{
				if( stopFlag == true )
				{
					break;
				}
			}
			
			
			
			// send AddSalesOrder message
			fileName = "messages/AddSalesOrder.xml";
			fXmlFile = new File(fileName);
			
			root = new XmlParser().parse(fXmlFile);
			
			// populate sales order 
			
			// AddSalesOrder\DataArea\SalesOrder\SalesOrderHeader\CustomerParty\PartyIDs\ID
			def customerIdElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderHeader][0][ns.CustomerParty][0][ns.PartyIDs][0][ns.ID][0];
			customerIdElem.value = customerKey;
			
			// AddSalesOrder\DataArea\SalesOrder\SalesOrderHeader\CatalogReference\DocumentID\ID
			catalogIdElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderHeader][0][ns.CatalogReference][0][ns.DocumentID][0][ns.ID][0];
			catalogIdElem.value = catalogId;
			
			// AddSalesOrder\DataArea\SalesOrder\SalesOrderHeader\ExtendedAmount
			def extendedAmountElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderHeader][0][ns.ExtendedAmount][0];
			extendedAmountElem.value = extendedAmountValue;
			
			// AddSalesOrder\DataArea\SalesOrder\SalesOrderHeader\TotalAmount
			def totalAmountElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderHeader][0][ns.TotalAmount][0];
			totalAmountElem.value = totalAmountValue;
			
			// AddSalesOrder\DataArea\SalesOrder\SalesOrderLine\Item\ItemID\ID
			def lineItemIdElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderLine][0][ns.Item][0][ns.ItemID][0][ns.ID][0];
			lineItemIdElem.value = itemKey;
			
			// AddSalesOrder\DataArea\SalesOrder\SalesOrderLine\Quantity
			def lineQuantityElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderLine][0][ns.Item][0];
			lineQuantityElem.value = rfqLineQuantity;
			
			// AddSalesOrder\DataArea\SalesOrder\SalesOrderLine\UnitPrice\Amount
			def unitPriceAmountElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderLine][0][ns.UnitPrice][0][ns.Amount][0];
			unitPriceAmountElem.value = itemPrice;
			
			
			def salesOrderDocIdElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderHeader][0][ns.DocumentID][0][ns.ID][0];
			String salesOrderDocId = java.util.UUID.randomUUID().toString();
			salesOrderDocIdElem.value = salesOrderDocId;
			
			
			writer = new StringWriter()
			new XmlNodePrinter(new PrintWriter(writer)).print(root)
			xmlString = writer.toString()
			
			msgCreator.setData( xmlString.trim() );
			
			jmsTemplate.send( "foobar", msgCreator );
			
			System.out.println( "sent AddSalesOrder" );
			
	
			// random pause
			pauseInterval = random.nextInt( 25000 ) + 5001;
			try
			{
				Thread.sleep( pauseInterval );
			}
			catch( InterruptedException e )
			{
				if( stopFlag == true )
				{
					break;
				}
			}
			
			
			// send AcknowledgeSalesOrder message
			fileName = "messages/AcknowledgeSalesOrder.xml";
			fXmlFile = new File(fileName);
			
			root = new XmlParser().parse(fXmlFile);
			
			// populate AcknowledgeSalesOrder
			
			// AcknowledgeSalesOrder\DataArea\SalesOrder\SalesOrderHeader\CustomerParty\PartyIDs\ID
			customerIdElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderHeader][0][ns.CustomerParty][0][ns.PartyIDs][0][ns.ID][0];
			customerIdElem.value = customerKey;
			
			// AcknowledgeSalesOrder\DataArea\SalesOrder\SalesOrderHeader\CatalogReference\DocumentID\ID
			catalogIdElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderHeader][0][ns.CatalogReference][0][ns.DocumentID][0][ns.ID][0];
			catalogIdElem.value = catalogId;
			
			// AcknowledgeSalesOrder\DataArea\SalesOrder\SalesOrderHeader\ExtendedAmount
			extendedAmountElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderHeader][0][ns.ExtendedAmount][0];
			extendedAmountElem.value = extendedAmountValue;
			
			// AcknowledgeSalesOrder\DataArea\SalesOrder\SalesOrderHeader\TotalAmount
			totalAmountElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderHeader][0][ns.TotalAmount][0];
			totalAmountElem.value = totalAmountValue;
			
			// AcknowledgeSalesOrder\DataArea\SalesOrder\SalesOrderLine\Item\ItemID\ID
			lineItemIdElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderLine][0][ns.Item][0][ns.ItemID][0][ns.ID][0];
			lineItemIdElem.value = itemKey;
			
			// AcknowledgeSalesOrder\DataArea\SalesOrder\SalesOrderLine\Quantity
			lineQuantityElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderLine][0][ns.Item][0];
			lineQuantityElem.value = rfqLineQuantity;
			
			// AcknowledgeSalesOrder\DataArea\SalesOrder\SalesOrderLine\UnitPrice\Amount
			unitPriceAmountElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderLine][0][ns.UnitPrice][0][ns.Amount][0];
			unitPriceAmountElem.value = itemPrice;
			
			
			writer = new StringWriter()
			new XmlNodePrinter(new PrintWriter(writer)).print(root)
			xmlString = writer.toString()
			
			msgCreator.setData( xmlString.trim() );
			
			jmsTemplate.send( "foobar", msgCreator );
			
			
			System.out.println( "sent AcknowledgeSalesOrder" );
			
			
			
			// random pause
			pauseInterval = random.nextInt( 25000 ) + 5001;
			try
			{
				Thread.sleep( pauseInterval );
			}
			catch( InterruptedException e )
			{
				if( stopFlag == true )
				{
					break;
				}
			}
			
			
			// send ProcessSalesOrder message
			fileName = "messages/ProcessSalesOrder.xml";
			fXmlFile = new File(fileName);
			
			root = new XmlParser().parse(fXmlFile);
			
			// populate ProcessSalesOrder
			
			// ProcessSalesOrder\DataArea\SalesOrder\SalesOrderHeader\CustomerParty\PartyIDs\ID
			customerIdElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderHeader][0][ns.CustomerParty][0][ns.PartyIDs][0][ns.ID][0];
			customerIdElem.value = customerKey;
			
			// ProcessSalesOrder\DataArea\SalesOrder\SalesOrderHeader\CatalogReference\DocumentID\ID
			catalogIdElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderHeader][0][ns.CatalogReference][0][ns.DocumentID][0][ns.ID][0];
			catalogIdElem.value = catalogId;
			
			// ProcessSalesOrder\DataArea\SalesOrder\SalesOrderHeader\ExtendedAmount
			extendedAmountElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderHeader][0][ns.ExtendedAmount][0];
			extendedAmountElem.value = extendedAmountValue;
			
			// ProcessSalesOrder\DataArea\SalesOrder\SalesOrderHeader\TotalAmount
			totalAmountElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderHeader][0][ns.TotalAmount][0];
			totalAmountElem.value = totalAmountValue;
			
			// ProcessSalesOrder\DataArea\SalesOrder\SalesOrderLine\Item\ItemID\ID
			lineItemIdElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderLine][0][ns.Item][0][ns.ItemID][0][ns.ID][0];
			lineItemIdElem.value = itemKey;
			
			// ProcessSalesOrder\DataArea\SalesOrder\SalesOrderLine\Quantity
			lineQuantityElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderLine][0][ns.Item][0];
			lineQuantityElem.value = rfqLineQuantity;
			
			// ProcessSalesOrder\DataArea\SalesOrder\SalesOrderLine\UnitPrice\Amount
			unitPriceAmountElem = root[ns.DataArea][0][ns.SalesOrder][0][ns.SalesOrderLine][0][ns.UnitPrice][0][ns.Amount][0];
			unitPriceAmountElem.value = itemPrice;
			
			
			writer = new StringWriter()
			new XmlNodePrinter(new PrintWriter(writer)).print(root)
			xmlString = writer.toString()
			
			msgCreator.setData( xmlString.trim() );
			
			jmsTemplate.send( "foobar", msgCreator );
			
			System.out.println( "sent ProcessSalesOrder" );
			
			
			// random pause
			pauseInterval = random.nextInt( 25000 ) + 5001;
			try
			{
				Thread.sleep( pauseInterval );
			}
			catch( InterruptedException e )
			{
				if( stopFlag == true )
				{
					break;
				}
			}
			
			
			// send ProcessShipment message
			fileName = "messages/ProcessShipment.xml";
			fXmlFile = new File(fileName);
			
			root = new XmlParser().parse(fXmlFile);
			
			
			// populate ProcessShipment
			
			// ProcessShipment\DataArea\Shipment\ShipmentItem\ItemID\ID
			def shipmentItemIdElem = root[ns.DataArea][0][ns.Shipment][0][ns.ShipmentItem][0][ns.ItemID][0][ns.ID][0];
			shipmentItemIdElem.value = itemKey;
			
			// ProcessShipment\DataArea\Shipment\ShipmentItem\PurchaseOrderReference\
			def purchaseOrderReferenceElem = root[ns.DataArea][0][ns.Shipment][0][ns.ShipmentItem][0][ns.PurchaseOrderReference][0]; 
			def poRefDocIdElem = purchaseOrderReferenceElem[ns.DocumentID][0][ns.ID][0];
			poRefDocIdElem.value = purchaseOrderDocId;
			
			// ProcessShipment\DataArea\Shipment\ShipmentItem\SalesOrderReference\
			def salesOrderReferenceElem = root[ns.DataArea][0][ns.Shipment][0][ns.ShipmentItem][0][ns.SalesOrderReference][0];
			def salesOrderRefDocIdElem = salesOrderReferenceElem[ns.DocumentID][0][ns.ID][0];
			salesOrderRefDocIdElem.value = salesOrderDocId;
			
			
			
			writer = new StringWriter()
			new XmlNodePrinter(new PrintWriter(writer)).print(root)
			xmlString = writer.toString()
			
			msgCreator.setData( xmlString.trim() );
			
			jmsTemplate.send( "foobar", msgCreator );
			
			
			System.out.println( "sent ProcessShipment" );
			
			
			
			println "done sending this batch of messages";	
			pauseInterval = 480000;
			try
			{
				Thread.sleep( pauseInterval );
			}
			catch( InterruptedException e )
			{
				if( stopFlag == true )
				{
					break;
				}
			}
			
			
		}
		
	}
		
}
