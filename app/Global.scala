import play.api._
import play.api.db.DB
import play.api.mvc._
import play.api.mvc.Results._
import slick.driver.ExtendedProfile
import controllers.Store


object Global extends GlobalSettings {

    import Store.driver.simple._
    import play.api.Play.current

    override def onStart(app: Application) {
        lazy val db = Database.forDataSource(DB.getDataSource())
    }


    override def onHandlerNotFound(request: RequestHeader): Result = {
        NotFound(views.html.notFound())
    }

}