/**
  * Copyright 2013 Pascal Voitot (@mandubian)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package play.api.libs.json

sealed trait Node {
  def result: JsLookupResult

  def filter(fn: JsValue => Boolean) = result match {
    case JsDefined(value) => if (fn(value)) this else Node.empty
    case _ => Node.empty
  }

  def isArray = result match {
    case JsDefined(_: JsArray) => true
    case _ => false
  }

  def isObject = result match {
    case JsDefined(_: JsObject) => true
    case _ => false
  }

  def isEmptyObjArr = result match {
    case JsDefined(JsObject(fields)) if fields.isEmpty => true
    case JsDefined(JsArray(value)) if value.isEmpty => true
    case _ => false
  }

  /* TODO: Determine if true is correct for defined non-object and non-array values (upstream returns false in all cases). */
  def isValue = result match {
    case JsDefined(_: JsObject) => false
    case JsDefined(_: JsArray) => false
    case JsDefined(_) => true
    case _ => false
  }

}

object Node {
  val empty = Node.Empty

  case object Empty extends Node {
    override val result = JsUndefined("undef")
  }

  case class Error(error: (JsPath, String)) extends Node {
    override val result = JsUndefined("error")
  }

  def apply(key: String, value: JsValue): Node = KeyNode(key, value)
  def apply(value: JsValue): Node = PlainNode(value)

  def unapply(node: Node): Option[JsValue] = node.result match {
    case JsDefined(value) => Some(value)
    case _ => None
  }

  def copy(node: Node) = node match {
    case Node.Empty           => Node.Empty
    case KeyNode(key, value)  => KeyNode(key, value)
    case PlainNode(value)     => PlainNode(value)
    case Error(e)             => Error(e)
  }

  def copy(node: Node, newValue: JsValue) = node match {
    case Node.Empty        => Node.Empty
    case KeyNode(key, _)  => KeyNode(key, newValue)
    case PlainNode(_)     => PlainNode(newValue)
    case Error(e)             => Error(e)
  }

  def copyKeyNode(node: Node, newKeyValue: (String, JsValue)) = node match {
    case KeyNode(_, _)    => KeyNode(newKeyValue._1, newKeyValue._2)
    case _                => node
  }
}

case class KeyNode(key: String, override val result: JsLookupResult) extends Node

object KeyNode {
  def apply(key: String, value: JsValue): KeyNode = KeyNode(key, JsDefined(value))
}

case class PlainNode(override val result: JsLookupResult) extends Node

object PlainNode {
  def apply(value: JsValue): PlainNode = PlainNode(JsDefined(value))
}
