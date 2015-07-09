package alal.fabnodes

import scalaj.http.{HttpRequest, HttpConstants, HttpOptions}
import java.net.Proxy

object Http {

  def createHttp(method: String)(url: String): HttpRequest = {

    val proxy = Proxy.NO_PROXY
    val options = HttpConstants.defaultOptions
    val charset = HttpConstants.utf8
    val sendBufferSize = 4096
    val userAgent = "fab-nodes/0.1 scalaj-http/1.0"
    val compress = true

    HttpRequest(
      url = url,
      method = method,
      connectFunc = (req, conn) => conn.connect,
      params = Nil,
      headers = Seq("User-Agent" -> userAgent),
      options = options,
      proxy = proxy,
      charset = charset,
      sendBufferSize = sendBufferSize,
      urlBuilder = (req) => HttpConstants.appendQs(req.url, req.params, req.charset),
      compress = compress
    )
     .option(HttpOptions.readTimeout(60000))
     .option(HttpOptions.connTimeout(10000))

  }

  def Get = createHttp("GET")_
  def Delete = createHttp("DELETE")_
  def Post = createHttp("POST")_
  def Head = createHttp("HEAD")_

}
