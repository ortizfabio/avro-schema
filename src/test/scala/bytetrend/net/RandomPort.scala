package bytetrend.net

import java.io.IOException
import java.net.ServerSocket

import org.slf4j.Logger

import scala.util.{Failure, Success, Try}

trait RandomPort {

  val logger: Logger

  def port(count: Int): Try[Int] = {
    var socket: ServerSocket = null
    try {
      socket = new ServerSocket(0)
      socket.setReuseAddress(true)
      Success(socket.getLocalPort())
    } catch {
      case e: IOException => Failure(e)
    } finally {
      if (socket != null)
        try {
          socket.close()
        } catch {
          case _ :Throwable =>
        }
    }
  }
}
