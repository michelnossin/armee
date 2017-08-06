package playground

import scalikejdbc._;
/*
To test mysql (on MAC):
Install mysql on localhost (https://dev.mysql.com/downloads/file/?id=469584)
Install driver mysql on localhost
curl -L http://git.io/dbconsole | sh
source /Users/michelnossin/.bash_profile
dbconsole -e
<change mysql settings:
  mysql.jdbc.url=jdbc:mysql://localhost:3306/michel
  mysql.jdbc.username=root
  mysql.jdbc.password=michelnossin
  >
sudo launchctl load -F /Library/LaunchDaemons/com.oracle.oss.mysql.mysqld.plist
/usr/local/mysql/bin/mysql -u root -p
  SET PASSWORD FOR 'root'@'localhost' = PASSWORD('michelnossin');

  to test, first set connector driver in classpath :
    export CLASSPATH=/path/mysql-connector-java-ver-bin.jar:$CLASSPATH
  or java -cp <classpath>
  or Intellij -> file -> project structure -> libraries -> add the mysql-<version>.bin.jar path

    Run this application
    sudo launchctl unload -F /Library/LaunchDaemons/com.oracle.oss.mysql.mysqld.plist
    */

    object JdbcTest extends App{

  ConnectionPool.singleton("jdbc:mysql://localhost:3306/michel", "root", "michelnossin")

  //jdbc:mysql://localhost/test?user=minty&password=greatsqldb

  //#sandbox.jdbc.url=jdbc:h2:file:./db/sandbox
  //#sandbox.jdbc.username=
  //#sandbox.jdbc.password=
  //mysql.jdbc.url=jdbc:mysql://localhost:3306/michel
  //mysql.jdbc.username=root
  //mysql.jdbc.password=michelnossin
  //#postgres.jdbc.url=jdbc:postgresql://localhost:5432/dbname
  //#oracle.jdbc.url=jdbc:oracle:thin:@localhost:1521:dbname


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
  members.foreach(x => print("x:" + x.toString))
  // use paste mode (:paste) on the Scala REPL
  //val m = Member.syntax("m")
  //val name = "Alice"
  //val alice: Option[Member] = withSQL {
  //  select.from(Member as m).where.eq(m.name, name)
  //}.map(rs => Member(rs)).single.apply()
}
