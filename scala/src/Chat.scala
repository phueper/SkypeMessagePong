object Chat {
  def main(args: Array[String]) {
    println("Hello World\n")
    val skypeConnector = new SkypeConnector();
    println("connecting...\n")
    skypeConnector.connect
    println("connected...\n")
  }
  
}