import cats.effect.{IO, IOApp}
import kantan.csv._
import kantan.csv.ops._
import kantan.csv.generic._
import java.io.File

import model.Estudiante
import dao.SQLquerys
import config.ConfiguracionDB

//PARA QUE FUNCIONE EL TRANSACTOR
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import com.typesafe.config.ConfigFactory
import doobie.hikari.HikariTransactor
import doobie.implicits._


object Main extends App {
  //LEER EL ARCHIVO
  val ruta_entrada = "src/main/resources/data/estudiantes.csv"
  val dataSource = new File(ruta_entrada)
    .readCsv[List, Estudiante](rfc.withHeader.withCellSeparator(','))
  val estudiantes_csv = dataSource.collect {
    case Right(est) => est
  }
  println("NUMERO DE FILAS LEIDAS EN EL CSV: " + estudiantes_csv.length)


  //INSERTAR LOS DATOS EXTRAIDOS DEL CSV (EXTENDER DE IO APP PARA HACER FUNCIONAR)
  /*
  def run: IO[Unit] =
    SQLquerys.insertAll(estudiantes_csv)
      .flatMap(result => IO.println(s"NUMERO DE REGISTROS INSERTADOS A LA DB: ${result.size}"))
   */


  //TRAER LOS DATOS DE LA DB
  val estudiantes: IO[List[Estudiante]] = ConfiguracionDB.transactor.use { xa =>
    SQLquerys.lista_estudiantes().transact(xa)
  }
  estudiantes.flatMap { lista =>
    IO {
      lista.foreach(est => println(s"\tNombre: ${est.nombre}\t\tEdad: ${est.edad}\t\tCalificación: ${est.calificacion}\t\tGenero: ${est.genero}"))
    }
  }.unsafeRunSync()
  println("ESTA LISTA FUE EXTRAIDA DE UNA BASE DE DATOS")

}