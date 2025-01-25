package dao

import doobie._
import doobie.implicits._

import cats.effect.unsafe.implicits.global
import cats.effect._

import cats.implicits._

import model.Estudiante
import config.ConfiguracionDB

//FUNCION PARA INSERTAR LOS DATOS A LA DB (UNO POR UNO)
object SQLquerys {
  def insert(est: Estudiante): ConnectionIO[Int] = {
    sql"""
     INSERT INTO Estudiante (nombre, edad, calificacion, genero)
     VALUES (
       ${est.nombre},
       ${est.edad},
       ${est.calificacion},
       ${est.genero}
     )
   """.update.run
  }

  //FUNCION PARA METER UNA LISTA DE OBJETOS A UNA DB
  def insertAll(est: List[Estudiante]): IO[List[Int]] = {
    ConfiguracionDB.transactor.use { xa =>
      est.traverse(t => insert(t).transact(xa))
    }
  }

  //FUNCION PARA TRAER UNA LISTA DE OBJETOS A LA DB
  def lista_estudiantes(): ConnectionIO[List[Estudiante]] =
    sql"SELECT nombre, edad, calificacion, genero FROM Estudiante"
      .query[Estudiante]
      .to[List]

}