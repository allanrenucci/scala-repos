package org.jetbrains.plugins.scala.lang.refactoring.mock

import com.intellij.openapi.editor._
import com.intellij.openapi.editor.impl.DocumentImpl

/**
  * Pavel Fatin
  */
class EditorMock(text: String, offset: Int) extends EditorStub {
  private val selection = new SelectionModelStub()

  override def offsetToLogicalPosition(offset: Int) = {
    val s = text.take(offset)
    new LogicalPosition(
        s.count(_ == '\n'),
        s.reverse.takeWhile(_ != '\n').length) // Workaround for SI-5971 (should be "s.view.reverse.")
  }

  override def logicalPositionToOffset(pos: LogicalPosition) =
    text.split('\n').view.map(_.length + 1).take(pos.line).sum + pos.column

  override def getDocument: Document = new DocumentImpl(text)

  override def getSelectionModel: SelectionModel = selection

  override def getCaretModel: CaretModel =
    new CaretModelMock(offset, offsetToLogicalPosition(offset))

  override def offsetToVisualPosition(
      i: Int, b: Boolean, b1: Boolean): VisualPosition = null
}
