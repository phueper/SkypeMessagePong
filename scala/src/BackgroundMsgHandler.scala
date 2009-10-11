import com.skype._
import scala.actors._
import scala.actors.Actor._
import scala.util.matching._

object BackgroundMsgHandler extends Actor {
  
  def act {
    loop {
      receive {
        case m:ChatMessage => {
          m.getStatus match {
		      case ChatMessage.Status.RECEIVED => {
		        println("message received from" + m.getSenderDisplayName)
		        m.getContent match {
		          case _ => println(m.getContent)
		        }
		      }
		      case ChatMessage.Status.SENT => { println("message sent...")
		    	var rP = new Regex(""".*will (.*) spielen.*""");
                m.getContent match {
		          case rP(what) => {
		            m.getChat.send("Aber ich hab keine Lust " + what + " zu spielen")
		          }
		          case "xyzzy" => {
		        	  m.getChat.send("xyzzy?")
		          }
		          case _ => println(m.getContent)
		        }
		      }
		    }
        }
      }
    }
  }

}
