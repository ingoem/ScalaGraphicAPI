package graphic
package test

import java.awt.{Canvas=>_, _}
import java.awt.geom._
import math._

object LineDashDemo extends FPSDemo {
  def dashButt(w: Float)(d: Float*) = new BasicStroke(w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, d.toArray, 0)
  def dashSquare(w: Float)(d: Float*) = new BasicStroke(w, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 0, d.toArray, 0)
  def dashRound(w: Float)(d: Float*) = new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, d.toArray, 0)
  
  def hline(y: Int) = new Line2D.Float(5, y, width-10, y)
  
  def draw(g: Canvas) {
    def drawDashSet(y: Int)(stroke: (Float*)=>BasicStroke): Int = {
      g.stroke = stroke(1, 1)
      g.stroke(hline(y))
      g.stroke = stroke(10, 10)
      g.stroke(hline(y+8))
      g.stroke = stroke(20, 15)
      g.stroke(hline(y+16))
      g.stroke = stroke(10, 13, 4, 8)
      g.stroke(hline(y+24))
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
}