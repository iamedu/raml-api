package iamedu.raml.exception.handlers

interface UserExceptionHandler<T> {
  Object handleException(T exception)
}
