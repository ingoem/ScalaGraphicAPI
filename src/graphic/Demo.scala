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

abstract class Demo {
  var t0, lastFPSUpdate = System.nanoTime
  var t1 = 0L
  var framesCounter = 0L
  var fpsCounter = 0.0
  
  def step(canvas: Canvas) {
    draw(canvas)
    
    framesCounter +=1
    t1 = System.nanoTime
    var fps = 1000000000.0/(t1 - t0)
    fpsCounter += fps
    val avg = fpsCounter / framesCounter
    if(t1 - lastFPSUpdate > 1000000000){  // display fps in each sec
      println("Fps: " + fps.toFloat +", Avg: " + avg.toFloat)
      lastFPSUpdate = t1
    }
    t0 = t1
  }

  def draw(canvas: Canvas)
  
  def textOutline(f: Font, str: String, x: Int, y: Int): Shape =
    f.createGlyphVector(new FontRenderContext(null, false, false), str).getOutline(x,y)
}