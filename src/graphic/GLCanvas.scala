
package graphic

import java.nio.ByteBuffer
import java.nio.FloatBuffer
import javax.media.opengl._
import javax.media.opengl.fixedfunc.GLMatrixFunc
import javax.media.opengl.glu.GLU
import java.awt.Shape
import java.awt.geom.Arc2D
import java.awt.geom.Ellipse2D
import java.awt.geom.Line2D
import java.awt.geom.Path2D
import java.awt.geom.PathIterator
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D
import java.awt.font.FontRenderContext
import java.awt.font.GlyphVector
import java.awt.{Font, Color, BasicStroke}

class GLCanvas extends Canvas with GLTextRenderer {
  private var bufferId : Array[Int] = Array(0)
  var floatSize : Int = 4
  private var bufferData : FloatBuffer = FloatBuffer.allocate(3620)
  private var bufferByte : ByteBuffer = null
  private var verts : Array[Float] = new Array[Float](3620)
  private var tmpverts : Array[Float] = new Array[Float](3620)
  private[graphic] var gl: GL2 = null
  private var vbo : VBuffer = new VBuffer
  private var vertsNumTmp:Int = 0
  private var vertsNum:Int = 0
  private var point = new Array[Float](6)
  private var arcVerts = new Array[Float](360)

  private val rrect:RoundRectangle2D = new RoundRectangle2D.Float()
  private val rect:Rectangle2D = new Rectangle2D.Float()
  private val line2D:Line2D = new Line2D.Float()
  private val triPath = new Path2D.Float()
  private val ellipse = new Ellipse2D.Float()
  private val arc2D = new Arc2D.Float()
  private val mainPath = new Path2D.Float()

  private var i:Int = 0
  private var ind:Int = 0
  private var nx:Float = 0
  private var ny:Float= 0
  private var curx:Float = 0
  private var cury:Float= 0
  private var startInd:Int = 0
  private var endInd:Int = 0
  val ARC_OPEN = Arc2D.OPEN
  val ARC_CHORD = Arc2D.CHORD
  val ARC_PIE = Arc2D.PIE

  val miter_limit = 100
  var roundness:Float = 5
  private var arcInd:Int = 0
  private var endsAtStart:Boolean = false
  val WIDTH_MIN = 0.25
  val WIDTH_MAX = 20

  val tess = new Tesselator
  
  private var _stroke = new BasicStroke
  def stroke: BasicStroke = _stroke
  def stroke_=(s: BasicStroke) {
    val w = s.getLineWidth
    _stroke = if(w > WIDTH_MIN && w < WIDTH_MAX) s
              else new BasicStroke(math.max(w, math.min(WIDTH_MAX, w)), s.getEndCap, s.getLineJoin, 
                                   s.getMiterLimit, s.getDashArray, s.getDashPhase)
  }
  
  private def lineWidth = _stroke.getLineWidth
  
  private var _color = Color.BLACK
  def color: Color = _color
  def color_=(c: Color) = {
    _color = c
    gl.glColor4ub(c.getRed.toByte, c.getGreen.toByte, c.getBlue.toByte, c.getAlpha.toByte)
  }
  
  // TODO: respect AA and fractional metrics settings
  def fontRenderContext = new FontRenderContext(null, true, false)

  private[graphic] def init(gl: GL2) = {
    this.gl = gl
    gl.glEnable(GL.GL_MULTISAMPLE)
    //gl.glDisable(GL.GL_DEPTH_TEST)
    //gl.glBlendFunc(GL.GL_SRC_ALPHA_SATURATE, GL.GL_ONE)
    gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA)      
    gl.glEnable(GL.GL_BLEND)
    //gl.glEnable(javax.media.opengl.GL2GL3.GL_POLYGON_SMOOTH)
    //gl.glHint(javax.media.opengl.GL2GL3.GL_POLYGON_SMOOTH_HINT, GL.GL_NICEST)

