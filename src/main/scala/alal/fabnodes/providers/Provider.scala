package alal.fabnodes

abstract class Provider {
  def list(hasFilter:Boolean, prefix: String, start: Int, end: Int)
  def sshkeys()
  def create(prefix: String, start: Int, end: Int)
  def delete(prefix: String, start: Int, end: Int)
  def action(prefix: String, start: Int, end: Int, act: String)
}
