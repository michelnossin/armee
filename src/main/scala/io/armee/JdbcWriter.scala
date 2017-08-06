/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.armee

import akka.actor.{Actor, ActorLogging}
import akka.cluster.ClusterEvent.MemberEvent
import io.armee.messages.EventGeneratorMessages.{JdbcEventTarget, MonitorRequests}
import io.armee.messages.WriterMessages.{ResetDatabase, WriteMessage}
import io.armee.messages.WriterMessages.WriteMessage
import scalikejdbc._;

class JdbcWriter(jdbcTarget : JdbcEventTarget) extends Actor with ActorLogging{

  //ConnectionPool.singleton("jdbc:mysql://localhost:3306/michel", "root", "michelnossin")
  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
    enabled = false,
    singleLineMode = false,
    printUnprocessedStackTrace = false,
    stackTraceDepth= 15,
    logLevel = 'error,
    warningEnabled = false,
    warningThresholdMillis = 3000L,
    warningLogLevel = 'warn
  )


  ConnectionPool.singleton(jdbcTarget.url,jdbcTarget.user,jdbcTarget.password)

  // ad-hoc session provider on the REPL
  implicit val session = AutoSession

  def receive = {
    case MonitorRequests() => {}
    case ResetDatabase() => {
      sql"""
drop table if exists armee
    """.execute.apply()

      sql"""
create table armee (
  message varchar(164),
  created_at timestamp not null
)
""".execute.apply()
    }
    case WriteMessage(message) => {
      sql"insert into armee (message, created_at) values (${message}, current_timestamp)".update.apply()
    }
    case _: MemberEvent => // ignore
  }

}
