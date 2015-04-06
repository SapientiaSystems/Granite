// ##########################################################################
// # File Name:	AuthorizeNetPaymentProcessor.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerServerPlatform Framework.
// #
// #   			The IdentityManagerServerPlatform Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerServerPlatform Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerServerPlatform
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.serverplatform.authorizenetpayment;

import net.authorize.Merchant;
import net.authorize.TransactionType;
import net.authorize.aim.Result;
import net.authorize.aim.Transaction;
import net.authorize.data.Customer;
import net.authorize.data.Order;
import identitymanager.application.payment.IPaymentProcessor;
import identitymanager.service.payment.MakePaymentReply;
import identitymanager.service.payment.MakePaymentRequest;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class AuthorizeNetPaymentProcessor
    implements IPaymentProcessor
{
    private final Merchant itsMerchant;

    /************************************************************************
     * Creates a new AuthorizeNetPaymentProcessor. 
     *
     */
    public 
    AuthorizeNetPaymentProcessor(Merchant merchant)
    {
        itsMerchant = merchant;
    }


    /************************************************************************
     * {@inheritDoc} 
     */

    @SuppressWarnings("unchecked")
    @Override
    public MakePaymentReply 
    makePayment(MakePaymentRequest request)
    {   
        Transaction         transaction = null;
        Customer            customer = null;
        Order               order = null;
        Result<Transaction> result = null;
        MakePaymentReply    reply = new MakePaymentReply();
       
        transaction = 
            itsMerchant.createAIMTransaction( 
                TransactionType.CREDIT,
                null );
        customer = Customer.createCustomer();
        
        order = Order.createOrder();
        
        transaction.setCustomer( customer );
        transaction.setCreditCard( null );
        transaction.setOrder( order );
        
        result = 
            (Result<Transaction>)
                itsMerchant.postTransaction(transaction);
        
        reply.setApproved(result.isApproved());
        return reply;
    }
}

// ##########################################################################
