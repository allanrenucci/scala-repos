/*
 * Copyright (c) 2011-2014, ScalaFX Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the ScalaFX Project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE SCALAFX PROJECT OR ITS CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package scalafx.scene.control.cell

import javafx.scene.control.{cell => jfxscc}
import javafx.{collections => jfxc, util => jfxu}

import scala.language.implicitConversions
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.delegate.SFXDelegate
import scalafx.scene.control.{TableCell, TableColumn}
import scalafx.util.StringConverter

/**
  * Companion Object for [[scalafx.scene.control.cell.ChoiceBoxTableCell]].
  *
  * @define CBTC `ChoiceBoxTableCell`
  * @define FTCINIT Creates a [[scalafx.scene.control.ChoiceBox]] cell factory for use in [[scalafx.scene.control.TableColumn]] controls.
  * @define TTYPE  The type of the elements contained within the `TableColumn`.
  * @define CONVPARAM A `StringConverter` to convert the given item (of type T) to a String for displaying to the user.
  * @define ITEMSPARAM Zero or more items that will be shown to the user when the ChoiceBox menu is showing. 
  * @define BUFITEMSPARAM A `ObservableBuffer` containing $ITEMSPARAM
  * @define FTCRET A Function that will return a `TableCell` that is able to work on the type of element contained within the `TableColumn`.
  */
object ChoiceBoxTableCell {

  /**
    * Converts a ScalaFX $CBTC to its JavaFX counterpart.
    *
    * @tparam T $TTYPE
    * @param cell ScalaFX $CBTC
    * @return JavaFX $CBTC
    */
  implicit def sfxChoiceBoxTableCell2jfx[S, T](
      cell: ChoiceBoxTableCell[S, T]): jfxscc.ChoiceBoxTableCell[S, T] =
    if (cell != null) cell.delegate else null

  /**
    * $FTCINIT
    *
    * @tparam T $TTYPE
    * @param items $ITEMSPARAM
    * @return $FTCRET
    */
  def forTableColumn[S, T](
      items: ObservableBuffer[T]): (TableColumn[S, T] => TableCell[S, T]) =
    (view: TableColumn[S, T]) =>
      jfxscc.ChoiceBoxTableCell.forTableColumn[S, T](items).call(view)

  /**
    * Added to satisfy Spec tests.
    */
  @deprecated(
      message = "Use forTableColumn[S, T](ObservableBuffer[T])", since = "1.0")
  def forTableColumn[S, T](items: jfxc.ObservableList[T]) =
    jfxscc.ChoiceBoxTableCell.forTableColumn[S, T](items)

  /**
    * $FTCINIT
    *
    * @tparam T $TTYPE
    * @param converter $CONVPARAM
    * @param items $BUFITEMSPARAM
    * @return $FTCRET
    */
  def forTableColumn[S, T](
      converter: StringConverter[T],
      items: ObservableBuffer[T]): (TableColumn[S, T] => TableCell[S, T]) =
    (view: TableColumn[S, T]) =>
      jfxscc.ChoiceBoxTableCell
        .forTableColumn[S, T](converter, items)
        .call(view)

  /**
    * Added to satisfy Spec tests.
    */
  @deprecated(
      message =
        "Use forTableColumn[S, T](StringConverter[T], ObservableBuffer[T])",
      since = "1.0")
  def forTableColumn[S, T](
      converter: jfxu.StringConverter[T], items: jfxc.ObservableList[T]) =
    jfxscc.ChoiceBoxTableCell.forTableColumn[S, T](converter, items)

  /**
    * $FTCINIT
    *
    * @tparam T $TTYPE
    * @param converter $CONVPARAM
    * @param items $ITEMSPARAM
    * @return $FTCRET
    */
  def forTableColumn[S, T](converter: StringConverter[T],
                           items: T*): (TableColumn[S, T] => TableCell[S, T]) =
    (view: TableColumn[S, T]) =>
      jfxscc.ChoiceBoxTableCell
        .forTableColumn[S, T](converter, items: _*)
        .call(view)

