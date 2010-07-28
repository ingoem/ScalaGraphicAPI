package graphic

import javax.media.opengl._
import javax.media.opengl.fixedfunc.GLMatrixFunc
import javax.media.opengl.glu.GLU
import java.nio.{ByteBuffer, ByteOrder, FloatBuffer}
import java.util.ArrayList
import java.awt.Shape
import java.awt.geom._
import java.awt.{Font, Color, BasicStroke}

class Stroker(builder: GeometryBuilder) {
  import builder._
  private val point = new Array[Float](6)
  private var curx = 0.0f
  private var cury = 0.0f

  def stroke(shape: Shape, stroke: BasicStroke, noDash: Boolean) {
    val iter = shape match {
      case arc: Arc2D if !noDash => 
        val p = new Path2D.Float
        p.append(shape.getPathIterator(null, 1.0), false)
        shape.getPathIterator(null, flatnessFactor(shape)).currentSegment(point)
        if(arc.getArcType != Arc2D.OPEN)
          p.lineTo(point(0), point(1))
        p.closePath
        p.getPathIterator(null, 1.0)
      case _ => shape.getPathIterator(null, flatnessFactor(shape))
    }

    val strokePath = new Path2D.Float
    var len = 10.0f // lenght of stroke segment
    var prevx = 0.0f
    var prevy = 0.0f
    var next = false // wheater to go to next point form path iterator
    var space = false // wheater is space or stroke
    var tDist= 0.0f // temporary distance from prev point, when dist < len
    var inter = 0 // determine when to start space or stroke
    val dash = stroke.getDashArray
    var f = false

    gvi=0

    if(!noDash) {
      //len = dash(0) + stroke.getDashPhase// stroke
      len = stroke.getDashPhase % (dash(0)+dash(1))
      if(len <= dash(0)) {
        inter = 0 // stroke
      } else {
        inter = 1
        len = len - dash(0) // break
      }
    }
    while(!iter.isDone){
      iter.currentSegment(point) match {
        case PathIterator.SEG_MOVETO =>
          if(noDash) {
            addTmpVertex(point(0), point(1), gvi)
            gvi += 2
          } else {
            if(inter%2 == 0) {
              strokePath.moveTo(point(0), point(1))
//            inter = 0
              addTmpVertex(point(0), point(1), gvi)
              gvi += 2
            }
            prevx = point(0)
            prevy = point(1)
          }
      case PathIterator.SEG_LINETO =>
        if(noDash) {
          addTmpVertex(point(0), point(1), gvi)
          gvi += 2
        } else {
          while(!next) {
            val dist = math.sqrt((point(0)-prevx)*(point(0)-prevx) + (point(1)-prevy)*(point(1)-prevy)).toFloat
            if(dist+tDist > len) {
              val x1 = point(0) - prevx
              val y1 = point(1) - prevy
              val mtp1 = (x1/dist)*(len-tDist) + prevx
              val mtp2 = (y1/dist)*(len-tDist) + prevy
              tDist = 0
              prevx = mtp1
              prevy = mtp2
              // end point
              if(inter%2 == 0) {
                //if(first == true)
                  strokePath.lineTo(mtp1.toFloat, mtp2.toFloat)
                len = dash(1) // space
                addTmpVertex(mtp1.toFloat, mtp2.toFloat, gvi)
                gvi += 2
              }
              else {
                //first = true
                strokePath.moveTo(prevx, prevy)
                len = dash(0) // stroke
                addTmpVertex(prevx, prevy, gvi)
                gvi += 2
              }
              inter += 1
            } else {
              tDist += dist
              prevx = point(0)
              prevy = point(1)
              if(inter%2 == 0){ // dont add when break
                strokePath.lineTo(point(0), point(1)) // middle point
                addTmpVertex(point(0), point(1), gvi)
                gvi += 2
              }
              next = true
            }
          }
          if(tDist >= len) tDist= 0.0f
          next=false
        }
        case PathIterator.SEG_CLOSE =>
        case _ => Predef.error("PathIterator contract violated")
      }
      iter.next
    }
    if(!noDash) strokePath.closePath
    storeCapsAndJoins(shape, strokePath, stroke, noDash)
  }
  
  // TODO: is that the right name?
  private def storeCapsAndJoins(shape: Shape, stroked: Shape, stroke: BasicStroke, noDash: Boolean) {
    def thinLine = stroke.getLineWidth <= 0.75
    
    gvi = 0
    ind = 0
    var prevt = 0
    var startInd = 0
    val iter = 
      if(noDash) shape.getPathIterator(null, flatnessFactor(shape))
      else stroked.getPathIterator(null, 1.0)
    
    while(!iter.isDone){
      iter.currentSegment(point) match {
        case t @ PathIterator.SEG_MOVETO =>
          if(ind>0)
            endCapOrCapClose(stroke, startInd, false)
          startInd = ind
          moveTo(stroke, ind)
          ind+=2
          prevt = t
        case t @ PathIterator.SEG_LINETO =>
          if (prevt != PathIterator.SEG_MOVETO && !thinLine)
            join(stroke, ind)
          lineTo(ind)
          ind+=2
          prevt = t
        case PathIterator.SEG_CLOSE =>
          endCapOrCapClose(stroke, startInd, implicitClose)
        case _ =>
          Predef.error("PathIterator contract violated")
      }
      iter.next
    }
    endCapOrCapClose(stroke, startInd, false)
    //vertsNum = gvi/2
  }
  
