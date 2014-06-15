package models

import anorm.SqlQuery
import anorm.SQL
import play.api.db.DB

/**
 * Created by kaffein on 15/06/14.
 */
case class Product(id: Long, ean: Long, name: String, description: String)

case class Warehouse(id: Long, name: String)

case class StockItem(id: Long, productId: Long, warehouseId: Long, quantity: Long)

object Product {

  val sql: SqlQuery = SQL("select * from product order by name asc")

  /**
   * Querying in Anorm with the Streaming API
   * @return
   */
  def getAll: List[Product] = DB.withConnection { implicit connection =>
    sql().map { row =>
      Product(row[Long]("id"), row[Long]("ean"), row[String]("name"), row[String]("description"))
    }.toList
  }

  /**
   * Querying in Anorm with pattern matching
   * @return
   */
  def getAllWithPatterns: List[Product] = DB.withConnection { implicit connection =>
    import anorm.Row
    sql().collect {
      case Row(Some(id: Long), Some(ean: Long), Some(name: String), Some(description: String)) => Product(id, ean, name, description)
    }.toList
  }

}