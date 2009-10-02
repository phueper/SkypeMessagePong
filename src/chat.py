# ----------------------------------------------------------------------------------------------------
#  Python / Skype4Py example that prints out chat messages
#
#  Tested with  Skype4Py version 0.9.28.5 and Skype verson 3.8.0.96

import sys
import re
import Skype4Py

from background import Background

# ----------------------------------------------------------------------------------------------------
# Fired on attachment status change. Here used to re-attach this script to Skype in case attachment is lost. Just in case.
def OnAttach(status):
    print 'API attachment status: ' + skype.Convert.AttachmentStatusToText(status)
    if status == Skype4Py.apiAttachAvailable:
        skype.Attach();

    if status == Skype4Py.apiAttachSuccess:
        print('******************************************************************************');


# ----------------------------------------------------------------------------------------------------
# Fired on chat message status change. 
# Statuses can be: 'UNKNOWN' 'SENDING' 'SENT' 'RECEIVED' 'READ'        

rePlay = re.compile(".*will (.*) spielen.*")
reViel = re.compile(".*viel viel")

def OnMessageStatus(Message, Status):
    B = Background()
    Marker = ' ';
    if Message.IsEditable:
        Marker = '* ';
    if (Status == 'RECEIVED' or Status == 'SENT'):
        mP = rePlay.match(Message.Body)
        mV = reViel.match(Message.Body)
        if (mP):
            Message.Chat.SendMessage('Aber ich hab keine Lust '+mP.group(1)+' zu spielen.')
        elif (mV and Message.IsEditable):
            def repViel(m):
                print "repViel"
                x = re.sub("viel viel", "viel viel viel", m.Body)
                print "attempted viel viel replacement: " + x
                m.Body = x
            print "found viel viel"
            for i in range(7):
                print i
                B.runLater(2*i, lambda m: repViel(m), {'m':Message})
        elif (Message.Body == 'xyzzy'):
                B.runLater(2, lambda m: m.Chat.SendMessage(m.Body+"?"), {'m':Message})
        elif (Message.Body == '===='):
            Message.Chat.SendMessage('. \n\n\n\n----------------------------\n\n\n\n\n ... sooo weit bin ich noch nicht.')
    if Status == 'RECEIVED':
        print(Message.FromDisplayName + ': ' + Message.Body);
    elif Status == 'SENT':
        print('Myself:' + Marker + Message.Body);
    else:
        print(Message.FromDisplayName + '(' + Message.FromHandle + ')' + Status + ':' + Marker + Message.Body);


# ----------------------------------------------------------------------------------------------------
# Creating instance of Skype object, assigning handler functions and attaching to Skype.
skype = Skype4Py.Skype();
skype.OnAttachmentStatus = OnAttach;
skype.OnMessageStatus = OnMessageStatus;


print('******************************************************************************');
print 'Connecting to Skype..'
skype.Attach();

# ----------------------------------------------------------------------------------------------------
# Looping until user types 'exit'
Cmd = '';
while not Cmd == 'exit':
    Cmd = raw_input('');
