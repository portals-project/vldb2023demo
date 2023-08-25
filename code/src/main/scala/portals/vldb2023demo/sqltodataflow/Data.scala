package portals.vldb2023demo.sqltodataflow

import portals.application.generator.Generators
import portals.libraries.sql.internals.COMMIT_QUERY
import portals.libraries.sql.internals.TxnQuery
import portals.system.TestSystem
import portals.vldb2023demo.sqltodataflow.Config.N_ATOM_SIZE
import portals.vldb2023demo.Util.*

object Data:
  import Config.*

  private val rand = new scala.util.Random

  //////////////////////////////////////////////////////////////////////////////
  // QUERY TYPES
  //////////////////////////////////////////////////////////////////////////////

  inline def insert_into(table: String, k: Int, v: Int): String =
    s"INSERT INTO $table (k, v) Values ($k, $v)"

  inline def select_from(table: String, k: Int): String =
    s"SELECT * FROM $table WHERE k = $k"

  inline def select_from_where(table: String, v: Int, ks: List[Int]): String =
    s"SELECT * FROM $table WHERE v = $v AND k in (${ks.mkString(", ")})"

  //////////////////////////////////////////////////////////////////////////////
  // NON TRANSACTIONAL
  //////////////////////////////////////////////////////////////////////////////

  def queries: Iterator[Iterator[String]] =
    Iterator
      // info: to make the iterator infinite, use `Iterator.continually` instead
      .fill(N_EVENTS) {
        rand.nextInt(3) match
          case 0 =>
            insert_into("KVTable", rand.nextInt(N_KEYS), rand.nextInt(N_VALUES))
          case 1 =>
            select_from("KVTable", rand.nextInt(N_KEYS))
          case 2 =>
            select_from_where("KVTable", rand.nextInt(N_VALUES), List.fill(6)(rand.nextInt(N_KEYS)))
      }
      .grouped(N_ATOM_SIZE)
      .map(_.iterator)

  def queriesGenerator =
    ThrottledGenerator(
      Generators.fromIteratorOfIterators(queries),
      RATE_LIMIT,
    )

  //////////////////////////////////////////////////////////////////////////////
  // TRANSACTIONAL
  //////////////////////////////////////////////////////////////////////////////

  def transactionalQueries: Iterator[Iterator[TxnQuery]] =
    Iterator
      .fill(N_EVENTS) {
        rand.nextInt(2) match
          case 0 =>
            val txid = rand.nextInt()
            Iterator(
              TxnQuery(insert_into("KVTable", rand.nextInt(N_KEYS), rand.nextInt(N_VALUES)), txid),
              TxnQuery(insert_into("KVTable", rand.nextInt(N_KEYS), rand.nextInt(N_VALUES)), txid),
              TxnQuery(COMMIT_QUERY, txid),
            )
          case 1 =>
            val txid = rand.nextInt()
            Iterator(
              TxnQuery(select_from("KVTable", rand.nextInt(N_KEYS)), txid),
              TxnQuery(COMMIT_QUERY, txid),
            )
      }

  def transactionalQueriesGenerator =
    ThrottledGenerator(
      Generators.fromIteratorOfIterators(transactionalQueries),
      RATE_LIMIT,
    )
