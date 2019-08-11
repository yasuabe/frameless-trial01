package com.nothing

import cats.data.ReaderT
import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.mtl.ApplicativeAsk
import cats.mtl.instances.local.askReader
import cats.syntax.functor._
import cats.syntax.flatMap._
import com.nothing.common.UseSpark
import frameless.TypedDataset
import org.apache.spark.SparkConf
import frameless.cats.implicits._
import org.apache.spark.sql.SparkSession

case class Bar(b: String, a: Long)
case class Foo(f: Long, o: Bar)

object common {
  trait UseSpark[F[_]] {
    private def acquire(implicit S: Sync[F]): F[SparkSession] = S.delay {
      val conf = new SparkConf() .setMaster("local[*]") .setAppName("frameless-first-example") .set("spark.ui.enabled", "false")
      implicit val spark: SparkSession = SparkSession.builder().config(conf).appName("trial02").getOrCreate()

      spark.sparkContext.setLogLevel("WARN")
      spark
    }
    private def release(spark: SparkSession)(implicit S: Sync[F]): F[Unit] = S.delay(spark.stop())

    def useSpark(use: SparkSession => F[Unit])(implicit S: Sync[F]): F[Unit] =
      S.bracket(acquire)(use)(release)
  }
  type SparkAsk[F[_]] = ApplicativeAsk[F, SparkSession]
  type Action[T] = ReaderT[IO, SparkSession, T]
  implicit val readerIOApplicativeAsk: SparkAsk[Action] =
    askReader[IO, SparkSession]
}
object trial02 {
  import common._
  def program[F[_]](implicit S: Sync[F], F: SparkAsk[F]): F[Unit] = F.ask.flatMap { implicit spark =>
    val fTypedDataset = TypedDataset.create(Foo(1,Bar("a",2)) :: Foo(10,Bar("b",20)) :: Nil)
    fTypedDataset.show[F]()
  }
}
object MainFramelesstrial01 extends IOApp with UseSpark[IO] {
  import common._
  import trial02._

  def run(args: List[String]): IO[ExitCode] =
    useSpark(program[Action].run) as ExitCode.Success
}

