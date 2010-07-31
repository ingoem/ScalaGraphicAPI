package graphic

import com.jogamp.opengl.util.FPSAnimator
import com.jogamp.opengl.util.Animator
import java.awt._
import javax.media.opengl.GLAutoDrawable
import javax.media.opengl.GLCapabilities
import javax.media.opengl.GLEventListener
import javax.media.opengl.GLProfile
import javax.media.opengl.awt.{GLCanvas => JOGLCanvas}
import javax.swing.JFrame
import com.jogamp.opengl.util.texture.TextureIO
import java.awt.font.FontRenderContext
import java.io.IOException
import java.io.InputStream

abstract class Demo extends JFrame {
  val canvas = new GLCanvas  
  
  var t0, lastFPSUpdate = System.nanoTime
  var t = 0
  var t1 = 0L
  var framesCounter = 0L
  var fpsCounter = 0.0

  def textOutline(f: Font, str: String, x: Int, y: Int): Shape =
    f.createGlyphVector(new FontRenderContext(null, false, false), str).getOutline(x,y)

  def loadImage(name: String, sufix: String): GLImage = {
    val f: InputStream = getClass.getResourceAsStream(name)
    try {
      val img = TextureIO.newTexture(f, true, sufix)
      return new GLImage(img)
    } catch {
      case ioe: IOException => {
          error("Image loading: can't find file "+name+"\n"+ioe.toString)
        }
      case e: Exception => { error("Image loading: " + e.toString) }
    }
    return null
  }

  def draw(canvas: GLCanvas)
}