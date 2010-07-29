package graphic
package test

import java.awt.{Canvas=>_, _}
import java.awt.geom._
import math._

/*class RectDashDemo extends FPSDemo {
  def dashButt(w: Float)(d: Float*) = new BasicStroke(w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, d.toArray, 0)
  def dashSquare(w: Float)(d: Float*) = new BasicStroke(w, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 0, d.toArray, 0)
  def dashRound(w: Float)(d: Float*) = new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, d.toArray, 0)
  
  def draw(g: Canvas) {
    def drawDashSet(x: Int, y: Int)(stroke: (Float*)=>BasicStroke): Int = {
      var w = width/3f
      var h = height/3f
      g.stroke = stroke(1, 1)
      g.stroke(new Rectangle2D.Float(x, y, w, h))
      w = w*2/3f
      h = h*2/3f
      g.stroke = stroke(10, 10)
      g.stroke(new Rectangle2D.Float(x, y, w, h))
      w = w*2/3f
      h = h*2/3f
      g.stroke = stroke(20, 15)
      g.stroke(new Rectangle2D.Float(x, y, w, h))
      w = w*2/3f
      h = h*2/3f
      g.stroke = stroke(10, 13, 4, 8)
      g.stroke(new Rectangle2D.Float(x, y, w, h))
      y+40
    }
    
    def drawWidthSet(y0: Int)(stroke: Float=>(Float*)=>BasicStroke): Int = {
      var y = drawDashSet(y0)(stroke(0.5f))
      y = drawDashSet(y)(stroke(1))
      y = drawDashSet(y)(stroke(2))
      y = drawDashSet(y)(stroke(6))
      y
    }
    
    g.clear(Color.white)
    g.color = Color.black
    
    var y = drawWidthSet(5)(dashButt)
    y = drawWidthSet(y)(dashSquare)
    drawWidthSet(y)(dashRound)
  }
}*/