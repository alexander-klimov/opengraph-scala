package model

sealed trait OpenGraphElement

case class Basic(
  title: String,
  `type`: String,
  image: String,
  url: String
) extends OpenGraphElement

case class MetaData(
  audio: String,
  description: String,
  determiner: String,
  locale: String,
  localeAlternative: List[String],
  siteName: String,
  video: String
)

case class OpenGraphRequest(url: String)


