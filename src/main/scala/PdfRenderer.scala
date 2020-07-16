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

    s"""
       |<link rel="stylesheet" type="text/css" href="$path/plugin-assets/pdfjs/style.css">
       |<script src="$path/plugin-assets/pdfjs/build/pdf.js"></script>
       |<div>
       |  <button id="prev">Previous</button>
       |  <button id="next">Next</button>
       |  &nbsp; &nbsp;
       |  <span>Page: <span id="page_num"></span> / <span id="page_count"></span></span>
       |</div>
       |<canvas id="the-canvas"></canvas>
       |<script>
       |// If absolute URL from the remote server is provided, configure the CORS
       |// header on that server.
       |var url = '${context.request.getRequestURL + "?raw=true"}';
       |
       |// Loaded via <script> tag, create shortcut to access PDF.js exports.
       |var pdfjsLib = window['pdfjs-dist/build/pdf'];
       |
       |// The workerSrc property shall be specified.
       |pdfjsLib.GlobalWorkerOptions.workerSrc = "$path/plugin-assets/pdfjs/build/pdf.worker.js";
       |
       |var pdfDoc = null,
       |    pageNum = 1,
       |    pageRendering = false,
       |    pageNumPending = null,
       |    scale = 0.8,
       |    canvas = document.getElementById('the-canvas'),
       |    ctx = canvas.getContext('2d');
       |
       |/**
       | * Get page info from document, resize canvas accordingly, and render page.
       | * @param num Page number.
       | */
       |function renderPage(num) {
       |  pageRendering = true;
       |  // Using promise to fetch the page
       |  pdfDoc.getPage(num).then(function(page) {
       |    var viewport = page.getViewport({scale: scale});
       |    canvas.height = viewport.height;
       |    canvas.width = viewport.width;
       |
       |    // Render PDF page into canvas context
       |    var renderContext = {
       |      canvasContext: ctx,
       |      viewport: viewport
       |    };
       |    var renderTask = page.render(renderContext);
       |
       |    // Wait for rendering to finish
       |    renderTask.promise.then(function() {
       |      pageRendering = false;
       |      if (pageNumPending !== null) {
       |        // New page rendering is pending
       |        renderPage(pageNumPending);
       |        pageNumPending = null;
       |      }
       |    });
       |  });
       |
       |  // Update page counters
       |  document.getElementById('page_num').textContent = num;
       |}
       |
       |/**
       | * If another page rendering in progress, waits until the rendering is
       | * finised. Otherwise, executes rendering immediately.
       | */
       |function queueRenderPage(num) {
       |  if (pageRendering) {
       |    pageNumPending = num;
       |  } else {
       |    renderPage(num);
       |  }
       |}
       |
       |/**
       | * Displays previous page.
       | */
       |function onPrevPage() {
       |  if (pageNum <= 1) {
       |    return;
       |  }
       |  pageNum--;
       |  queueRenderPage(pageNum);
       |}
       |document.getElementById('prev').addEventListener('click', onPrevPage);
       |
       |/**
       | * Displays next page.
       | */
       |function onNextPage() {
       |  if (pageNum >= pdfDoc.numPages) {
       |    return;
       |  }
       |  pageNum++;
       |  queueRenderPage(pageNum);
       |}
       |document.getElementById('next').addEventListener('click', onNextPage);
       |
       |/**
       | * Asynchronously downloads PDF.
       | */
       |pdfjsLib.getDocument(url).promise.then(function(pdfDoc_) {
       |  pdfDoc = pdfDoc_;
       |  document.getElementById('page_count').textContent = pdfDoc.numPages;
       |
       |  // Initial/first page rendering
       |  renderPage(pageNum);
       |});
       |</script>
       |""".stripMargin

  }

}