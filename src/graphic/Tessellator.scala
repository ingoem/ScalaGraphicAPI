
package graphic

import javax.media.opengl.glu.{GLU, GLUtessellatorCallback}
import javax.media.opengl.GL
import java.awt.Shape
import java.awt.geom.PathIterator

class Tessellator(builder: GeometryBuilder) extends GLUtessellatorCallback {
  import builder._
  
  private val point = new Array[Float](6)
  
  protected var mode = 0
  protected var tempTessIndex = 0 // temporary index
  private var tobj =  javax.media.opengl.glu.GLU.gluNewTess  
  
  javax.media.opengl.glu.GLU.gluTessProperty(tobj, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_POSITIVE)
  javax.media.opengl.glu.GLU.gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, this)
  javax.media.opengl.glu.GLU.gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, this)
  javax.media.opengl.glu.GLU.gluTessCallback(tobj, GLU.GLU_TESS_END, this)
  javax.media.opengl.glu.GLU.gluTessCallback(tobj, GLU.GLU_TESS_ERROR, this)
  javax.media.opengl.glu.GLU.gluTessCallback(tobj, GLU.GLU_TESS_COMBINE, this)
  
  protected def startTessPolygon() {
    GLU.gluTessBeginPolygon(tobj, null)    
    tempTessIndex = 0
  }

  protected def endTessPolygon() {
    GLU.gluTessEndPolygon(tobj)
  }

  protected def startTessContour() {
    GLU.gluTessBeginContour(tobj)
    tempTessIndex = 0
  }

  protected def endTessContour() {
    GLU.gluTessEndContour(tobj)
  }

  protected def addVertexToTess(x: Float, y: Float) {
    val p: Array[Double] = Array(x.toDouble, y.toDouble, 0)
    GLU.gluTessVertex(tobj, p, 0, p)
  }

  protected def setWindRule(rule: Int) {
      rule match {
      case PathIterator.WIND_EVEN_ODD =>
        GLU.gluTessProperty(tobj, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ODD)
      case PathIterator.WIND_NON_ZERO =>
        GLU.gluTessProperty(tobj, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_NONZERO)
    }    
  }

  override def begin(mode: Int) {
    this.mode = mode
  }
  
  override def end() {
    var st = 0
    var end = tempTessIndex    
    
    mode match {
      case GL.GL_TRIANGLE_FAN =>
        st+=2
        addVertex(tmpVertex(st), tmpVertex(st+1))
        while(st < end){
          addVertex(tmpVertex(st), tmpVertex(st+1))
          st+=2
          addVertex(tmpVertex(0), tmpVertex(1))
          if(st<end){
            addVertex(tmpVertex(st), tmpVertex(st+1))
            st+=2
          }
          if(st<end){
            addVertex(tmpVertex(st), tmpVertex(st+1))
            st+=2
            addVertex(coord(gvi-2), coord(gvi-1))
          }
        }
        addVertex(coord(gvi-2), coord(gvi-1))

      case GL.GL_TRIANGLE_STRIP =>
        addVertex(tmpVertex(st), tmpVertex(st+1))
        while(st < end) {
          addVertex(tmpVertex(st), tmpVertex(st+1))
          st+=2
        }
        addVertex(coord(gvi-2), coord(gvi-1))

      case GL.GL_TRIANGLES =>
        var tri = 0
        while(st < end) {
          if(tri==0) {
            addVertex(tmpVertex(st), tmpVertex(st+1))
          }
          addVertex(tmpVertex(st), tmpVertex(st+1))
          st+=2
          tri+=1
          if(tri==3) {
            addVertex(coord(gvi-2), coord(gvi-1))
            tri = 0
          }
        }
      
      case _ =>
        System.err.println("Tessellation mode error!")
    }
    tempTessIndex = 0
  }
  
  override def vertex(vertexData: Any) {
    val data:Array[Double] = vertexData.asInstanceOf[Array[Double]]
    addTmpVertex(data(0).toFloat, data(1).toFloat, tempTessIndex)
    tempTessIndex+=2
  }


  override
  def vertexData(vertexData: Any, polygonData: Any) {
        //println("vertex data")
    }

  override
  def combine(coords: Array[Double], data: Array[java.lang.Object], weight: Array[Float], outData: Array[java.lang.Object]){
      outData(0) = coords
    }

  override
  def combineData(coords: Array[Double], data: Array[java.lang.Object],
        weight: Array[Float], outData: Array[java.lang.Object], polygonData: Any) {
        //println("combine data")
    }

  override
  def error(errnum: Int) {
        //error("Tessellation Error: ")// + GLU.gluErrorString(errnum))
        System.err.println("Tessellation Error")
    }
  override
  def beginData(i: Int, o: Any) {
        //println("beginData")
    }

  override
  def edgeFlag(bln: Boolean) {
        //println("edgeFlag")
    }

  override
  def edgeFlagData(bln: Boolean, o: Any) {
        //println("edgeFlagData")
    }

  override
  def endData(o: Any) {
        //println("endData")
    }

  override
  def errorData(i: Int, o: Any) {
        //println("errorData")
    }
  
  def tessellate(shape: Shape)  {
    val path = shape.getPathIterator(null, flatnessFactor(shape))
    this.setWindRule(path.getWindingRule)
    this.startTessPolygon
    //tess.startTessContour
    gvi = 0
    while(!path.isDone) {
      path.currentSegment(point) match {
        case java.awt.geom.PathIterator.SEG_MOVETO =>
          this.startTessContour
          this.addVertexToTess(point(0), point(1))
        case java.awt.geom.PathIterator.SEG_LINETO =>
          this.addVertexToTess(point(0), point(1))
        case java.awt.geom.PathIterator.SEG_CLOSE =>
          this.endTessContour
        case _ =>
          System.err.println("PathIterator contract violated")
      }
      path.next
    }
    //tess.endTessContour
    this.endTessPolygon
  }

  def tessellateConvex(shape: Shape) {
    val path = shape.getPathIterator(null, flatnessFactor(shape))
    var z = 0
    gvi = 0
    while(!path.isDone) {
      path.currentSegment(point) match {
        case java.awt.geom.PathIterator.SEG_MOVETO =>
          z = 0
          addTmpVertex(point(0), point(1), z)
          z+=2
        case java.awt.geom.PathIterator.SEG_LINETO =>
          addTmpVertex(point(0), point(1), z)
          z+=2
        case java.awt.geom.PathIterator.SEG_CLOSE =>
          var st = 0
          var end = z
          while(st < end) {
            addVertex(tmpVertex(st), tmpVertex(st+1))
            st+=2
            addVertex(tmpVertex(end-2), tmpVertex(end-1))
            end-=2
          }
          //addVertex(verts(i-2), verts(i-1))
          addVertex(coord(gvi-2), coord(gvi-1))
        case _ =>
          Predef.error("PathIterator contract violated")
      }
      path.next
    }
    //vertsNum = gvi/2
    gvi = 0
    ind = 0
  }
}
