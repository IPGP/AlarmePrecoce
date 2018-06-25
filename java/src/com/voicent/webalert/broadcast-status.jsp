<%
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
     */
%>


<%@ page import="com.voicent.webalert.BroadcastManager" %>

<%
    BroadcastManager manager = BroadcastManager.getInstance();
%>

<html>

<head>
    <meta http-equiv="Content-Language" content="en-us">
    <meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
    <title>Voicent Emergency Alert System</title>
    <script>
        <!--
        var tleft = 10;

        function beginrefresh() {
            if (!document.images)
                return;
            if (tleft == 1)
                window.location.reload();
            else {
                tleft--;
                window.status = "page rerefresh in " + tleft + " seconds";
                setTimeout("beginrefresh()", 1000)
            }
        }

        window.onload = beginrefresh
        //-->
    </script>
</head>

<body>
<H1><span style="font-family: Impact; font-size: large; "><span style="background-color: #FFFF00">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Emergency Telephone Broadcast&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span>
</H1>
<table border="1" cellpadding="0" cellspacing="0" style="border-collapse: collapse; border-color: #008080;" width="54%"
       id="AutoNumber1">
    <tr>
        <td width="100%">
            <table border="0" cellpadding="0" cellspacing="8" style="border-collapse: collapse; border-color: #111111"
                   width="100%" id="AutoNumber2" height="153">
                <tr>
                    <td width="100%" bgcolor="#97C2C4" height="22"><span style="color: #FFFFFF; ">
                        Broadcast Status</span></td>
                </tr>
                <tr>
                    <td width="100%" height="115">
                        <table border="0" cellpadding="0" cellspacing="0"
                               style="border-collapse: collapse; border-color: #111111;" width="100%" id="AutoNumber3">
                            <tr>
                                <td width="33%" align="right">
                                    <span style="font-family: Impact; font-size: x-small; ">Calls Made</span></td>
                                <td width="4%" align="left">
                                    &nbsp;
                                </td>
                                <td width="63%" align="left">
                                    <%=Integer.toString(manager.getCallsMade())%>
                                </td>
                            </tr>
                            <tr>
                                <td width="33%" align="right">
                                    <span style="font-family: Impact; font-size: x-small; ">Calls Failed</span></td>
                                <td width="4%" align="left">
                                    &nbsp;
                                </td>
                                <td width="63%" align="left">
                                    <%=Integer.toString(manager.getCallsFailed())%>
                                </td>
                            </tr>
                            <tr>
                                <td width="33%" align="right">
                                    <span style="font-family: Impact; font-size: x-small; ">Calls In Progress</span>
                                </td>
                                <td width="4%" align="left">
                                    &nbsp;
                                </td>
                                <td width="63%" align="left">
                                    <%=Integer.toString(manager.getCallsInprogress())%>
                                </td>
                            </tr>
                            <tr>
                                <td width="33%" align="right">
                                    <span style="font-family: Impact; font-size: x-small; ">Calls To be Made</span></td>
                                <td width="4%" align="left">
                                    &nbsp;
                                </td>
                                <td width="63%" align="left">
                                    <%=Integer.toString(manager.getCallsToBeMade())%>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
<% if (manager.getCallsToBeMade() > 0) { %>
<form method="POST" action="stop-broadcast.jsp">
    <p>
        <input type="submit" value="Stop Broadcast" name="stop"/>
    </p>
</form>
<% } %>
</body>

</html>