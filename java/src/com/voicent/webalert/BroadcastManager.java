/*
 * Voicent Communucations, Inc Sample Code
 * http://www.voicent.com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL VOICENT COMMUNICATIONS, INC OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * ----------------------------------------------------------------------
 */
 
package com.voicent.webalert;


public class BroadcastManager
{
    /**
    * Constructor with default localhost:8155
    */
    BroadcastManager()
    {    
        bt_ = new BroadcastThread();
    }

    /**
    * Get the singleton instance
    */
    public static BroadcastManager getInstance()
    {
        if (inst_ == null)
            inst_ = new BroadcastManager();

        return inst_; 
    }
    
    /**
     * Set the current call list
     * @param filename CSV file name of the call list
     */
    public void setCallList(String filename)
    {
        list_ = new BroadcastListFile(filename);
    }
      
    /**
    * Start a background thread to manage calls
    * @param notes alert message
    */
    public boolean startBroadcast(String notes)
    {   
        return bt_.startBroadcast(list_, notes);
    }
      
    /**
    * Stop broadcast
    */
    public boolean stopBroadcast()
    {   
        return bt_.stopBroadcast();
    }

    /**
    * get current total calls made
    */
    public int getCallsMade()
    {
        return bt_.getCallsMade();
    }
      
    /**
    * get current total calls failed
    */
    public int getCallsFailed()
    {
    return bt_.getCallsFailed();
    }
      
    /**
    * get current total calls in progress
    */
    public int getCallsInprogress()
    {
        return bt_.getCallsInProgress();
    }
      
    /**
    * get current total calls to be made
    */
    public int getCallsToBeMade()
    {
        return bt_.getCallsToBeMade();
    }

    /* test usage */
    public static void main(String args[])
        throws InterruptedException
    {
        BroadcastManager manager = BroadcastManager.getInstance();
        manager.startBroadcast("SOS SOS");
        
        do {
            Thread.currentThread().sleep(20000);
            
            System.out.println("Calls Made       : " + Integer.toString(manager.getCallsMade()));
            System.out.println("Calls Failed     : " + Integer.toString(manager.getCallsFailed()));
            System.out.println("Calls Inprogress : " + Integer.toString(manager.getCallsInprogress()));
            System.out.println("Calls To be Made : " + Integer.toString(manager.getCallsToBeMade()));
            
        }
        while (manager.getCallsInprogress() > 0);
    }

    private static BroadcastManager inst_ = null;
    private BroadcastListFile list_ = null;
    private BroadcastThread bt_ = null;
}
