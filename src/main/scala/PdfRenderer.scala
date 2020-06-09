import java.io

import gitbucket.core.controller.Context
import gitbucket.core.plugin.{RenderRequest, Renderer}
import gitbucket.core.service.RepositoryService.RepositoryInfo

import play.twirl.api.Html
import scala.util.{Failure, Success, Try}

class PdfRenderer extends Renderer {

  def render(request: RenderRequest): Html = {
    import request._
    Html(Try(toHtml(filePath, fileContent, branch, repository, enableWikiLink, enableRefsLink)(context)) match {
      case Success(v) => v
      case Failure(e) => s"""<h2>Error</h2><div class="ipynb-error"><pre>$e</pre></div>"""
    })
  }

  def shutdown(): Unit = {
  }

  def toHtml(
              filePath: List[String],
              content: String,
              branch: String,
              repository: RepositoryInfo,
              enableWikiLink: Boolean,
              enableRefsLink: Boolean)(implicit context: Context): String = {
    val path = context.baseUrl
    val f = new io.File(context.currentPath)
    val basename = f.getName()

    // TODO doesn't not work yet ...
    s"""
    <iframe width="100%" height="100%" src="$path/plugin-assets/pdfjs/web/viewer.html?file=${context.request.getRequestURL + "?raw=true"}">
    """

  }

}