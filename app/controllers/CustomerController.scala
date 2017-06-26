package controllers

import models._
import play.modules.reactivemongo.json.collection._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import models.Candidate.candidateJsonFormat
import reactivemongo.play.json.collection.JSONColletion

import models.Candidate._

import play.api.mvc.{Action, Controller}
import play.api.data.Form
import play.api.data.Forms._
import play.api.{Logger, db}

import scala.concurrent.{ExecutionContext, Future}

class CustomerController(val reactiveMongoApi:ReactiveMongoApi)(implicit ec: ExecutionContext)
  extends Controller with MongoController with ReactiveMongoComponents{

  def collectionF:Future[JSONCollection]  = db.map(_.collection[JSONCollection]("candidates"))

  val candidateForm = Form(
    mapping(
      "name"-> text,
      "email" -> optional(text),
      "skype" -> optional(text)
    )(Candidate.apply)(Candidate.unapply)
  )

  def get = Action.async{
    implicit request =>
    val emptyForm = candidateForm
      Future.successful(
        Ok(views.html.mongoform(candidateForm))
      )
  }

  def post = Action.async{
    implicit request =>
      candidateForm.bindFromRequest().fold(
        //Error
        formWithErrors => {
          Logger.error("form has errors")
          Future.successful(BadRequest(views.html.mongoform(candidateForm)))
        },
          //Bind
          candidate => {
            val futureResult = collectionF.flatMap(_.insert(candidate))
            futureResult.map(r=>Ok(r.message))

            //Future.successful(Ok(candidate.toString))
          }
    )

  }


}