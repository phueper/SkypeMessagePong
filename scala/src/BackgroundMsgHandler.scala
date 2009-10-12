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
              val rePlay = new Regex(".*will (.*) spielen.*");
              val reViel = new Regex(".*viel viel");
              m.getContent match {
                case rePlay(what) => {
                  m.getChat.send("Aber ich hab keine Lust " + what + " zu spielen")
                }
                case reViel() => {
                  m.isEditable
                  for (i <- 0 until 7 ) {
                  if (/*m.isEditable*/ true) {
                    val newContent = m.getContent.replaceFirst("viel viel", "viel viel viel")
                    m.setContent(newContent)
                  }
                  }
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
