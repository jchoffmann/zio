// Copyright (C) 2017-2018 John A. De Goes. All rights reserved.
package scalaz.effect

/**
 * The `AsyncReturn` class describes the return value of an asynchronous effect
 * that is imported into an `IO` value.
 *
 * Asynchronous effects can return `later`, which represents an uninterruptible
 * asynchronous action, `now` which represents a synchronously computed value,
 * or `maybeLater`, which represents an interruptible asynchronous action.
 */
sealed abstract class AsyncReturn[E, A]
object AsyncReturn {
  type Interruptor = Throwable => Unit

  private final case object Later extends AsyncReturn[Nothing, Nothing]
  // TODO: Optimize this common case to less overhead with opaque types
  final case class Now[E, A](value: FiberResult[E, A]) extends AsyncReturn[E, A]
  final case class MaybeLater[E, A](interruptor: Interruptor) extends AsyncReturn[E, A]

  /**
   * Constructs an `AsyncReturn` that represents an uninterruptible asynchronous
   * action. The action should invoke the callback passed to the handler when
   * the value is available or the action has failed.
   *
   * See `IO.async0` for more information.
   */
  final def later[E, A]: AsyncReturn[E, A] = Later.asInstanceOf[AsyncReturn[E, A]]

  /**
   * Constructs an `AsyncReturn` that represents a synchronous return. The
   * handler should never invoke the callback.
   *
   * See `IO.async0` for more information.
   */
  final def now[E, A](result: FiberResult[E, A]): AsyncReturn[E, A] = Now(result)

  /**
   * Constructs an `AsyncReturn` that represents an interruptible asynchronous
   * action. The action should invoke the callback passed to the handler when
   * the value is available or the action has failed.
   *
   * The specified canceler, which must be idempotent, should attempt to cancel
   * the asynchronous action to avoid wasting resources for an action whose
   * results are no longer needed because the fiber computing them has been
   * terminated.
   */
  final def maybeLater[E, A](interruptor: Interruptor): AsyncReturn[E, A] =
    MaybeLater(interruptor)
}