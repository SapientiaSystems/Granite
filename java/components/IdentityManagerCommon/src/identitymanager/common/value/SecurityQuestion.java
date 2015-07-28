// ##########################################################################
// # File Name:	SecurityQuestion.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerCommon Framework.
// #
// #   			The IdentityManagerCommon Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerCommon Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerCommon
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.common.value;

import strata1.common.utility.ICopyable;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class SecurityQuestion
    implements ICopyable,Comparable<SecurityQuestion>
{
    private final Integer itsQuestionId;
    private final String  itsQuestion;
    private final String  itsAnswer;
    
    /************************************************************************
     * Creates a new SecurityQuestion. 
     *
     * @param questionId
     * @param question
     * @param answer
     */
    public 
    SecurityQuestion(
        Integer questionId,
        String  question,
        String  answer)
    {
        itsQuestionId = questionId;
        itsQuestion   = question;
        itsAnswer     = answer;
    }

    /************************************************************************
     * Creates a new SecurityQuestion. 
     *
     * @param other
     */
    public 
    SecurityQuestion(SecurityQuestion other)
    {
        this( other.itsQuestionId,other.itsQuestion,other.itsAnswer );
    }
    
    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public ICopyable 
    copy()
    {
        return new SecurityQuestion( this );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public int 
    compareTo(SecurityQuestion other)
    {
        int result =
             itsQuestionId.compareTo( other.itsQuestionId );
        
        if ( result != 0 )
            return result;
        
        result = 
            itsQuestion.compareToIgnoreCase( other.itsQuestion );
        
        if ( result != 0 )
            return result;
        
        result = 
            itsAnswer.compareToIgnoreCase( other.itsAnswer );
        
        return result;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    equals(Object other)
    {
        if ( other instanceof SecurityQuestion )
            return compareTo( (SecurityQuestion)other ) == 0;
        
        return false;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public int 
    hashCode()
    {
        int hash = 31;
        
        hash = 31 * hash + itsQuestionId.hashCode();
        hash = 31 * hash + itsQuestion.hashCode();
        hash = 31 * hash + itsAnswer.hashCode();
        
        return hash;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public String 
    toString()
    {
        StringBuilder builder = new StringBuilder();
        
        builder
            .append( "QuestionId: " )
            .append( itsQuestionId )
            .append( '\n' )
            .append( "  Question: " )
            .append( itsQuestion )
            .append( '\n' )
            .append( "    Answer: " )
            .append( itsAnswer );
        
        return builder.toString();
    }

    /************************************************************************
     *  
     *
     * @return
     */
    public Integer
    getQuestionId()
    {
        return itsQuestionId;
    }
    
    /************************************************************************
     *  
     *
     * @return
     */
    public String
    getQuestion()
    {
        return itsQuestion;
    }
    
    /************************************************************************
     *  
     *
     * @return
     */
    public String
    getAnswer()
    {
        return itsAnswer;
    }
}

// ##########################################################################
