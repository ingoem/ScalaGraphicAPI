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
import javax.media.opengl.fixedfunc.GLMatrixFunc
import javax.media.opengl.glu.GLU
import javax.swing.JFrame

abstract class Demo extends JFrame {
  var gl: GL2 = null
  val graphic = new GLCanvas

  //var text:FontText = new FontText
  var image:ImageRender = new ImageRender
  //var shader:Shader = new Shader


  
  def main(args: Array[String]) {
    val profile = GLProfile.getDefault
    val caps = new GLCapabilities(profile)
    caps.setHardwareAccelerated(true)
    caps.setSampleBuffers(true)
    caps.setNumSamples(4)
    caps.setStencilBits(8)
    caps.setDoubleBuffered(true)
    println(caps.getSampleBuffers)
    println(caps.toString)
    val canvas = new JOGLCanvas(caps)
    canvas.addGLEventListener(OGLEventListener)
    getContentPane.add(canvas, BorderLayout.CENTER)
    pack()
    setSize(500, 500)
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    canvas.requestFocusInWindow()
    setVisible(true)   
    
    val anim = new FPSAnimator(canvas, 80)
    anim.start
  }
  
  object OGLEventListener extends GLEventListener{
    def init(drawable: GLAutoDrawable) {
      gl = drawable.getGL().getGL2
      val glu:GLU = new GLU()
      gl.glClearColor(0.7f, 0.7f, 0.7f, 0.0f)
      gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT)
      gl.glViewport(0, 0, 500, 500)
      gl.getGL2().glMatrixMode(GLMatrixFunc.GL_PROJECTION)
      gl.getGL2().glLoadIdentity()
      glu.gluOrtho2D(0.0, 500.0, 0.0, 500.0)
      gl.glClearStencil(0)
      gl.getGL2().glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
      gl.getGL2().glLoadIdentity()

      gl.glEnable(GL.GL_MULTISAMPLE)
      //gl.glDisable(GL.GL_DEPTH_TEST)
      //gl.glBlendFunc(GL.GL_SRC_ALPHA_SATURATE, GL.GL_ONE)
      gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA)      
      gl.glEnable(GL.GL_BLEND)
      //gl.glEnable(javax.media.opengl.GL2GL3.GL_POLYGON_SMOOTH)
      //gl.glHint(javax.media.opengl.GL2GL3.GL_POLYGON_SMOOTH_HINT, GL.GL_NICEST)

      gl.glEnableClientState(javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY)

      graphic.init(gl)
      
      image.loadImage(gl, "CoffeeBean.bmp", "bmp")
      //shader.buildShader(gl)
      //shader.compileShaders(null, null)
    }

    def dispose(drawable: GLAutoDrawable) {}

    def display(drawable: GLAutoDrawable) {      
      draw(graphic)
    }

    def reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {}
  }
  
  def textOutline(g: Canvas, str: String, x: Int, y: Int): Shape = 
    g.font.createGlyphVector(g.fontRenderContext, str).getOutline(x,y)
  
  def draw(canvas: GLCanvas)
}