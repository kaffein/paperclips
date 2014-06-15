package models

import anorm.{SQL, SqlQuery}
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

  /**
   * Querying in Anorm with parser combinators
   */

  import anorm.RowParser

  val productParser: RowParser[Product] = {
    import anorm.SqlParser._
    import anorm._

    long("id") ~ long("ean") ~ str("name") ~ str("description") map {
      case id ~ ean ~ name ~ description => Product(id, ean, name, description)
    }
  }

  import anorm.ResultSetParser

  val productsParser: ResultSetParser[List[Product]] = productParser *

  def getAllWithParsers: List[Product] = DB.withConnection { implicit connection =>
    sql.as(productsParser)
  }

  def productStockItemParser: RowParser[(Product, StockItem)] = {
    import anorm.SqlParser._
    productParser ~ StockItem.stockItemParser map (flatten)
  }

  def getAllProductsWithStockItems: Map[Product, List[StockItem]] = {
    DB.withConnection { implicit connection =>
      val sql = SQL( """
                       |select p.*, s.*
                       |from products p inner join stock_items s
                       |on (p.id = s.product_id)
                     """.stripMargin)
      val results = sql.as(productStockItemParser *)
      results.groupBy { _._1 }.mapValues { _.map { _._2 }}
    }
  }

}

object StockItem {

  import anorm.SqlParser._
  import anorm._

  val stockItemParser: RowParser[StockItem] = {
    long("id") ~ long("product_id") ~ long("warehouse_id") ~ long("quantity") map {
      case id ~ productId ~ warehouseId ~ quantity => StockItem(id, productId, warehouseId, quantity)
    }
  }

  val stockItemsParser: ResultSetParser[List[StockItem]] = stockItemParser *

}