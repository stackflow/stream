package com.lordmancer2.stream

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.lordmancer2.stream.clan.event.ClanEvents

package object json {

  implicit val mapper: ObjectMapper = (new ObjectMapper with ScalaObjectMapper)
    .registerModule(DefaultScalaModule)
//    .registerModule(new JodaModule())
//    .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
//    .configure(SerializationFeature.INDENT_OUTPUT, true)
//    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
    .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
    .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
    .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
    .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)

  mapper.registerSubtypes(
    new NamedType(classOf[ClanEvents.ClanCreated], "ClanEvents.ClanCreated"),
    new NamedType(classOf[ClanEvents.SiegeUpdated], "ClanEvents.SiegeUpdated"),
    new NamedType(classOf[ClanEvents.TitleUpdated], "ClanEvents.TitleUpdated"),
    new NamedType(classOf[ClanEvents.DescriptionUpdated], "ClanEvents.DescriptionUpdated"),
    new NamedType(classOf[ClanEvents.ClanRemoved], "ClanEvents.ClanRemoved"),
    new NamedType(classOf[ClanEvents.RequestAccepted], "ClanEvents.RequestAccepted"),
    new NamedType(classOf[ClanEvents.RequestRejected], "ClanEvents.RequestRejected"),
    new NamedType(classOf[ClanEvents.MemberUpdated], "ClanEvents.MemberUpdated"),
    new NamedType(classOf[ClanEvents.MemberRemoved], "ClanEvents.MemberRemoved"),
    new NamedType(classOf[ClanEvents.ThingsAdded], "ClanEvents.ThingsAdded"),
    new NamedType(classOf[ClanEvents.ThingRemoved], "ClanEvents.ThingRemoved"),
    new NamedType(classOf[ClanEvents.ResourcesUpdated], "ClanEvents.ResourcesUpdated.v1"),
    new NamedType(classOf[ClanEvents.ClanSettingsUpdated], "ClanEvents.ClanSettingsUpdated"),
    new NamedType(classOf[ClanEvents.RequestAdded], "ClanEvents.RequestAdded"),
    new NamedType(classOf[ClanEvents.ClanRequestAdded], "ClanEvents.RequestAddedEx")  //Вычисляемое событие
  )

  /** Трэйт метка, о том что класс при сериализации должен иметь поле $type */
  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "$type")
  trait JsonSerializable

}
