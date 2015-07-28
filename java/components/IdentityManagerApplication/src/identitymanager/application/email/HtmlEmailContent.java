// ##########################################################################
// # File Name:	HtmlEmailContent.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerApplication Framework.
// #
// #   			The IdentityManagerApplication Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerApplication Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerApplication
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.application.email;

import org.w3c.tidy.Tidy;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class HtmlEmailContent
    implements IEmailContent
{
    private String itsHtml;
    
    /************************************************************************
     * Creates a new HtmlEmailContent. 
     *
     */
    public 
    HtmlEmailContent()
    {
        this( null );
    }
    
    /************************************************************************
     * Creates a new HtmlEmailContent. 
     *
     * @param html
     */
    public 
    HtmlEmailContent(String html)
    {
        setHtml( html );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    isText()
    {
        return false;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    isHtml()
    {
        return true;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    isComposite()
    {
        return false;
    }

    /************************************************************************
     *  
     *
     * @param text
     */
    public void
    setHtml(String html)
    {
        Tidy                  tidy = new Tidy();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream           ps   = new PrintStream(baos);

        tidy.setShowWarnings( false );
        tidy.parse(new StringReader(html),ps);
        
        if ( tidy.getParseErrors() > 0 )
            throw 
                new IllegalArgumentException(tidy.getErrout().toString());
        
        itsHtml = html;
     }
    
    /************************************************************************
     *  
     *
     * @return
     */
    public String
    getHtml()
    {
        return itsHtml;
    }
}

// ##########################################################################
