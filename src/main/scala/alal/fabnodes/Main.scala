package alal.fabnodes
import scopt.OptionParser

case class Config(hasFilter: Boolean = false,
  prefix: String = "node", start: Int = 1, end: Int = 100,
  mode: String = "list", provider: String = "digitalocean")

object Main extends App {
  val parser = new OptionParser[Config]("fab-nodes") {
    val modes = "list create delete reboot rebuild shutdown boot sshkeys".split(' ')
    val providers = "digitalocean vultr".split(' ')
    head("fab-nodes", "0.1")

    opt[Unit]('f', "hasFilter") action { (_, c) =>
      c.copy(hasFilter = true) } text("filter list result")
    opt[Int]('s', "start") action { (x, c) =>
      c.copy(start = x) } text("start is low No.(included)")
    opt[Int]('e', "end") action { (x, c) =>
      c.copy(end = x) } text("end is the high No. (included)")
    opt[String]('p', "prefix") action { (x, c) =>
      c.copy(prefix = x) } text("prefix like production/testing")
    opt[String]('m', "mode") action { (x, c) =>
      c.copy(mode = x) } text(modes.mkString(" ")) validate { x =>
      if (modes.contains(x)) success else failure(
          s"mode must be once of ${modes.mkString("/")}")}
    opt[String]('v', "provider") action { (x, c) =>
      c.copy(provider = x) } text(providers.mkString(" ")) validate { x =>
      if (providers.contains(x)) success else failure(
          s"mode must be once of ${providers.mkString("/")}")}

    help("help") text("prints this usage text")
  }
  parser.parse(args, Config()) match {
    case Some(config) => {
      val client = config.provider match {
        case "digitalocean" => DigitalOcean
        case "vultr" => Vultr
      }
      config.mode match {
        case "list" => client.list(hasFilter=config.hasFilter,
                                   prefix=config.prefix,
                                   start=config.start,
                                   end=config.end)
        case "sshkeys" => client.sshkeys()
        case "create" => client.create(prefix=config.prefix,
                                       start=config.start,
                                       end=config.end)
        case "delete" => client.delete(prefix=config.prefix,
                                       start=config.start,
                                       end=config.end)

        case "reboot" | "rebuild" | "shutdown" | "boot" =>
          client.action(prefix=config.prefix,
                        start=config.start,
                        end=config.end,
                        act=config.mode)
      }
    }

    case None =>
  }
}
