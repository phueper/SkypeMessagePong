import com.skype._
import scala.actors._
import scala.actors.Actor._
import scala.util.matching._

object BackgroundMsgHandler extends Actor {
  
  def act {
    loop {
      receive {
        case m:ChatMessage => {
          val rePlay = new Regex(".*will (.*) spielen.*");
          m.getStatus match {
            case ChatMessage.Status.RECEIVED => {
              println("message received from" + m.getSenderDisplayName + " : " + m.getContent)
              m.getContent match {
                case rePlay(what) => {
                  m.getChat.send("Aber ich hab keine Lust " + what + " zu spielen")
                }
                case "xyzzy" => {
                  m.getChat.send("xyzzy?")
                }
                case "====" => {
                  val pong = new Pong()
                  pong.init(m)
                }
              }
            }
            case ChatMessage.Status.SENT => { println("message sent...")
              val reViel = new Regex(".*viel viel.*");
              m.getContent match {
                case rePlay(what) => {
                  m.getChat.send("Aber ich hab keine Lust " + what + " zu spielen")
                }
                case reViel() => {
                  for (i <- 0 until 7 ) {
                  if (m.isEditable) {
                    val newContent = m.getContent.replaceFirst("viel viel", "viel viel viel")
                    m.setContent(newContent)
                  }
                  }
                }
                case "xyzzy" => {
                  m.getChat.send("xyzzy?")
                }
                case "====" => {
                  val pong = new Pong()
                  pong.init(m)
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
