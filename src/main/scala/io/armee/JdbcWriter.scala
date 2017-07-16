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
import io.armee.messages.EventGeneratorMessages.MonitorRequests
import scalikejdbc.{AutoSession, ConnectionPool, WrappedResultSet}
import scalikejdbc._

class JdbcWriter() extends Actor with ActorLogging{

  ConnectionPool.singleton("jdbc:mysql://localhost:3306/michel", "root", "michelnossin")

  // ad-hoc session provider on the REPL
  implicit val session = AutoSession

  // table creation, you can run DDL by using #execute as same as JDBC
  sql"""
drop table members
    """.execute.apply()

  sql"""
create table members (
  id serial not null primary key,
  name varchar(64),
  created_at timestamp not null
)
""".execute.apply()

  // insert initial data
  Seq("Alice", "Bob", "Chris") foreach { name =>
    sql"insert into members (name, created_at) values (${name}, current_timestamp)".update.apply()
  }

  // for now, retrieves all data as Map value
  val entities: List[Map[String, Any]] = sql"select * from members".map(_.toMap).list.apply()

  // defines entity object and extractor
  import org.joda.time._
  case class Member(id: Long, name: Option[String], createdAt: DateTime)
  object Member extends SQLSyntaxSupport[Member] {
    override val tableName = "members"
    def apply(rs: WrappedResultSet) = new Member(
      rs.long("id"), rs.stringOpt("name"), rs.jodaDateTime("created_at"))
  }

  // find all members
  val members: List[Member] = sql"select * from members".map(rs => Member(rs)).list.apply()


  def receive = {
    case MonitorRequests() => {}

    case _: MemberEvent => // ignore
  }

}
