package model

sealed trait OpenGraphElement

case class Basic(
  title: String,
  `type`: String,
  image: String,
  url: String
) extends OpenGraphElement

case class OpenGraphRequest(url: String)


