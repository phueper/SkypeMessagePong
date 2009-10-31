import com.skype._
import scala.actors._
import scala.actors.Actor._

class Pong {
  
  case class BallPosition(var x:Int, var y:Int)
  case class BallSpeed(var v_x:Int, var v_y:Int)
  
  private[this] var fieldMsg:ChatMessage=null
  private[this] var p1Msg:ChatMessage=null
  private[this] var p2Msg:ChatMessage=null
  
  private[this] var ballPosition = BallPosition(1, 1)
  private[this] var ballSpeed = BallSpeed(1, 0)
  
  private[this] var chat:Chat=null
  
  private[this] var gameFinished = false;
  
  final val fieldWidth = 21
  final val fieldHeight = 11
  final val paddle = "===="
  final val ball = "o"
  
  def init(p1Msg:ChatMessage) {
    this.p1Msg = p1Msg
    chat = p1Msg.getChat()
    fieldMsg = chat.send("... ok ... lets play pong...")
      draw
//      draw
//      draw
    if (!fieldMsg.isEditable) {
      chat.send("SORRY... the message is not editable, i'm afraid we can't play...")
    } else {
      loopWhile(!gameFinished) {
        draw
        //receiveWithin(1000) _
        /* yes, i know... Thread.sleep is supposedly not a good idea in scala... 
         * still using it... somehow receiveWithin doesnt do the same thing */
        Thread.sleep(500)
      }
    }
  }
  
  /* fill string s with spaces until length len*/
  private def fillString(s:String, len:Int):String = {
    var rval = s
    while (rval.length < len) {
      rval += " "
    }
    rval
  }
  
  /* returns a string of length len, with s in the "center" of the string */
  private def center(s:String, len:Int):String = {
    var rval:String = "";
    rval = " " * ((len / 2) - (s.length / 2))
    rval += s
    fillString(rval, len)
  }
  
  /* draw the Pong Field */
  private def draw = {
    println("BallPosition: " + ballPosition)
    var pongField:String = "playing pong\n \n \n"
    /* first line... p1 paddle*/
    var line = center(paddle, fieldWidth)
    pongField += line + "\n"
    for (y <- 1 until fieldHeight) {
      line = fillString("|", fieldWidth - 1) + "|\n"
      /* hmm... should we draw the ball ??? */
      if (y == ballPosition.y) {
        line = line.substring(0,ballPosition.x - ball.length) + ball + line.substring(ballPosition.x)
        /* now that we've drawn the ball let's calculate its next position */
        ballPosition.x = ballPosition.x + ballSpeed.v_x
        ballPosition.y = ballPosition.y + ballSpeed.v_y
        /* and check wether it hit the wall... then we need to invert the speed */
        if (ballPosition.x >= fieldWidth - 1) ballSpeed.v_x = ballSpeed.v_x * -1
        if (ballPosition.x <= 1) ballSpeed.v_x = ballSpeed.v_x * -1
        /* TODO... y speed calc */
      }
      pongField += line
    }
    /* last line... p2 paddle*/
    line = center(paddle, fieldWidth)
    pongField += line + "\n"
    fieldMsg.setContent(pongField)
  }

}
