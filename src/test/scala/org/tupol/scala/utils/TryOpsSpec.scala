package org.tupol.scala.utils

import org.scalatest.{ FunSuite, Matchers }

import scala.collection.mutable.ArrayBuffer
import scala.util.{ Failure, Success, Try }

class TryOpsSpec extends FunSuite with Matchers {

  test("Success.logSuccess") {
    val logger = ArrayBuffer[Int]()
    Success(1).logSuccess(message => logger.append(message))
    logger should contain theSameElementsAs Seq(1)
  }

  test("Failure.logSuccess") {
    val logger = ArrayBuffer[Int]()
    Failure[Int](new Exception("")).logSuccess(message => logger.append(message))
    logger.isEmpty shouldBe true
  }

  test("Success.logFailure") {
    val logger = ArrayBuffer[String]()
    Success(1).logFailure(throwable => logger.append(throwable.getMessage))
    logger.isEmpty shouldBe true
  }

  test("Failure.logFailure") {
    val logger = ArrayBuffer[String]()
    Failure[Int](new Exception("1")).logFailure(throwable => logger.append(throwable.getMessage))
    logger should contain theSameElementsAs Seq("1")
  }

  test("Success.log") {
    val loggerSuccess = ArrayBuffer[Int]()
    val loggerFailure = ArrayBuffer[String]()
    Success(1).log(
      message => loggerSuccess.append(message),
      throwable => loggerFailure.append(throwable.getMessage)
    )
    loggerSuccess should contain theSameElementsAs Seq(1)
    loggerFailure.isEmpty shouldBe true
  }

  test("Failure.log") {
    val loggerSuccess = ArrayBuffer[Int]()
    val loggerFailure = ArrayBuffer[String]()
    Failure[Int](new Exception("1")).log(
      message => loggerSuccess.append(message),
      throwable => loggerFailure.append(throwable.getMessage)
    )
    loggerSuccess.isEmpty shouldBe true
    loggerFailure should contain theSameElementsAs Seq("1")
  }

  test("allOkOrFail yields a Success for a list of a single element") {
    val result = Seq(Success(1)).allOkOrFail
    result shouldBe a[Success[_]]
    result.get should contain theSameElementsAs (Seq(1))
  }

  test("allOkOrFail yields a Success") {
    val result = Seq(Success(1), Success(2)).allOkOrFail
    result shouldBe a[Success[_]]
    result.get should contain theSameElementsAs (Seq(1, 2))
  }

  test("allOkOrFail for an empty list yields a Success") {
    val result = Seq[Try[Int]]().allOkOrFail
    result shouldBe a[Success[_]]
    result.get.isEmpty shouldBe true
  }

  test("allOkOrFail yields a Failure") {
    val result = Seq(Success(1), Failure[Int](new Exception("ex"))).allOkOrFail
    result shouldBe a[Failure[_]]
  }

  test("tryWithCloseable should be successful if everything goes well and the resource should be closed") {
    val expectedResult = 111
    var closedFlag: Boolean = false
    trait Resource extends AutoCloseable {
      override def close(): Unit = closedFlag = true
    }
    def code(r: Resource) = expectedResult
    val result = tryWithCloseable(new Resource {})(code)
    result shouldBe a[Success[_]]
    result.get shouldBe expectedResult
    closedFlag shouldBe true
  }

  test("tryWithCloseable should be successful if everything goes well, even if the resource fails closing") {
    val expectedResult = 111
    var closedFlag: Boolean = false
    class ResourceException extends Exception("")
    trait Resource extends AutoCloseable {
      override def close(): Unit = throw new ResourceException
    }
    def code(r: Resource) = expectedResult
    val result = tryWithCloseable(new Resource {})(code)
    result shouldBe a[Success[_]]
    result.get shouldBe expectedResult
    closedFlag shouldBe false
  }

  test("tryWithCloseable should fail if the code fails and the resource should be closed") {
    var closedFlag: Boolean = false
    trait Resource extends AutoCloseable {
      override def close(): Unit = closedFlag = true
    }
    class CodeException extends Exception("")
    def code(r: Resource) = throw new CodeException
    val result = tryWithCloseable(new Resource {})(code)
    result shouldBe a[Failure[_]]
    a[CodeException] should be thrownBy result.get
    closedFlag shouldBe true
  }

}
