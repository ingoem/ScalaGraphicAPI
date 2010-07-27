package graphic
package test

import java.awt.{Canvas=>_, _}
import java.awt.geom._
import math._

object StrokeDemo extends FPSDemo {
  var animY1 = 0.01
  var dashOffset = 0f
  var dashWidth = 4f
  
  val outline = textOutline(new Font("Times New Roman", Font.BOLD, 108), "H e l l o", 100, 400)
  
  def draw(g: Canvas) {
    g.clear(Color.WHITE)

    g.color = new Color(0.0f, 0.0f, 0.01f)
    g.stroke = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 50, Array(8f, 10f), dashOffset)
    g.stroke(new Ellipse2D.Float(200, 200, 200, 50))

    dashOffset += 0.2f
    //dashOffset %= 18

    g.stroke = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 50, Array(8f, 10f), dashOffset)
    g.stroke(new RoundRectangle2D.Float(290, 290, 80, 90, 30, 30))

    dashWidth += 0.05f
    dashWidth %= 10
    g.stroke = new BasicStroke(dashWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 50, Array(12f, 6f), dashOffset)
    g.stroke(new RoundRectangle2D.Float(390, 290, 80, 90, 30, 30))

    g.stroke = new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 50, Array(4f, 8f), dashOffset)
    g.stroke(outline)

    g.color = new Color(1.0f, 0.5f, 0.3f)
    //g.font = new Font("Times New Roman", Font.BOLD, dashWidth.toInt*5)
    //g.drawTextOnPath("scala java kawa", new Path2D.Float(new Ellipse2D.Float(100, 100, 150+(math.sin(animY1*5)*90.0).toInt, 100)))

    animY1 +=0.01f
    val p = new Path2D.Float
    p.moveTo(200 - 150*sin(animY1), 200 + 80*cos(animY1))
    p.curveTo(200 + 300*sin(animY1), 200 + 300*cos(animY1),
              300 - 300*sin(animY1), 300 - 300*cos(animY1),
              350 + 50*sin(animY1), 350 - 150*cos(animY1))
    p.quadTo(200 - 50*sin(animY1), 200 + 50*cos(animY1),
             300 + 150*sin(animY1*3.5), 300 + 180*cos(animY1*2))
    p.lineTo(200 - 150*sin(animY1), 200 + 80*cos(animY1))
    p.closePath()

    g.stroke = new BasicStroke(14, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 50, Array(10f, 15f), 0)
    g.color = new Color(0.8f, 0.2f, 0.2f)
    g.fill(p)
    g.color = new Color(0.2f, 0.8f, 0.2f, 0.5f)
    g.stroke(p)
  }
}