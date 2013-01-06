import play.api._
import play.api.db.DB
import play.api.mvc._
import play.api.mvc.Results._
import slick.driver.ExtendedProfile
import controllers.Store
import play.api.Play.current
import Store.driver.simple._


object Global extends GlobalSettings {

    override def onStart(app: Application) {
        lazy val db = Database.forDataSource(DB.getDataSource())
    }


    override def onHandlerNotFound(request: RequestHeader): Result = {
        NotFound(views.html.notFound())
    }

}