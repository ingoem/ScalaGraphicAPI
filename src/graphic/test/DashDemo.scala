package graphic
package test

import java.awt.{Canvas=>_, _}
import java.awt.geom._
import math._

object DashDemo extends FPSDemo {
  def dashButt(w: Float)(d: Float*) = new BasicStroke(w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, d.toArray, 0)
  def dashSquare(w: Float)(d: Float*) = new BasicStroke(w, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 0, d.toArray, 0)
  def dashRound(w: Float)(d: Float*) = new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, d.toArray, 0)
  
  def hline(y: Int) = new Line2D.Float(5, y, width-10, y)
  
  def draw(g: Canvas) {
    def drawDashSet(y: Int)(stroke: (Float*)=>BasicStroke): Int = {
      g.stroke = stroke(1, 1)
      g.stroke(hline(y))
      g.stroke = stroke(2, 2)
      g.stroke(hline(y+8))
      g.stroke = stroke(5, 4)
      g.stroke(hline(y+16))
      g.stroke = stroke(10, 5, 2, 3)
      g.stroke(hline(y+24))
      y+40
    }
    
    def drawWidthSet(y0: Int)(stroke: (Float)=>(Float*)=>BasicStroke): Int = {
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
    
    /*g.stroke = dashButt(1, 1)
    g.stroke(hline(10))
    g.stroke = dashButt(2, 2)
    g.stroke(hline(20))
    g.stroke = dashButt(5, 2)
    g.stroke(hline(30))
    g.stroke = dashButt(10, 5, 2, 4)
    g.stroke(hline(40))
    
    g.stroke = wideDashButt(1, 1)
    g.stroke(hline(60))
    g.stroke = wideDashButt(2, 2)
    g.stroke(hline(70))
    g.stroke = wideDashButt(5, 2)
    g.stroke(hline(80))
    g.stroke = wideDashButt(10, 5, 2, 4)
    g.stroke(hline(90))
    
    g.stroke = fatDashButt(1, 1)
    g.stroke(hline(110))
    g.stroke = fatDashButt(2, 2)
    g.stroke(hline(130))
    g.stroke = fatDashButt(5, 2)
    g.stroke(hline(150))
    g.stroke = fatDashButt(10, 5, 2, 4)
    g.stroke(hline(170))*/
  }
}