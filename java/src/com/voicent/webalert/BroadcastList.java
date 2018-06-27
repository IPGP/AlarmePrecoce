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
 * BroadcastList is the interface for call list
 * Implement this interface to suit you need. For example, you can
 * setup a mysql based call list and implement a db broadcast list class.
 *
 * In this sample, we use a file based call list.
 */

package com.voicent.webalert;


public interface BroadcastList {
    String NAME = "name";
    String PHONE = "phone";

    /**
     * move the record forward
     */
    boolean next();

    /**
     * get the current record values
     *
     * @param name parameter name
     */
    String getValue(String name);

    /**
     * total number of records
     */
    int getTotal();

    /**
     * move the current record to the beginning
     */
    void reset();
}
