object Chat {
  def main(args: Array[String]) {
    println("Hello World\n")
    println("connecting...\n")
    SkypeConnector.connect
    println("connected...\n")
  }
  
}