    // TODO: rename
  private def pw0(stroke: BasicStroke, dx: Float, dy: Float) = stroke.getLineWidth / 2.0f / 
    (if (dx == 0) math.abs(dy)
    else if (dy == 0) math.abs(dx)
    else math.sqrt(dx*dx + dy*dy).toFloat)
  
  private  def lineTo(ind: Int) {
    emitLineSeg(tmpVertex(ind), tmpVertex(ind+1), nx, ny)
    curx = tmpVertex(ind)
    cury = tmpVertex(ind+1)
  }
    
  private def moveTo(stroke: BasicStroke, ind: Int) {
    // normal vector
    val x1 = tmpVertex(ind)
    val y1 = tmpVertex(ind+1)
    curx = tmpVertex(ind)
    cury = tmpVertex(ind+1)
    val x2 = tmpVertex(ind+2)
    val y2 = tmpVertex(ind+3)
    val dx = x2 - x1
    val dy = y2 - y1
    var pw = pw0(stroke, dx, dy)

    nx = -dy * pw
    ny = dx * pw

    stroke.getEndCap match {
      case BasicStroke.CAP_SQUARE =>
        addVertex(curx + nx, cury + ny)
      case BasicStroke.CAP_BUTT =>
        addVertex(curx - ny + nx, cury + nx + ny)
        emitLineSeg(curx - ny, cury + nx, nx, ny)
      case BasicStroke.CAP_ROUND =>
        arcPoints(curx, cury, curx+nx, cury+ny, curx-nx, cury-ny)
        var st = 0
        var end = arcInd
        addVertex(curx + nx, cury + ny)
        addVertex(curx + nx, cury + ny)
        while(end > st){
          addVertex(arcVerts(st), arcVerts(st+1))
          st += 2
          addVertex(arcVerts(end-2), arcVerts(end-1))
          end -= 2
        }
        //addVertex(verts(i-2), verts(i-1))
        addVertex(coord(gvi-2), coord(gvi-1))
        addVertex(curx + nx, cury + ny)
    }
    emitLineSeg(curx, cury, nx, ny)
  }

  

  private def arcPoints(cx: Float, cy: Float, fromX: Float, fromY: Float, toX: Float, toY: Float) {
    var dx1 = fromX - cx
    var dy1 = fromY - cy
    var dx2 = toX - cx
    var dy2 = toY - cy
    val roundFactor = 15.0

    val sin_theta = math.sin(math.Pi/roundFactor).toFloat
    val cos_theta = math.cos(math.Pi/roundFactor).toFloat

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

    while (dx1 * dy2 - dx2 * dy1 >= 0) {
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

  private def endCapOrCapClose(stroke: BasicStroke, startInd: Int, implicitClose: Boolean) {
    if(endsAtStart){
      join(stroke, startInd+2)
    } else if(implicitClose) {
      join(stroke, startInd)
      lineTo(startInd)
      join(stroke, startInd+2)
    } else {
      endCap(stroke)
    }
    //addVertex(verts(i-2), verts(i-1))
    addVertex(coord(gvi-2), coord(gvi-1))
  }

  def endCap(stroke: BasicStroke) {
    stroke.getEndCap match {
      case BasicStroke.CAP_SQUARE =>
      case BasicStroke.CAP_BUTT =>
        emitLineSeg(curx+ny, cury-nx, nx, ny)
      case BasicStroke.CAP_ROUND =>
        arcPoints(curx, cury, coord(gvi-2), coord(gvi-1), coord(gvi-4), coord(gvi-3) )
        var front:Int = 1
        var end:Int = (arcInd-2) / 2
        while (front < end) {
          addVertex(arcVerts(2*end-2), arcVerts(2*end-1))
          end-=1
          if (front < end) {
            addVertex(arcVerts(2*front), arcVerts(2*front+1))
            front+=1
          }
        }
        addVertex(coord(gvi-2), coord(gvi-1))
    }
  }
  
  private def join(stroke: BasicStroke, ind: Int) {
    // normal vector
    val x1 = curx
    val y1 = cury
    val x2 = tmpVertex(ind)
    val y2 = tmpVertex(ind+1)
    val dx = x2 - x1
    val dy = y2 - y1
    val pw = pw0(stroke, dx, dy)

    nx = -dy * pw
    ny = dx * pw

    stroke.getLineJoin match {
      case BasicStroke.JOIN_BEVEL =>
      case BasicStroke.JOIN_MITER =>
        val count = gvi
        val prevNvx = coord(count-2) - curx
        val prevNvy = coord(count-1) - cury
        val xprod = prevNvx * ny - prevNvy * nx
        var px, py, qx, qy = 0.0

        if(xprod <0 ) {
          px = coord(count-2)
          py = coord(count-1)
          qx = curx - nx
          qy = cury - ny
        } else {
          px = coord(count-4)
          py = coord(count-3)
          qx = curx + nx
          qy = cury + ny
        }

        var pu = px * prevNvx + py * prevNvy
        var qv = qx * nx + qy * ny
        var ix = (ny * pu - prevNvy * qv) / xprod
        var iy = (prevNvx * qv - nx * pu) / xprod

        if ((ix - px) * (ix - px) + (iy - py) * (iy - py) <= stroke.getMiterLimit * stroke.getMiterLimit) {
          addVertex(ix.toFloat, iy.toFloat)
          addVertex(ix.toFloat, iy.toFloat)
        }
      case BasicStroke.JOIN_ROUND =>
        val prevNvx = coord(gvi-2) - curx
        val prevNvy = coord(gvi-1) - cury
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
    emitLineSeg(curx, cury, nx, ny)
  }
}