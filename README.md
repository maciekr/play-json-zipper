# JsZipper [![Build Status](https://travis-ci.org/michaelahlers/play-json-zipper.svg?branch=v1.2.0.24)](https://travis-ci.org/michaelahlers/play-json-zipper) [![Coverage Status](https://coveralls.io/repos/github/michaelahlers/play-json-zipper/badge.svg?branch=v1.2.0.24)](https://coveralls.io/github/michaelahlers/play-json-zipper?branch=v1.2.0.24)

See [this project's wiki](https://github.com/michaelahlers/play-json-zipper/wiki) for installation instructions, and project rationale.

## Usage

We'll use following JSON object.

```scala
import play.api.libs.json._
import play.api.libs.json.monad.syntax._
import play.api.libs.json.extensions._

val js = Json.obj(
  "key1" -> Json.obj(
    "key11" -> "TO_FIND",
    "key12" -> 123L,
    "key13" -> JsNull
  ),
  "key2" -> 123,
  "key3" -> true,
  "key4" -> Json.arr("TO_FIND", 345.6, "test", Json.obj("key411" -> Json.obj("key4111" -> "TO_FIND")))
)

js: play.api.libs.json.JsObject = {"key1":{"key11":"TO_FIND","key12":123,"key13":null},"key2":123,"key3":true,"key4":["TO_FIND",345.6,"test",{"key411":{"key4111":"TO_FIND"}}]}
```

### Basic Manipulations

#### Setting multiple paths/values

```scala
js.set(
  (__ \ "key4")(2) -> JsNumber(765.23),
  (__ \ "key1" \ "key12") -> JsString("toto")
)

res1: play.api.libs.json.JsValue = {"key1":{"key11":"TO_FIND","key12":"toto","key13":null},"key2":123,"key3":true,"key4":["TO_FIND",345.6,765.23,{"key411":{"key4111":"TO_FIND"}}]}
```

#### Deleting Multiple Paths and Values

```scala
js.delete(
  (__ \ "key4")(2),
  (__ \ "key1" \ "key12"),
  (__ \ "key1" \ "key13")
)

res2: play.api.libs.json.JsValue = {"key1":{"key11":"TO_FIND"},"key2":123,"key3":true,"key4":["TO_FIND",345.6,{"key411":{"key4111":"TO_FIND"}}]}
```

#### Finding Paths/Values by Filter

```scala
js.findAll( (_,v) => v == JsString("TO_FIND") ).toList

res5: List[(play.api.libs.json.JsPath, play.api.libs.json.JsValue)] = List(
  (/key1/key11,"TO_FIND"),
  (/key4(0),"TO_FIND"),
  (/key4(3)/key411/key4111,"TO_FIND")
)
```

#### Updating Values According to a Filter Based on a Value

```scala
js.updateAll( (_:JsValue) == JsString("TO_FIND") ){ js =>
  val JsString(str) = js
  JsString(str + "2")
}

res6: play.api.libs.json.JsValue = {"key1":{"key11":"TO_FIND2","key12":123,"key13":null},"key2":123,"key3":true,"key4":["TO_FIND2",345.6,"test",{"key411":{"key4111":"TO_FIND2"}}]}
```

#### Updating Values According to a Filter Based on Path and Value

```scala
js.updateAll{ (path, js) =>
  JsPathExtension.hasKey(path) == Some("key4111")
}{ (path, js) =>
  val JsString(str) = js
  JsString(str + path.path.last)
}

res1: play.api.libs.json.JsValue = {"key1":{"key11":"TO_FIND","key12":123,"key13":null},"key2":123,"key3":true,"key4":["TO_FIND",345.6,"test",{"key411":{"key4111":"TO_FIND/key4111"}}]}
```

#### Creating an Object from Scratch

```scala
val build = JsExtensions.buildJsObject(
  __ \ "key1" \ "key11" -> JsString("toto"),
  __ \ "key1" \ "key12" -> JsNumber(123L),
  (__ \ "key2")(0)      -> JsBoolean(true),
  __ \ "key3"           -> Json.arr(1, 2, 3)
)

build: play.api.libs.json.JsValue = {"key1":{"key11":"toto","key12":123},"key3":[1,2,3],"key2":[true]}
```

### Monadic Manipulations

Let's use `Future` as our Monad.

Imagine you call several services returning `Future[JsValue]` and you want to build/update a `JsObject` from it.
Until now, if you wanted to do that with Play2/Json, it was quite tricky and required some code.

#### Updating Multiple `Future` Values at Given Paths

```scala
val maybeJs = js.setM[Future](
  (__ \ "key4")(2)        -> future{ JsNumber(765.23) },
  (__ \ "key1" \ "key12") -> future{ JsString("toto") }
)

maybeJs: scala.concurrent.Future[play.api.libs.json.JsValue] = scala.concurrent.impl.Promise$DefaultPromise@6beb722d

Await.result(maybeJs, Duration("2 seconds"))

res4: play.api.libs.json.JsValue = {"key1":{"key11":"TO_FIND","key12":"toto","key13":null},"key2":123,"key3":true,"key4":["TO_FIND",345.6,765.23,{"key411":{"key4111":"TO_FIND"}}]}
```

#### Updating Multiple `Future` Values According to a Filter

```scala
val maybeJs = js.updateAllM[Future]( (_:JsValue) == JsString("TO_FIND") ){ js =>
  Future {
    val JsString(str) = js
    JsString(str + "2")
  }
}

maybeJs: scala.concurrent.Future[play.api.libs.json.JsValue] = scala.concurrent.impl.Promise$DefaultPromise@35a4bb1a

Await.result(maybeJs, Duration("2 seconds"))

res6: play.api.libs.json.JsValue = {"key1":{"key11":"TO_FIND2","key12":123,"key13":null},"key2":123,"key3":true,"key4":["TO_FIND2",345.6,"test",{"key411":{"key4111":"TO_FIND2"}}]}
```

## Creating a `Future[JsArray]` from Scratch

```scala
val maybeArr = JsExtensions.buildJsArrayM[Future](
  Future { JsNumber(123.45) },
  Future { JsString("toto") }
)

maybeArr: scala.concurrent.Future[play.api.libs.json.JsValue] = scala.concurrent.impl.Promise$DefaultPromise@220d48e4

scala> Await.result(maybeArr, Duration("2 seconds"))
res0: play.api.libs.json.JsValue = [123.45,"toto"]
```
