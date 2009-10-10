import com.skype._
import com.skype.connector.Connector

class SkypeConnector {
  
  
  // converter for anonymous func to ChatMessageListener
  implicit def func2ChatMessageListener(funcs:(
    (ChatMessage)=>Unit,	/* received func */
    (ChatMessage)=>Unit,	/* sent func */
    (ChatMessage)=>Unit		/* edited func */
  	)) = {
      new ChatMessageListener() {
        def chatMessageReceived(m:ChatMessage) = funcs._1.apply(m)
        def chatMessageSent(m:ChatMessage) = funcs._2.apply(m)
        def chatMessageEdited(m:ChatMessage) = funcs._3.apply(m)
      }
  }
  def connect {
    Connector.getInstance().setApplicationName("SkypeMessagePong,ScalaVersion");
    Skype.setDeamon(false)
    Skype.setDebug(true)
    Skype.addChatMessageListener((
      (m:ChatMessage) => { println("message received from" + m.getSenderDisplayName + ": " + m.getContent) },
      (m:ChatMessage) => { println("message sent: " + m.getContent)},
      (m:ChatMessage) => { println("message edited: " + m.getContent) }
    ))
    
  }

}
