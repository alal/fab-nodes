package alal.fabnodes

import scalaj.http.{HttpRequest, HttpConstants}
import Util._


object Vultr extends Provider {
  import org.json4s._
  import org.json4s.JsonDSL._
  import org.json4s.native.JsonMethods._
  implicit val formats = DefaultFormats

  val endpointFrame = "https://api.vultr.com"
  val endpointList = "/v1/server/list"
  val endpointSshkeys = "/v1/sshkey/list"
  val endpointCreate = "/v1/server/create"
  val endpointAction = "/v1/server/"
  val clientActions = Map("reboot" -> "reboot",
                          "rebuild" -> "reinstall",
                          "shutdown" -> "halt",
                          "boot" -> "start",
                          "delete" -> "destroy"
                          )
  val apiKey = sys.env.get("VT_KEY")
                         .getOrElse(throw new IllegalArgumentException(
                                    "Set api key to env var VT_KEY!"))


  private def authReq(method: String, endpoint: String) = {
    Http.createHttp(method)(endpointFrame + endpoint + s"?api_key=${apiKey}")
  }


  private def _list = {
    val resp = authReq("GET", endpointList).asString
    val jResp = parse(resp.body)
    //val jResp = parse(io.Source.fromFile("vt.json").mkString)

    val ids = (jResp \\ "SUBID" \ "SUBID").extract[List[String]]
    val names = (jResp \\ "label" \ "label").extract[List[String]]
    val statuses = (jResp \\ "status" \ "status").extract[List[String]]
    val sizes = (jResp \\ "ram" \ "ram").extract[List[String]]
    val regions = (jResp \\ "location" \ "location").extract[List[String]]
    val ips = (jResp \\ "main_ip" \ "main_ip").extract[List[String]]

    val nodeNum = ids.length
    assert(nodeNum == names.length)
    assert(nodeNum == statuses.length)
    assert(nodeNum == sizes.length)
    assert(nodeNum == regions.length)
    assert(nodeNum == ips.length)

    val nodes = new collection.mutable.ArrayBuffer[Node]
    for (i <- 0 until nodeNum) {
      nodes += new Node(ids(i), names(i), statuses(i), sizes(i), 
                        regions(i), ips(i))
    }

    nodes.toArray
  }


  override def list(hasFilter: Boolean = false,
                    prefix: String = "yorg",
                    start: Int = 1,
                    end: Int = 100) = {
    val nodes = _list
    val targetNodes = if (hasFilter) {
        val desiredNames = makeNames(prefix, start, end)
        nodes.filter((i: Node) => desiredNames.contains(i.name))
      } else{
        nodes
      }

    showFab(targetNodes)
  }


  override def sshkeys(){
    val resp = authReq("GET", endpointSshkeys).asString
    println(resp.body)
  }


  override def create(prefix: String, start: Int, end: Int): Unit = {
    val sshkeyid = sys.env.get("VT_SSHKEYID")
                          .getOrElse(throw new IllegalArgumentException(
                                      "Set ssh key id to env var SSHKEYID!"))
    val regions = "1 2 3 4 5 6 7 8 9 12 24 39".split(' ')

    val newNodeNames = makeNames(prefix, start, end)
    println(newNodeNames.mkString("\n"))

    if (confirm) {
      val formData = collection.mutable.Map[String,String]()
      formData("VPSPLANID") = "201"
      formData("OSID") = "215"
      formData("SSHKEYID") = sshkeyid
      for ((nodeName, region) <- (newNodeNames
                                  zip List.fill(end)(regions).flatten)){

        println(s"${nodeName} at ${region}")

        formData("DCID") = region
        formData("label") = nodeName

        val resp = authReq("POST",endpointCreate)
                          .postForm(formData.toSeq)
                          .asString

        println(resp.body.take(80))
      }
    }
  }


  override def delete(prefix: String, start: Int, end: Int): Unit = {
    action(prefix, start, end, "delete")
  }


  override def action(prefix: String, start: Int, end: Int,
                      act:String): Unit = {
    val nodes = _list
    val targetNodes = {
      val desiredNames = makeNames(prefix, start, end)
      nodes.filter((i: Node) => desiredNames.contains(i.name))
    }

    showFab(targetNodes)
    if (confirm) {
      for (node <- targetNodes){
        val resp = authReq("POST", endpointAction + clientActions(act))
                          .postForm(Seq("SUBID" -> node.id))
                          .asString

        println(f"${node.name} ${act} returns ${resp.code}")
        println(f"${resp.body}")
      }
    }
  }
}
