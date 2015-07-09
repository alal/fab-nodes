package alal.fabnodes

object Util {
  case class Node(`id`: String, `name`: String, `status`: String,
                  `size`: String, `region`: String, `ip`: String)


  def confirm: Boolean ={
    if (io.StdIn.readLine("is that correct (yes/No)?") == "yes") true else {
      println("Not confirmed")
      false
    }
  }


  def makeNames(prefix: String, start: Int, end: Int) = {
    val names = new collection.mutable.ArrayBuffer[String]
    for (no <- start to end){
      val nodeName = f"${prefix}%s-${no}%d"
      names += nodeName
    }
    names.toArray
  }


  def showFab(nodes: Array[Node]) = {
    for (i <- nodes) {
      // 'root@198.211.114.120',
      println(f"'root@${i.ip + "',"}%-17s  # ${i.name}%-8s " +
              f"${i.status} ${i.size} ${i.region}")
    }
    println(s"# total num: ${nodes.length}")
  }
}
