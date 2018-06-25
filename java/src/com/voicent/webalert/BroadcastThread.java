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

import java.util.ArrayList;


public class BroadcastThread extends Thread {
    private Voicent voicent_ = null;
    private BroadcastList list_ = null;
    private String notes_ = null;
    private boolean isOngoing_ = false;
    private int callsMade_ = 0;
    private int callsFailed_ = 0;
    private int lines_ = 1;
    private ArrayList curReqIds_ = new ArrayList();

    public BroadcastThread() {
        // We use only one gateway in this sample, but you can create
        // multiple Voicent object for multiple gateways
        voicent_ = new Voicent();

        // set the number of phone lines available
        // this number can be fetched from Voicent Gateway
        // it is hard coded in this sample.
        // it is fine if the number does not match the actual phone lines
        // since the gateway has a call scheduler to decide which calls to make
        lines_ = 4;
    }

    public boolean startBroadcast(BroadcastList list, String notes) {
        if (isOngoing_)
            return false;

        list_ = list;
        notes_ = notes;

        callsMade_ = 0;
        callsFailed_ = 0;
        curReqIds_.clear();

        isOngoing_ = true;
        start();

        return true;
    }

    public boolean stopBroadcast() {
        isOngoing_ = false;
        return true;
    }

    /**
     * this is a simple call dispatcher, there is no need to schedule the call
     * since the gateway has a call scheduler. for client, it needs to send the
     * call request, poll the call status, wait if the call is not finished, or
     * remove the call record if the call is completed.
     * <p>
     * if you send more calls than the available phone lines on the gateway, the
     * extra calls will be queued by the gateway scheduler.
     */
    public void run() {
        // keep loop until stopped or there are current calls
        while (isOngoing_ || curReqIds_.size() > 0) {
            // any room for more call
            while (isOngoing_ && curReqIds_.size() < lines_) {
                if (!list_.next()) { // end of call list
                    isOngoing_ = false;
                    break;
                }
                // submit call request to gateway
                String phoneno = list_.getValue(BroadcastList.PHONE);
                String reqId = voicent_.callText(phoneno, notes_, false);
                curReqIds_.add(reqId);

                // message will be included in the gateway output.log
                System.out.println("<<<<<<<<<< " + phoneno + " : " + reqId);
            }

            // check status
            boolean hasFinishedCall = false;
            int index = 0;
            while (index < curReqIds_.size()) {
                String reqId = (String) curReqIds_.get(index);
                String status = voicent_.callStatus(reqId);
                if (status.length() == 0) {
                    index++;
                    continue; // not finished yet
                }

                System.out.println(">>>>>>>>>> " + status + " : " + reqId);
                if ("Call Made".equals(status))
                    callsMade_++;
                else
                    callsFailed_++;
                hasFinishedCall = true;

                curReqIds_.remove(index);
                voicent_.callRemove(reqId);
            }

            // Voicent Gateway is like a web server, you have to poll in order to get status
            // if no finished calls, wait 10 seconds before continue
            if (!hasFinishedCall) {
                try {
                    sleep(10000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * get current total calls made
     */
    public int getCallsMade() {
        return callsMade_;
    }

    /**
     * get current total calls failed
     */
    public int getCallsFailed() {
        return callsFailed_;
    }

    /**
     * get current total calls in progress
     */
    public int getCallsInProgress() {
        return curReqIds_.size();
    }

    /**
     * get current total calls to be made
     */
    public int getCallsToBeMade() {
        return list_.getTotal() - callsMade_ - callsFailed_ - curReqIds_.size();
    }

    public boolean isOngoing() {
        return isOngoing_;
    }
}
