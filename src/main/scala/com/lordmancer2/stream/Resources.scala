package com.lordmancer2.stream

import com.lordmancer2.stream.json.JsonSerializable

object Resources {

  trait ResourceLike extends JsonSerializable {

    def id: String

    def count: Long

  }

  case class SimpleResource(id: String,
                            count: Long) extends ResourceLike

}