    gl.glEnableClientState(javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY)
    vbo.init(gl, bufferId, verts, bufferData, floatSize)
    gl.glColor3f(0, 0, 0)
  }
  
  private[graphic] def resize(width: Int, height: Int) {
    val glu = new GLU
    gl.glViewport(0, 0, width, height)
    gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION)
    gl.glLoadIdentity()
    glu.gluOrtho2D(0.0, width, height, 0.0)
    gl.glClearStencil(0)
    gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
    gl.glLoadIdentity()
  }

  def fillRoundRectangle(x:Int, y:Int, w:Int, h:Int, arcw:Float, arch:Float) = {
    rrect.setRoundRect(x, y, w, h, arcw, arch)
    calcFigure(rrect, false)
    bufferData = vbo.mapBuffer(gl, bufferId, verts)
    vbo.drawBuffer(gl, GL2.GL_POLYGON, vertsNum)
  }

  def outlineRoundRectangle(x:Int, y:Int, w:Int, h:Int, arcw:Float, arch:Float) = {
    rrect.setRoundRect(x, y, w, h, arcw, arch)
    endsAtStart = true
    calcFigure(rrect, true)
    bufferData = vbo.mapBuffer(gl, bufferId, verts)
    vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP, vertsNum)
  }

  def strokeRoundRectangle(x:Int, y:Int, w:Int, h:Int, arcw:Float, arch:Float) = {
    rrect.setRoundRect(x, y, w, h, arcw, arch)
    createStroke(rrect)
  }

  def fillRectangle(x:Int, y:Int, w:Int, h:Int) = {
    rect.setRect(x, y, w, h)
    calcFigure(rect, false)
    bufferData = vbo.mapBuffer(gl, bufferId, verts)
    vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP, vertsNum)
  }

  def outlineRectangle(x:Int, y:Int, w:Int, h:Int, width:Float) = {
    rect.setRect(x, y, w, h)
    endsAtStart = true
    calcFigure(rect, true)
    bufferData = vbo.mapBuffer(gl, bufferId, verts)
    vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP, vertsNum)
  }

  def strokeRectangle(x:Int, y:Int, w:Int, h:Int) = {
    rect.setRect(x, y, w, h)
    createStroke(rect)
  }

  def outlineArc(x:Float, y:Float, w:Float, h:Float, angleStart:Float,
                 angleExtend:Float, arcType:Int) = {
    arc(x, y, w, h, angleStart, angleExtend, arcType, true)
  }

  def fillArc(x:Float, y:Float, w:Float, h:Float, angleStart:Float,
                 angleExtend:Float, arcType:Int) = {
    var at = arcType
    if(at==ARC_OPEN) at = ARC_CHORD
    arc(x, y, w, h, angleStart, angleExtend, at, false)
  }

  private def arc(x:Float, y:Float, w:Float, h:Float, angleStart:Float,
                 angleExtend:Float, arcType:Int, outline:Boolean) = {
    arc2D.setArc(x-w/2, y-h/2, w, h, angleStart, angleExtend, arcType)
    if(arcType != ARC_OPEN){
      endsAtStart = true
      calcArcOutline(arc2D, outline)
    } else {
      endsAtStart = false
      calcFigure(arc2D, true)
    }
    bufferData = vbo.mapBuffer(gl, bufferId, verts)
    if(outline == true)vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP, vertsNum)
    else
    vbo.drawBuffer(gl, GL.GL_TRIANGLE_FAN, vertsNum)
  }

  def strokeArc(x:Float, y:Float, w:Float, h:Float, angleStart:Float,
      angleExtend:Float, arcType:Int) = {
        arc2D.setArc(x-w/2, y-h/2, w, h, angleStart, angleExtend, arcType)
        createStroke(arc2D)
      }

  def fillEllipse(x:Int, y:Int, w:Int, h:Int) = {    
    ellipse.setFrame(x-w/2, y-h/2, w, h)
    calcFigure(ellipse, false)
    bufferData = vbo.mapBuffer(gl, bufferId, verts)
    // !!! vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP, vertsNum)
    vbo.drawBuffer(gl, GL2.GL_POLYGON, vertsNum)
  }

  def outlineEllipse(x:Int, y:Int, w:Int, h:Int, width:Float) = {
    ellipse.setFrame(x-w/2, y-h/2, w, h)
    endsAtStart = true
    calcFigure(ellipse, true)
    bufferData = vbo.mapBuffer(gl, bufferId, verts)
    vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP, vertsNum)
  }

  def strokeEllipse(x:Int, y:Int, w:Int, h:Int) = {
    ellipse.setFrame(x-w/2, y-h/2, w, h)
    createStroke(ellipse)
  }

  def line(x1:Float, y1:Float, x2:Float, y2:Float, width:Float) = {
    line2D.setLine(x1, y1, x2, y2)
    endsAtStart = false
    calcFigure(line2D, true)
    bufferData = vbo.mapBuffer(gl, bufferId, verts)
    vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP, vertsNum)
  }

  def strokeLine(x1:Float, y1:Float, x2:Float, y2:Float) = {
    line2D.setLine(x1, y1, x2, y2)
    createStroke(line2D)
  }

  def fillTriangle(x1:Int, y1:Int, x2:Int, y2:Int, x3:Int, y3:Int) = {
    triPath.reset
    triPath.moveTo(x1, y1)
    triPath.lineTo(x2, y2)
    triPath.lineTo(x3, y3)
    triPath.lineTo(x1, y1)
    triPath.closePath
    calcFigure(triPath, false)
    bufferData = vbo.mapBuffer(gl, bufferId, verts)
    vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP, vertsNum)
  }

  def outlineTriangle(x1:Int, y1:Int, x2:Int, y2:Int, x3:Int, y3:Int, join:Int, width:Float) = {
    triPath.reset
    triPath.moveTo(x1, y1)
    triPath.lineTo(x2, y2)
    triPath.lineTo(x3, y3)
    triPath.lineTo(x1, y1)
    triPath.closePath
    endsAtStart = true
    calcFigure(triPath, true)
    bufferData = vbo.mapBuffer(gl, bufferId, verts)
    vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP , vertsNum)
  }

  def strokeTriangle(x1:Int, y1:Int, x2:Int, y2:Int, x3:Int, y3:Int) = {
    triPath.reset
    triPath.moveTo(x1, y1)
    triPath.lineTo(x2, y2)
    triPath.lineTo(x3, y3)
    triPath.lineTo(x1, y1)
    triPath.closePath
    createStroke(triPath)
  }
  
  def stroke(p: Shape) {
    endsAtStart = false
    calcFigure(p, true)
    bufferData = vbo.mapBuffer(gl, bufferId, verts)
    vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP, vertsNum)
  }

  def charOutline(fontName:String, fontSize:Int, text:Char, x:Int, y:Int) = {
    var frc:FontRenderContext = new FontRenderContext(null, false, false)
    val font:Font = new Font(fontName, Font.BOLD, fontSize)
    var glyph:GlyphVector = font.createGlyphVector(frc, text.toString)
    var shape = glyph.getOutline
    calcFigure(shape, true)
    bufferData = vbo.mapBuffer(gl, bufferId, verts)
    gl.glPushMatrix
    gl.glTranslatef(x, y, 0)    
    gl.glScalef(1.0f, -1.0f, 1.0f)
    vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP , vertsNum)
    gl.glPopMatrix
  }

  def pathMoveTo(x:Float, y:Float) = {
    mainPath.moveTo(x, y)
  }
  def pathLineTo(x:Float, y:Float) = {
    mainPath.lineTo(x, y)    
  }
  def pathCurveTo(x:Float, y:Float, ctrx1:Float, ctry1:Float, ctrx2:Float, ctry2:Float) = {
    mainPath.curveTo(ctrx1, ctry1, ctrx2, ctry2, x, y)
  }
  def pathQuadTo(x:Float, y:Float, ctrx:Float, ctry:Float) = {
    mainPath.quadTo(ctrx, ctry, x, y)
  }
  def pathReset() = {
    mainPath.reset
  }

  
  def pathDraw() = {
    mainPath.closePath
    endsAtStart = false
    calcFigure(mainPath, true)
    bufferData = vbo.mapBuffer(gl, bufferId, verts)
    vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP, vertsNum)
    //mainPath.reset
  }
  def pathDrawStroke() = {
    mainPath.closePath
    createStroke(mainPath)
    mainPath.reset    
  }
  def pathDrawFill() = {
    tessFigure(mainPath)
    bufferData = vbo.mapBuffer(gl, bufferId, verts)
    vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP, vertsNum)
  }
  def pathClipArea() = {
    gl.glEnable(GL.GL_STENCIL_TEST)
    gl.glStencilFunc(GL.GL_ALWAYS, 1, 1)
    gl.glStencilOp(GL.GL_REPLACE, GL.GL_REPLACE, GL.GL_REPLACE)
    gl.glColorMask(false, false, false, false)

    tessFigure(mainPath)
    bufferData = vbo.mapBuffer(gl, bufferId, verts)
    vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP, vertsNum)

    gl.glColorMask(true, true, true, true)
    gl.glStencilFunc(GL.GL_EQUAL, 1, 1)
    gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_KEEP)
  }

  def setClipRect(x:Int, y:Int, h:Int, w:Int) = {
    gl.glEnable(GL.GL_STENCIL_TEST)
    gl.glStencilFunc(GL.GL_ALWAYS, 1, 1)
    gl.glStencilOp(GL.GL_REPLACE, GL.GL_REPLACE, GL.GL_REPLACE)
    gl.glColorMask(false, false, false, false)
    
    gl.glBegin(GL2.GL_QUADS)
    gl.glVertex2i(x, y)
    gl.glVertex2i(x+w, y)
    gl.glVertex2i(x+w, y+h)
    gl.glVertex2i(x, y+h)
    gl.glEnd

    gl.glColorMask(true, true, true, true)
    gl.glStencilFunc(GL.GL_EQUAL, 1, 1)
    gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_KEEP)
  }

  def deactiveClipArea() = {
    gl.glDisable(GL.GL_STENCIL_TEST)
  }
