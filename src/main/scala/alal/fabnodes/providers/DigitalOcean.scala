package alal.fabnodes

import scala.util.control.Breaks._
import scalaj.http.{HttpRequest, HttpConstants}
import Util._


object DigitalOcean extends Provider {
  import org.json4s._
  import org.json4s.JsonDSL._
  import org.json4s.native.JsonMethods._
  implicit val formats = DefaultFormats

  val endpointFrame = "https://api.digitalocean.com"
  val endpointList = "/v2/droplets?per_page=200"
  val endpointSshkeys ="/v2/account/keys"
  val endpointCreate = "/v2/droplets"
  val endpointDelete = "/v2/droplets/"
  val endpointAction= "/v2/droplets/"
  val clientActions = Map("reboot" -> Map("type" -> "power_cycle"),
                          "rebuild" -> Map( "type"-> "rebuild",
                                            "image" -> "ubuntu-16-04-x64"),
                          "shutdown" -> Map("type" -> "power_off"),
                          "boot" -> Map("type" -> "power_on")
                          )
  val token = sys.env.get("DO_TOKEN")
                         .getOrElse(throw new IllegalArgumentException(
                                    "Set your token to env var DO_TOKEN"))


  object JNode2Node {
    def apply(node:JValue) = {
      val `id` = (node \ "id").extract[String]
      val `name` = (node \ "name").extract[String]
      val `status` = (node \ "status").extract[String]
      val `size` = (node \ "size_slug").extract[String]
      val `region` = (node \ "region" \ "slug").extract[String]

      var ip_t = ""
      try {
        val allIps = (node \ "networks" \ "v4" \ "ip_address")
                      .extract[List[String]]
        ip_t = allIps.filter(_.take(3) != "10.")(0)
      } catch {
        case e: MappingException =>
          ip_t = (node \ "networks" \ "v4" \ "ip_address").extract[String]
      }
      val `ip` = ip_t

      new Node(id, name, status, size, region, ip)
    }
  }


  private def authReq(method: String, endpoint: String) = {
    Http.createHttp(method)(endpointFrame + endpoint)
                   .header("Authorization", s"Bearer $token")
  }


  private def _list = {
    var page = 1
    var total = 0
    var from = 0
    val nodes = new collection.mutable.ArrayBuffer[Node]

    do{
      val resp = authReq("GET", endpointList + "&page=" + page).asString
      val jResp = parse(resp.body)
      val jNodes = (jResp \ "droplets" )
      total = (jResp \ "meta" \ "total").extract[Int]
      breakable { for (i <- 0 until total.min(200)) {
        nodes += JNode2Node(jNodes(i))
        from += 1
        if ( from >= total ) break
      } }
      page += 1
    }while( from < total )
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
    val sshkeyid = sys.env.get("DO_SSHKEYID")
                          .getOrElse(throw new IllegalArgumentException(
                                      "Set ssh key id to env var SSHKEYID!"))

    val regions = "nyc2 nyc3 sfo1 nyc1 sgp1".split(' ')

    val newNodeNames = makeNames(prefix, start, end)
    println(newNodeNames.mkString("\n"))

    if (confirm) {
      for ((nodeName, region) <- (newNodeNames
                                  zip List.fill(end)(regions).flatten)){

        println(s"${nodeName} at ${region}")

        val _json = s"""{"name":"${nodeName}","region":"${region}",""" +
                     s""""size":"512mb","image":"ubuntu-15-10-x64",""" +
                     s""""ssh_keys":[${sshkeyid}],"backups":false,""" +
                     s""""ipv6":false,"user_data":null,""" +
                     s""""private_networking":null}"""

        val resp = authReq("POST", endpointCreate)
                          .postData(_json)
                          .header("content-type", "application/json")
                          .asString

        println(resp.body)
      }
    }
  }


  override def delete(prefix: String, start: Int, end: Int): Unit = {
    val nodes = _list
    val targetNodes = {
      val desiredNames = makeNames(prefix, start, end)
      nodes.filter((i: Node) => desiredNames.contains(i.name))
    }

    showFab(targetNodes)
    if (confirm) {
      for (node <- targetNodes){
        val resp = authReq("DELETE", endpointDelete + node.id)
                          .header("content-type", "application/json")
                          .asString

        println(f"${node.name} delete returns ${resp.code}")
      }
    }
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
        val resp = authReq("POST", endpointAction + node.id + "/actions")
                          .postData(compact(render(clientActions(act))))
                          .header("content-type", "application/json")
                          .asString
        println(f"${node.name} ${act} returns ${resp.code}")
        println(f"${resp.body}")
      }
    }
  }
}
