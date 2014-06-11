package controllers

import models.Product
import play.api.mvc.Action
import play.api.mvc.Controller

/**
 * Created by kaffein on 12/06/14.
 */
object Products extends Controller {

  def list = Action { implicit request =>
    var products = Product.findAll
    Ok(views.html.products.list(products))
  }

}