/*
  private def tess() = {
    var s1 = new java.awt.geom.GeneralPath
    s1.moveTo(10, 200)
    s1.curveTo(50, 450, 180, -50, 290, 200)
    s1.curveTo(340, 500, 450, -50, 10, 200)//480, 300)
    //s1.lineTo(500, 500)
    var area = new java.awt.geom.Area(s1)

    cap_style = CAP_FLAT
    join_style = JOIN_BEVEL
    endsAtStart = false
    tessFigure2(area, true)
    bufferData = vbo.mapBuffer(gl, bufferId, verts)
    vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP, vertsNum)
//    vbo.drawBuffer(gl, GL.GL_POINTS, vertsNum)
  }
*/
  /*
  // current start
  private var cs = 0

  private def rectTran(st:Int, end:Int) = {
    cs = st
    val intersection = false
    if(intersection)
      rectTran(st+1, end)
    else {
          var st_t = rs
          var end_t = end
          while(st_t < end_t){ // && f==1) {
            verts(i) = tmpverts(st)
            verts(i+1) = tmpverts(st+1)
            i+=2
            st_t+=2
            verts(i) = tmpverts(end_t-2)
            verts(i+1) = tmpverts(end_t-1)
            i+=2
            end_t-=2
          }
          // close triangel strip
          verts(i) = verts(i-2)
          verts(i+1) = verts(i-1)
          i+=2
    }
  }
*/

  private def tessFigure(figure:Shape) = {
    //var area = new java.awt.geom.Area(figure)
    val path = figure.getPathIterator(null, 1.0) //area.getPathIterator(null, 1.0)
    tess.setPathRule(path.getWindingRule)
    tess.startTessPolygon
    tess.startTessContour
    var f = 0
    i=0
    var z:Int = 0
    while(!path.isDone){
      var t = path.currentSegment(point)
      t match {
      case java.awt.geom.PathIterator.SEG_CUBICTO => println("cubic to")
      case java.awt.geom.PathIterator.SEG_QUADTO => println("quad to")
      case java.awt.geom.PathIterator.SEG_MOVETO => {
          //tess.startTessContour
          tess.addVertex(point(0), point(1))
        }
      case java.awt.geom.PathIterator.SEG_LINETO => {
          tess.addVertex(point(0), point(1))
        }
      case java.awt.geom.PathIterator.SEG_CLOSE => {
          //tess.endTessContour
      }
      }
      path.next
    }
    tess.endTessContour
    tess.endTessPolygon
    
    i = tess.i    
    endInd = i
    vertsNumTmp = i/2
    i=0
    ind = 0
    tess.vertsData.copyToArray(verts)
    vertsNum = vertsNumTmp
  }

  private def createStroke(fig:Shape) = {
    //if(width > WIDTH_MIN && width < WIDTH_MAX) this.width = w*2.0f
    getStroke(fig)
    bufferData = vbo.mapBuffer(gl, bufferId, verts)
    vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP, vertsNum)
  }

  private def getStroke(figure:Shape) {
    val path = stroke.createStrokedShape(figure).getPathIterator(null, 1.0f)
    var z = 0
    i=0
    while(!path.isDone) {      
      path.currentSegment(point) match {
        case java.awt.geom.PathIterator.SEG_CUBICTO => println("cubic to")
        case java.awt.geom.PathIterator.SEG_QUADTO => println("quad to")
        case java.awt.geom.PathIterator.SEG_MOVETO =>
          z = 0
          tmpverts(z) = point(0)
          tmpverts(z+1) = point(1)
          z+=2
        case java.awt.geom.PathIterator.SEG_LINETO => 
          tmpverts(z) = point(0)
          tmpverts(z+1) = point(1)
          z+=2
        case java.awt.geom.PathIterator.SEG_CLOSE => 
          var st = 0
          var end = z
          while(st < end) {
            verts(i) = tmpverts(st)
            verts(i+1) = tmpverts(st+1)
            i+=2
            st+=2
            verts(i) = tmpverts(end-2)
            verts(i+1) = tmpverts(end-1)
            i+=2
            end-=2
          }
          verts(i) = verts(i-2)
          verts(i+1) = verts(i-1)
          i+=2
      }
      path.next
    }
    vertsNum = i/2
    i=0
    ind = 0
  }

  /* TODO: Example of a cleanly written method w/ pattern match
  private def storeOutline(shape: Shape) {
    val path = shape.getPathIterator(null, 1.0f)
    i=0
    while(!path.isDone){
      path.currentSegment(point) match {
        case PathIterator.SEG_MOVETO =>
          tmpverts(i) = point(0)
          tmpverts(i+1) = point(1)
          i+=2
        case PathIterator.SEG_LINETO =>
          tmpverts(i) = point(0)
          tmpverts(i+1) = point(1)
          i+=2
        case PathIterator.SEG_CLOSE =>
          // TODO???
        case _ => 
          error("FlatteningPathIterator contract violated")
      }
      path.next()
    }
  }*/ 
  
  private def calcFigure(figure:Shape, outline:Boolean) = {
    var path = figure.getPathIterator(null, 1.0f)
    i=0
    while(!path.isDone){
      var t = path.currentSegment(point)
      t match {
      case java.awt.geom.PathIterator.SEG_CUBICTO => println("cubic to")
      case java.awt.geom.PathIterator.SEG_QUADTO => println("quad to")
      case java.awt.geom.PathIterator.SEG_MOVETO => {
          tmpverts(i) = point(0)
          tmpverts(i+1) = point(1)
          i+=2
        }
      case java.awt.geom.PathIterator.SEG_LINETO => {
          tmpverts(i) = point(0)
          tmpverts(i+1) = point(1)
          i+=2
        }
      case java.awt.geom.PathIterator.SEG_CLOSE => {
      }
      }
      path.next
    }

    endInd = i
    vertsNumTmp = i/2
    i=0
    ind = 0

    if(outline == false) {
      tmpverts.copyToArray(verts)
      vertsNum = vertsNumTmp
    } else {
      var prevt = 0
      path = figure.getPathIterator(null, 1.0f)
      while(!path.isDone){
        var t = path.currentSegment(point)
        t match {
        case java.awt.geom.PathIterator.SEG_CUBICTO => println("cubic to")
        case java.awt.geom.PathIterator.SEG_QUADTO => println("quad to")
        case java.awt.geom.PathIterator.SEG_MOVETO =>
          {
            if(ind>0)
              endCapOrCapClose(startInd, false)
            startInd = ind
            moveto(ind)
            ind+=2
            prevt = t
          }
        case java.awt.geom.PathIterator.SEG_LINETO =>
          {
            if (prevt != PathIterator.SEG_MOVETO)
              join(ind)
            lineto(ind)
            ind+=2
            prevt = t
          }
        case java.awt.geom.PathIterator.SEG_CLOSE =>
          {
            //endCapOrCapClose(startInd, true)
          }
        }
        path.next
      }
      endCapOrCapClose(startInd, false)
      vertsNum = i/2
    }
  }

  private def calcArcOutline(figure:Shape, outline:Boolean) = {
    var path = figure.getPathIterator(null, 1.0f)
    
    i=2

    while(!path.isDone){
      var t = path.currentSegment(point)
      t match {
      case java.awt.geom.PathIterator.SEG_CUBICTO => println("cubic to")
      case java.awt.geom.PathIterator.SEG_QUADTO => println("quad to")
      case java.awt.geom.PathIterator.SEG_MOVETO => {          
          tmpverts(i) = point(0)
          tmpverts(i+1) = point(1)          
          i+=2
        }
      case java.awt.geom.PathIterator.SEG_LINETO => {
          tmpverts(i) = point(0)
          tmpverts(i+1) = point(1)
          i+=2          
        }
      case java.awt.geom.PathIterator.SEG_CLOSE => {          
          tmpverts(0) = point(0)
          tmpverts(1) = point(1)
          i-=2
      }
      }
      path.next
    }

    tmpverts.copyToArray(verts)
    endInd = i
    vertsNumTmp = i/2
    vertsNum = i/2
    i=0
    ind = 0

if(outline == true) {
      var prevt = 0
      path = figure.getPathIterator(null, 1.0f)
      while(!path.isDone){
        var t = path.currentSegment(point)
        t match {
        case java.awt.geom.PathIterator.SEG_CUBICTO => println("cubic to")
        case java.awt.geom.PathIterator.SEG_QUADTO => println("quad to")
        case java.awt.geom.PathIterator.SEG_MOVETO =>
          {
            if(ind>0)
              endCapOrCapClose(startInd, false)
            startInd = ind
            moveto(ind)
            ind+=2
            prevt = t            
          }
        case java.awt.geom.PathIterator.SEG_LINETO =>
          {
            if (prevt != PathIterator.SEG_MOVETO)
              join(ind)
            lineto(ind)
            ind+=2
            prevt = t            
          }
        case java.awt.geom.PathIterator.SEG_CLOSE =>
          {
            join(ind)
            lineto(ind)
            ind+=2
            //endCapOrCapClose(startInd, true)
          }
        }
        path.next
      }
      endCapOrCapClose(startInd, false)
      vertsNum = i/2
}
  }

  private def lineto(ind:Int) = {
    emitLineSeg(tmpverts(ind), tmpverts(ind+1), nx, ny)
    curx = tmpverts(ind)
    cury = tmpverts(ind+1)
  }

  private def emitLineSeg(x:Float, y:Float, nx:Float, ny:Float) = {
    verts(i) = x + nx
    verts(i+1) = y + ny
    i+=2
    verts(i) = x - nx
    verts(i+1) = y - ny
    i+=2
  }

  private def moveto(ind:Int) = {
    // normal vector
    var x1:Float = tmpverts(ind)
    var y1:Float = tmpverts(ind+1)
    curx = tmpverts(ind)
    cury = tmpverts(ind+1)
    var x2:Float = tmpverts(ind+2)
    var y2:Float = tmpverts(ind+3)

    var dx:Float = x2 - x1;
    var dy:Float = y2 - y1;

    var pw:Float = 0.0f;

    if (dx == 0.0)
      pw = lineWidth / scala.Math.abs(dy);
    else if (dy == 0.0)
      pw = lineWidth / scala.Math.abs(dx);
    else
      pw = lineWidth / scala.Math.sqrt(dx*dx + dy*dy).floatValue;

    nx = -dy * pw;
    ny = dx * pw;

    stroke.getEndCap match {
      case BasicStroke.CAP_BUTT => {
          verts(i) = curx + nx
          verts(i+1) = cury + ny
          i+=2
      }
      case BasicStroke.CAP_SQUARE => {
          verts(i) = curx - ny + nx
          verts(i+1) = cury + nx +ny
          i+=2
          emitLineSeg(curx - ny, cury + nx, nx, ny)
      }
      case BasicStroke.CAP_ROUND => {
          arcPoints(curx, cury, curx+nx, cury+ny, curx-nx, cury-ny)
          var count = i + arcInd + 2
          i = count
          var front = 0
          var end = arcInd / 2
          while(front != end && count-2>=0 && end>=1) {
            if(count-2>=0) {
              count-=1
              verts(count) = arcVerts(2 * end - 1)
              count-=1
              verts(count) = arcVerts(2 * end - 2)
            }

            end-=1
            if(front != end && count-2>=0) {
              count-=1
              verts(count) = arcVerts(2 * front + 1)
              count-=1
              verts(count) = arcVerts(2 * front + 0)
            }
            front+=1
          }

          if(count>=2) {
            verts(count - 1) = verts(count + 1)
            verts(count - 2) = verts(count + 0)
          }
      }
    }
    emitLineSeg(curx, cury, nx, ny)
  }

  private def join(ind:Int) = {
    // normal vector
    var x1:Float = curx
    var y1:Float = cury
    var x2:Float = tmpverts(ind)
    var y2:Float = tmpverts(ind+1)

    var dx:Float = x2 - x1
    var dy:Float = y2 - y1

    var pw:Float = 0.0f

    if (dx == 0)
        pw = lineWidth / math.abs(dy)
    else if (dy == 0)
        pw = lineWidth / math.abs(dx)
    else
      pw = lineWidth / math.sqrt(dx*dx + dy*dy).floatValue

    nx = -dy * pw
    ny = dx * pw

    stroke.getLineJoin match {
      case BasicStroke.JOIN_BEVEL => {}
      case BasicStroke.JOIN_MITER => {
          val count = i
          val prevNvx = verts(count-2) - curx
          val prevNvy = verts(count-1) - cury
          val xprod = prevNvx * ny - prevNvy * nx
          var px, py, qx, qy = 0.0

          if(xprod <0 ) {
            px = verts(count-2)
            py = verts(count-1)
            qx = curx - nx
            qy = cury - ny
          } else {
            px = verts(count-4)
            py = verts(count-3)
            qx = curx + nx
            qy = cury + ny
          }

          var pu = px * prevNvx + py * prevNvy
          var qv = qx * nx + qy * ny
          var ix = (ny * pu - prevNvy * qv) / xprod
          var iy = (prevNvx * qv - nx * pu) / xprod

        if ((ix - px) * (ix - px) + (iy - py) * (iy - py) <= miter_limit * miter_limit) {
            verts(i) = ix.floatValue
            verts(i+1) = iy.floatValue
            i+=2
            verts(i) = ix.floatValue
            verts(i+1) = iy.floatValue
            i+=2
        }
      }
      case BasicStroke.JOIN_ROUND => {
          val prevNvx = verts(i - 2) - curx
          val prevNvy = verts(i - 1) - cury
          var ii:Int = 0
          if(nx * prevNvy - ny * prevNvx < 0) {
            arcPoints(0, 0, nx, ny, -prevNvx, -prevNvy)
            ii = arcInd / 2
            while( ii > 0 ) {
                emitLineSeg(curx, cury, arcVerts(2*ii - 2), arcVerts(2*ii - 1) )
                ii-=1
            }
          } else {
            arcPoints(0, 0, -prevNvx, -prevNvy, nx, ny)
            ii = 0
            while (ii < arcInd / 2) {
                emitLineSeg(curx, cury, arcVerts(2*ii + 0), arcVerts(2*ii + 1) )
                ii+=1
            }
        }
      }
    }
    emitLineSeg(curx, cury, nx, ny)
  }

  private def arcPoints(cx:Float, cy:Float, fromX:Float, fromY:Float, toX:Float, toY:Float) = {
    var dx1 = fromX - cx
    var dy1 = fromY - cy
    var dx2 = toX - cx
    var dy2 = toY - cy

    val sin_theta = math.sin(math.Pi / roundness).toFloat
    val cos_theta = math.cos(math.Pi / roundness).toFloat

    arcInd = 0    
    while (dx1 * dy2 - dx2 * dy1 < 0) {
      val tmpx = dx1 * cos_theta - dy1 * sin_theta
      val tmpy = dx1 * sin_theta + dy1 * cos_theta
      dx1 = tmpx
      dy1 = tmpy
      arcVerts(arcInd) = cx + dx1
      arcVerts(arcInd+1) = cy + dy1      
      arcInd+=2
    }
    
    while (dx1 * dx2 + dy1 * dy2 < 0) {
      val tmpx = dx1 * cos_theta - dy1 * sin_theta
      val tmpy = dx1 * sin_theta + dy1 * cos_theta
      dx1 = tmpx
      dy1 = tmpy
      arcVerts(arcInd) = cx + dx1
      arcVerts(arcInd+1) = cy + dy1      
      arcInd+=2
    }

    while (dx1 * dy2 - dx2 * dy1 > 0) {
      val tmpx = dx1 * cos_theta - dy1 * sin_theta
      val tmpy = dx1 * sin_theta + dy1 * cos_theta
      dx1 = tmpx
      dy1 = tmpy
      arcVerts(arcInd) = cx + dx1
      arcVerts(arcInd+1) = cy + dy1      
      arcInd+=2
    }
    //if(arcInd>0) arcInd -= 2
  }

  private def endCapOrCapClose(startInd:Int, implicitClose:Boolean) = {
    if(endsAtStart){
      join(startInd+2)
    } else if(implicitClose) {
      join(startInd)
      lineto(startInd)
      join(startInd+2)
    } else {
      endCap()
    }

    verts(i) = verts(i-2)
    verts(i+1) = verts(i-1)
    i+=2
}

  private def endCap() {
    stroke.getEndCap match {
      case BasicStroke.CAP_BUTT => 
      case BasicStroke.CAP_SQUARE => 
        emitLineSeg(curx+ny, cury-nx, nx, ny)
      case BasicStroke.CAP_ROUND => 
        arcPoints(curx, cury, verts(i-2), verts(i-1), verts(i-4), verts(i-3) )
        var front:Int = 1
        var end:Int = (arcInd-2) / 2
        while (front < end) {
          verts(i) =  arcVerts(2*end-2)
          verts(i+1) = arcVerts(2*end-1)
          i+=2
          end-=1
          if (front < end) {
            verts(i) = arcVerts(2*front)
            verts(i+1) = arcVerts(2*front+1)
            i+=2
            front+=1
          }
        }
        verts(i) = verts(i-2)
        verts(i+1) = verts(i-1)
        i+=2
    }
  }
  
  def clear(c: Color) {
    gl.glClearColor(c.getRed/255f, c.getGreen/255f, c.getBlue/255f, c.getAlpha/255f)
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT)
  }
  def deinit() {
    gl.glDeleteBuffers(1, bufferId, 0)
  }
}