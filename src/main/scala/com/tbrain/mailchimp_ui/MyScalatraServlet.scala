package com.tbrain.mailchimp_ui

import org.scalatra._
import spray.json._
import DefaultJsonProtocol._

class MyScalatraServlet extends MailchimpuiStack {

  get("/") {
    if (session.contains("access_token")) redirect("/ui")
    else {
      contentType = "text/html"
      ssp("/index", "title" -> "Mailchimp Simple UI",
        "mailchimp_link" -> "https://login.mailchimp.com/oauth2/authorize?response_type=code&client_id=469054286787&redirect_uri=http%3A%2F%2F127.0.0.1%3A8080%2Foauth")
    }
  }

  get("/oauth") {
    params match {
      case _ if params.contains("code") => {
        val access_token = ApiCore.getAccessToken(params("code"))
        session("access_token") = access_token
        redirect("/ui")
      }
      case _ if params.contains("error") => {
        contentType = "text/html"
        ssp("/oauth_error", "title" -> "Mailchimp Simple UI")
      }
      case _ => {
        redirect("/")
      }
    }
  }

  get("/ui") {
    contentType = "text/html"
    ssp("/ui", "title" -> "Mailchimp Simple UI")
  }

  get("/logout") {
    session.remove("access_token")
    flash("notice") = "Вы вышли из аккаунта Mailchimp"
    redirect("/")
  }

  get("/method/:name") {
    contentType = "application/json"
    val methodName = params("name")
    methodName match {
      case "getLists" => ApiCore.getLists(session("access_token").asInstanceOf[String]).toJson
      case "getListMembers" =>
        val list_id = params("list_id")
        ApiCore.getListMembers(session("access_token").asInstanceOf[String], list_id).toJson
      case _ => "Nothing to do"
    }
  }

  post("/method/:name") {
    //    contentType = "application/json"
    val methodName = params("name")
    methodName match {
      case "createList" =>
        ApiCore.createList(session("access_token").asInstanceOf[String], params("list_name"))
        "ok"

      case "addEmail" =>
        ApiCore.createEmail(session("access_token").asInstanceOf[String], params("list_id"), params("email"))
        "ok"

      case "sendMessages" =>
        ApiCore.runCampaign(session("access_token").asInstanceOf[String], params("list_id"), params("text"))
        "ok"

      case "deleteEmail" =>
        ApiCore.deleteEmail(session("access_token").asInstanceOf[String], params("list_id"), params("email"))
        "ok"

      case _ => "Nothing to do"
    }
  }


}
