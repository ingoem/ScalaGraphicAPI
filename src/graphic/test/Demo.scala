package graphic
package test

import java.awt.{Canvas => _, _}

trait Demo {
  /*protected var t0 = System.nanoTime
  protected var t = 0
  protected var t1 = 0L*/
  
  def log(s: String) = println(s)
  
  def textOutline(f: Font, str: String, x: Int, y: Int): Shape =
    f.createGlyphVector(new font.FontRenderContext(null, false, false), str).getOutline(x,y)
    
  def drawRate = 1
    
  def step(canvas: Canvas)
  def draw(canvas: Canvas)
}