import org.squeryl.internals.DatabaseAdapter
import org.squeryl.{Session, SessionFactory}
import play.api._
import play.api.db.DB
import play.api.mvc._
import play.api.mvc.Results._


object Global extends GlobalSettings {

    override def onStart(app: Application) {
        SessionFactory.concreteFactory = app.configuration.getString("db.default.driver") match {
            case Some("org.h2.Driver") => {
                import org.squeryl.adapters.H2Adapter
                Some(() => getSession(new H2Adapter, app))
            }
            case Some("com.mysql.jdbc.Driver") => {
                import org.squeryl.adapters.MySQLAdapter
                Some(() => getSession(new MySQLAdapter, app))
            }
            case _ => sys.error("Database driver must be either org.h2.Driver or com.mysql.jdbc.Driver")
        }
    }


    def getSession(adapter: DatabaseAdapter, app: Application) = Session.create(DB.getConnection()(app), adapter)


    override def onHandlerNotFound(request: RequestHeader): Result = {
        NotFound(views.html.notFound())
    }

}