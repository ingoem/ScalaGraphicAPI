package graphic

import com.sun.opengl.util.Animator
import com.sun.opengl.util.FPSAnimator
import java.awt._
import javax.media.opengl.GL
import javax.media.opengl.GL2
import javax.media.opengl.GLAutoDrawable
import javax.media.opengl.GLCapabilities
import javax.media.opengl.GLEventListener
import javax.media.opengl.GLProfile
import javax.media.opengl.awt.{GLCanvas => JOGLCanvas}
import javax.swing.JFrame
import com.sun.opengl.util.texture.Texture
import com.sun.opengl.util.texture.TextureIO
import java.awt.font.FontRenderContext
import java.io.IOException
import java.io.InputStream

object DemoMain {
  def main(args: Array[String]) {
    if(args.contains("-gl")) (new GLAWTLauncher(SimpleDemo)).launch()
    else (new Java2DLauncher(SimpleDemo)).launch()
  }
}

class GLAWTLauncher(demo: Demo) {
  def launch() {
    val frame = new Frame
    val profile = GLProfile.getDefault
    val caps = new GLCapabilities(profile)
    caps.setHardwareAccelerated(true)
    caps.setSampleBuffers(true)
    caps.setNumSamples(4)
    caps.setStencilBits(8)
    caps.setDoubleBuffered(true)
    println(caps.toString)
    val joglCanvas = new JOGLCanvas(caps)
    joglCanvas.addGLEventListener(OGLEventListener)
    frame.add(joglCanvas)
    frame.setSize(500, 500)
    //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    joglCanvas.requestFocusInWindow()
    frame.setVisible(true)

    //val anim = new FPSAnimator(joglCanvas, 50)
    val anim = new Animator(joglCanvas)
    anim.setRunAsFastAsPossible(true)
    anim.start
  }
  
  val canvas = new GLCanvas
  var image1: Texture = null
  val shader: Shader = new Shader
  
  def loadImage(name: String, sufix: String): Texture = {
    val f: InputStream = getClass.getResourceAsStream(name)
    try {
      val img = TextureIO.newTexture(f, true, sufix)
      return img
    } catch {
      case ioe: IOException => {
          error("Image loading: can't find file "+name+"\n"+ioe.toString)
        }
      case e: Exception => { error("Image loading: " + e.toString) }
    }
    return null
  }
    
  object OGLEventListener extends GLEventListener {
    def init(drawable: GLAutoDrawable) {
      val gl = drawable.getGL.getGL2
      canvas.init(gl)      
      shader.buildShader(gl)
      shader.compileShadersFromFile("data/solid.fs", "data/solid.vs")
      image1 = loadImage("data/CoffeeBean.bmp", "bmp")
    }

    def display(drawable: GLAutoDrawable) {      
      canvas.gl = drawable.getGL.getGL2
      demo.step(canvas) 
    }

    def reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {
      canvas.resize(drawable.getWidth, drawable.getHeight)    
    }
    def dispose(drawable: GLAutoDrawable) {}
  }
}

class Java2DLauncher(demo: Demo) {
  def launch() {
    val frame = new JFrame
    val comp = new CanvasComponent
    frame.add(comp)
    frame.setSize(500, 500)
    //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    comp.requestFocusInWindow()
    frame.setVisible(true)
  }
  
  class CanvasComponent extends javax.swing.JComponent {
    lazy val canvas = new Java2DCanvas(getGraphics().asInstanceOf[Graphics2D], 0, 0)
    
    override def paintComponent(g: Graphics) {
      val g2d = g.asInstanceOf[Graphics2D]
      //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      canvas.width = getWidth
      canvas.height = getHeight
      demo.step(canvas)
      this.repaint()
    }
  }
}