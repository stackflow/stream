package com.lordmancer2.stream.clan

import java.util.Date

import com.lordmancer2.stream.{Equipment, Resources}

object State {

  case class Member(userId: String,
                    heroId: String,
                    roleId: Option[String],
                    updated: Date)

  case class Storage(things: Seq[Equipment.Thing],
                     resources: Seq[Resources.SimpleResource])

  case class StorageSettings(maxResources: Map[String, Long],
                             maxSlots: Int)

  case class ClanSettings(maxMembers: Int,
                          storage: StorageSettings)

  case class State(id: String,
                   title: String,
                   description: String,
                   created: Date,
                   members: Seq[Member],
                   requests: Seq[Member],
                   removed: Option[Date],
                   storage: Storage,
                   settings: ClanSettings)

}