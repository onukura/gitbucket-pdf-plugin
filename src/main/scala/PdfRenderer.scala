import gitbucket.core.controller.Context
import gitbucket.core.plugin.{RenderRequest, Renderer}

import play.twirl.api.Html
import scala.util.{Failure, Success, Try}

class PdfRenderer extends Renderer {

  def render(request: RenderRequest): Html = {
    import request._
    Html(Try(toHtml()(context)) match {
      case Success(v) => v
      case Failure(e) => s"""<h2>Error</h2><div><pre>$e</pre></div>"""
    })
  }

  def shutdown(): Unit = {
  }

  def toHtml()(implicit context: Context): String = {
    val path = context.baseUrl
    val url = context.currentPath

    s"""
       |<script src="$path/plugin-assets/pdfjs/style.js"></script>
       |<iframe style="width: 100%; height: 600px; border-width: 0;" src="$path/plugin-assets/pdfjs/web/viewer.html?file=$url?raw%3Dtrue">
       |""".stripMargin

  }

}