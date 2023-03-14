package edu.neu.coe.csye7200.asstmd
import spray.json._
import scala.annotation.tailrec

case class Artist(id: String, name: String, followers: Option[Int])

case class Track(name: String, duration_ms: Long, artists: Seq[Artist])

case class Item(track: Track)

case class PlaylistResponse(items: Seq[Item], limit: Int, total: Int, next: Option[String])

object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val artistFormat: RootJsonFormat[Artist] = new RootJsonFormat[Artist] {
        override def write(obj: Artist): JsValue = {
            val baseMap = Map(
                "id" -> JsString(obj.id),
                "name" -> JsString(obj.name)
            )
            val followersMap = obj.followers.map(f => Map("followers" -> JsNumber(f))).getOrElse(Map.empty)
            JsObject(baseMap ++ followersMap)
        }

        def read(json: JsValue): Artist = {
            val fields = json.asJsObject.fields
            val id = fields("id").convertTo[String]
            val name = fields("name").convertTo[String]
            val followers = fields.get("followers").flatMap {
                case JsObject(followersFields) => followersFields.get("total").map(_.convertTo[Int])
                case _ => None
            }
            Artist(id, name, followers)
        }
    }
    implicit val trackFormat: RootJsonFormat[Track] = jsonFormat3(Track)
    implicit val itemFormat: RootJsonFormat[Item] = jsonFormat1(Item)
    implicit val playlistResponseFormat: RootJsonFormat[PlaylistResponse] = jsonFormat4(PlaylistResponse)
}

object SpotifyApi extends App {
    val OAuth = "Bearer insert_your_token_here"
    val url = "https://api.spotify.com/v1/playlists/5Rrf7mqN8uus2AaQQQNdc1/tracks?offset=0&limit=100"
    val headers = Map(
        "Authorization" -> OAuth,
    )

    import MyJsonProtocol._

    @tailrec
    def getAllTracks(url: String, tracks: Seq[Track]): Seq[Track] = {
        val response = requests.get(url, headers = headers)
        val json = response.text.parseJson
        val playlistResponse = json.convertTo[PlaylistResponse]
        val allTracks = tracks ++ playlistResponse.items.map(_.track)

        playlistResponse.next match {
            case Some(nextUrl) => getAllTracks(nextUrl, allTracks)
            case None => allTracks
        }
    }

    val allTracks = getAllTracks(url, Seq.empty)
    val top10LongestTracks = allTracks.sortBy(_.duration_ms)(Ordering.Long.reverse).take(10)
    top10LongestTracks.foreach(track => println(s"Name: ${track.name}, Duration: ${track.duration_ms} ms"))

    val top10LongestTracksWithArtists = top10LongestTracks.map(track => {
        val artists = track.artists.map(artist => {
            val artistUrl = s"https://api.spotify.com/v1/artists/${artist.id}"
            val response = requests.get(artistUrl, headers = headers)
            val json = response.text.parseJson
            val artistObj = json.convertTo[Artist]
            (artistObj, track.duration_ms)
        })
        (track, artists)
    })

    val sortedArtists = top10LongestTracksWithArtists.flatMap(_._2).map(_._1).distinct.sortBy(_.followers.getOrElse(0))(Ordering.Int.reverse)
    println("------------")
    sortedArtists.foreach(artist => println(s"Name: ${artist.name}, Followers: ${artist.followers}"))
}
