import gitbucket.core.plugin.{AccountHook, PluginRegistry}
import gitbucket.core.service.SystemSettingsService
import io.github.gitbucket.solidbase.model.Version
import javax.servlet.ServletContext

class Plugin extends gitbucket.core.plugin.Plugin {
  override val pluginId: String = "pdf"
  override val pluginName: String = "PDF renderer Plugin"
  override val description: String = "Rendering pdf files."
  override val versions: List[Version] = List(
    new Version("0.1.0"),
  )

  private[this] var renderer: Option[PdfRenderer] = None

  override def initialize(registry: PluginRegistry, context: ServletContext, settings: SystemSettingsService.SystemSettings): Unit = {
    val test = Try{ new PdfRenderer() }
    val pdf = test.get
    registry.addRenderer("pdf", pdf)
    renderer = Option(pdf)
    super.initialize(registry, context, settings)
  }

  override def shutdown(registry: PluginRegistry, context: ServletContext, settings: SystemSettings): Unit = {
    renderer.map(r => r.shutdown())
  }

  override val assetsMappings = Seq("/pdfjs" -> "/pdfjs/assets")
}