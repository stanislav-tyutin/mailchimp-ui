package com.tbrain.mailchimp_ui


import com.tbrain.mailchimp_api._
import json_objects._

/**
  * Created by stanislav on 20.01.17.
  */
object ApiCore {

  private[this] val cores = scala.collection.mutable.Map[String, MailChimpApi]()

  private def getApi(accessToken: String): MailChimpApi = cores(accessToken)

  def getAccessToken(code: String): String = {
    val api = MailChimpApiByCode(client_id = "469054286787", client_secret = "7e9ebde81ed4b2d8c48ad627de8e2271",
      redirectUri = "http://127.0.0.1:8080/oauth", code = code)
    cores(api.authValue) = api
    api.authValue
  }

  def getLists(accessToken: String): Seq[Map[String, String]] = {
    getApi(accessToken).getLists.map(x => Map("id" -> x.id.getOrElse(null), "name" -> x.name))
  }

  def getListMembers(accessToken: String, list_id: String): Seq[String] = {
    getApi(accessToken).getListMembers(list_id).map(x => x.email_address)
  }

  def createList(accessToken: String, name: String) {
    val list = MailingList(Option(null), name,
      Contact("Example company", "address", "VRN", "VOR", "394000", "RU"),
      "Mailing List",
      MailingListCampaignDefaults("TBrain", "api@tbrain.ru", "Test mail", "Russian"), true)
    getApi(accessToken).addList(list)
  }

  def createEmail(accessToken: String, list_id: String, email: String) {
    val member = MailingListMember(email, "subscribed")
    val list = getApi(accessToken).getList(list_id)
    getApi(accessToken).addListMember(list, member)
  }

  def deleteEmail(accessToken: String, list_id: String, email: String) {
    val list = getApi(accessToken).getList(list_id)
    val target = getApi(accessToken).getListMembers(list).filter(x => x.email_address == email).head
    getApi(accessToken).deleteListMember(list, target)
  }

  def runCampaign(accessToken: String, list_id: String, text: String) {
    var cam = Campaign(Option(null), "plaintext", CampaignRecipients(list_id),
      CampaignSettings("Test mail", "Test mail", "TBrain", "robot@tbrain.ru"))
    cam = getApi(accessToken).addCampaign(cam)

    getApi(accessToken).setCampaignContent(cam, text)

    getApi(accessToken).sendCampaign(cam)
  }

  //  def getCampaigns(accessToken: String): Seq[Map[String, String]] = {
  //    getApi(accessToken).getCampaigns.map(x=>Map("id" -> x.id, "name"->x.))
  //  }
}
