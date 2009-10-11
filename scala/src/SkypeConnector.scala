import com.skype._
import com.skype.connector.Connector

object SkypeConnector {
  
  
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
  
  def msgHandler(m:ChatMessage) {
    m.getStatus match {
      case ChatMessage.Status.RECEIVED => println("message received from" + m.getSenderDisplayName + ": " + m.getContent)
      case ChatMessage.Status.SENT => println("message sent: " + m.getContent)
    }
  }
  
  def connect {
    Connector.getInstance().setApplicationName("SkypeMessagePong,ScalaVersion");
    Skype.setDeamon(false)
    Skype.setDebug(true)
    Skype.addChatMessageListener((
      msgHandler,
      msgHandler,
      (m:ChatMessage) => { println("message edited: " + m.getContent) }
    ))
    
  }

}
