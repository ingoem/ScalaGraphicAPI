package graphic

import java.awt._
import java.awt.geom._
import math._

object SimpleDemo extends Demo {
  var animY1 = 0.01
  var dashOffset = 0f
  var dashWidth = 4f
  
  def draw(g: GLCanvas) {
    g.clear(Color.WHITE)     
    g.color = new Color(0.0f, 0.0f, 0.01f)
    g.strokeEllipse(200, 200, 200, 50)
    
    dashOffset += 0.2f
    dashOffset %= 18
    
    g.stroke = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 50, Array(8f, 10f), dashOffset)
    g.strokeRoundRectangle(290, 290, 80, 90, 30, 30)
    
    dashWidth += 0.05f
    dashWidth %= 10
    g.stroke = new BasicStroke(dashWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 50, Array(8f, 1f, 6f), dashOffset)
    g.strokeRoundRectangle(390, 290, 80, 90, 30, 30)
    
//      g.strokeRectangle(100, 350, 200, 50, 10, g.CAP_FLAT, g.JOIN_BEVEL)

    g.font = g.font.deriveFont(64f)
    g.stroke = new BasicStroke(2)
    g.stroke(textOutline(g, "Hello", 100, 400))
    g.color = new Color(1.0f, 0.5f, 0.3f, 0.9f)
      
    g.font = new Font("Times New Roman", Font.BOLD, 48)
    
    // TODO: text API needs to be cleaned up
    g.setTextCurveParam(00, 100, 500, 100, 150, 
        200+(math.sin(animY1*5)*90.0).intValue,
        300,
        -50+(math.sin(animY1*5)* -90.0).intValue)
    
    g.drawShapeText(" I  LOVE  SCALA  2.8 ")

    // 3.7, 5.16
    animY1 +=0.01f            
    val p = new Path2D.Float
    p.moveTo(200 - 150*sin(animY1), 200 + 80*cos(animY1))
    p.curveTo(200 + 300*sin(animY1), 200 + 300*cos(animY1),
              300 - 300*sin(animY1), 300 - 300*cos(animY1),
              350 +  50*sin(animY1), 350 - 150*cos(animY1))
    p.quadTo(200 - 50*sin(animY1), 200 + 50*cos(animY1),
             200 - 150*sin(animY1), 200 + 80*cos(animY1))
    p.closePath()   
    g.color = new Color(0.2f, 0.8f, 0.2f)
    g.stroke = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    g.stroke(p)
//      g.pathReset
      //g.setColor(0,0,0)      
      //g.setClipRect(150, 150, 100, 100)

    // TODO: clipping API needs to be cleaned up
    g.pathClipArea
    g.pathReset      
    image.drawImage(00, 00, 500, 500)
    g.deactiveClipArea

    //shader.applyShader
    g.fillEllipse(50, 50, 90, 30)
    //shader.deactiveShader
  }
}