  /**
    * Added to satisfy Spec tests.
    */
  @deprecated(message = "Use forTableColumn[S, T](StringConverter[T], T*)",
              since = "1.0")
  def forTableColumn[S, T](converter: jfxu.StringConverter[T], items: T*) =
    jfxscc.ChoiceBoxTableCell.forTableColumn[S, T](converter, items: _*)

  /**
    * $FTCINIT
    *
    * @tparam T $TTYPE
    * @param items $ITEMSPARAM
    * @return $FTCRET
    */
  def forTableColumn[S, T](items: T*): (TableColumn[S, T] => TableCell[S, T]) =
    (view: TableColumn[S, T]) =>
      jfxscc.ChoiceBoxTableCell.forTableColumn[S, T](items: _*).call(view)

  /**
    * Added to satisfy Spec tests.
    */
  @deprecated(message = "Use forTableColumn[S, T](T*)", since = "1.0")
  def forTableColumn[S, T](items: Array[T]) =
    jfxscc.ChoiceBoxTableCell.forTableColumn[S, T](items: _*)
}

/**
  * Wraps [[http://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/cell/ChoiceBoxListCell.html $CBLC]]
  *
  * @tparam T Type used in this cell
  * @constructor Creates a new $CBLC from a JavaFX $CBLC
  * @param delegate JavaFX $CBLC
  *
  * @define CBLC `ChoiceBoxListCell`
  * @define CONVPARAM A `StringConverter` to convert the given item (of type T) to a String for displaying to the user.
  * @define ITEMSPARAM Zero or more items that will be shown to the user when the ChoiceBox menu is showing. 
  * @define BUFITEMSPARAM A `ObservableBuffer` containing $ITEMSPARAM
  */
class ChoiceBoxTableCell[S, T](
    override val delegate: jfxscc.ChoiceBoxTableCell[S, T] =
      new jfxscc.ChoiceBoxTableCell[S, T])
    extends TableCell[S, T](delegate)
    with ConvertableCell[jfxscc.ChoiceBoxTableCell[S, T], T, T]
    with UpdatableCell[jfxscc.ChoiceBoxTableCell[S, T], T]
    with ItemableCell[jfxscc.ChoiceBoxTableCell[S, T], T]
    with SFXDelegate[jfxscc.ChoiceBoxTableCell[S, T]] {

  /**
    * Creates a default $CBLC instance with the given items being used to populate the ChoiceBox when
    * it is shown.
    *
    * @param items $BUFITEMSPARAM
    */
  def this(items: ObservableBuffer[T]) =
    this(new jfxscc.ChoiceBoxTableCell[S, T](items))

  /**
    * Creates a $CBLC instance with the given items being used to populate the `ChoiceBox` when it is
    * shown, and the StringConverter being used to convert the item in to a user-readable form.
    *
    * @param converter $CONVPARAM
    * @param items $BUFITEMSPARAM
    */
  def this(converter: StringConverter[T], items: ObservableBuffer[T]) =
    this(new jfxscc.ChoiceBoxTableCell[S, T](converter, items))

  /**
    * Creates a $CBLC instance with the given items being used to populate the `ChoiceBox` when it is
    * shown, and the StringConverter being used to convert the item in to a user-readable form.
    *
    * @param converter $CONVPARAM
    * @param items $ITEMSPARAM
    */
  def this(converter: StringConverter[T], items: T*) =
    this(new jfxscc.ChoiceBoxTableCell[S, T](converter, items: _*))

  /**
    * Creates a default $CBLC instance with the given items being used to populate the `ChoiceBox` when
    * it is shown.
    *
    * @param items $ITEMSPARAM
    */
  def this(items: T*) = this(new jfxscc.ChoiceBoxTableCell[S, T](items: _*))
}
