package iamedu.raml.validator

import iamedu.raml.exception.*

import org.apache.commons.lang.WordUtils
import org.commonjava.mimeparse.MIMEParse
import org.eel.kitchen.jsonschema.main.*
import org.eel.kitchen.jsonschema.util.JsonLoader
import org.codehaus.groovy.grails.web.util.WebUtils

import org.raml.model.*
import org.raml.parser.visitor.*
import org.raml.parser.loader.*

import java.util.*

import grails.converters.JSON

class EndpointValidator {
  Raml raml
  
  String serviceName
  String path
  List<String> params

  Resource resource
  ResourceLoader loader
  Map<String, Action> actions

  EndpointValidator(ResourceLoader loader, Raml raml, String path, Resource resource, List<String> params, Map<String, Action> actions) {
    this.raml     = raml
    this.path     = path
    this.params   = params
    this.resource = resource
    this.loader   = loader
    this.actions  = actions.collectEntries { k, v ->
      [k.toString(), v]
    }

    setup()
  }

  def generateExampleResponse(def request) {
    def action = actions.get(request.method.toUpperCase())
    def ramlResponse = action.getResponses().find { k, v ->
      k.toInteger() < 300
    }

    def statusCode = ramlResponse.key.toInteger()
    def bodyResponse

    def body = ramlResponse.value.body.get("application/json")
    if(body) {
      if(body.example) {
        def resource = "raml/" + body.example.trim()
        def bodyContents = loader.fetchResource(resource)?.getText("UTF-8")
        if(bodyContents) {
          bodyResponse = JSON.parse(bodyContents)
        }
      }
    } else {
      bodyResponse = ramlResponse.value.body.find { k, v ->
        true
      }?.value
    }

    def result = [
      body: bodyResponse,
      statusCode: statusCode,
      contentType: 'application/json'
    ]

    result
  }

  def handleResponse(def request, def response, def error) {
    def action = actions.get(request.method.toUpperCase())
    def statusCode

    def ramlResponse = action.getResponses().find { k, v ->
      k.toInteger() < 300
    }

    if(error) {
      statusCode = 500
    } else if(ramlResponse.value.hasBody() && response == null)  {
      statusCode = 404
    } else {
      statusCode = ramlResponse.key.toInteger()
    }


    def result = [
      body: response,
      statusCode: statusCode
    ]

    if(ramlResponse.value.hasBody()) {
      if(request.headers.get("accept")) {
        def bestMatch = MIMEParse.bestMatch(ramlResponse.value.body.keySet(), request.headers.get("accept")?.first())
        result.contentType = bestMatch
      } else {
        def bestMatch = ramlResponse.value.body.keySet().toList().first()
        result.contentType = bestMatch
      }
    }

    result
  }

  def handleRequest(def request) {
    if(!supportsMethod(request.method)) {
      throw new RamlRequestException("Method ${request.method} for endpoint ${resource} does not exist", request.forwardURI, request.method)
    }

    def queryParams = [:]
    def action = actions.get(request.method.toUpperCase())
    def jsonBody
    def bestMatch

    if(request.queryString) {
      queryParams = WebUtils.fromQueryString(request.queryString)
      queryParams = action.queryParameters.collectEntries { k, v ->
        [k, queryParams.get(k)]
      }
    }


    if(action.hasBody()) {
      bestMatch = MIMEParse.bestMatch(action.body.keySet(), request.getHeader("Accept"))
      def mimeType

      if(bestMatch) {
        mimeType = action.body.get(bestMatch)
      } else {
        throw new RamlRequestException("Unable to find a matching mime type for ${path}", path, request.method)
      }

      if(mimeType.schema) {
        def schemaFormat = JsonLoader.fromString(raml.consolidatedSchemas.get(mimeType.schema))
        def factory = JsonSchemaFactory.defaultFactory()

        def schema = factory.fromSchema(schemaFormat)

        def stringBody = request.JSON.toString()
        jsonBody = JsonLoader.fromString(stringBody)
        def report =  schema.validate(jsonBody)

        if(!report.isSuccess()) {
          throw new RamlRequestException("Invalid body ${stringBody} for resource ${path} method ${request.method}",
            path,
            request.method,
            report,
            stringBody)
        }
      } else {
        def stringBody = request.JSON.toString()
        jsonBody = JsonLoader.fromString(stringBody)
      }
    }

    def headerValues = request.headerNames.toList().collectEntries {
      [it, request.getHeaders(it).toList()]
    }
    def headers = action.headers.collectEntries { k, v ->
      [k, headerValues.get(k.toLowerCase())?.first()]
    }

    headers.put('accept', request.getHeaders("accept").toList())

    def result = [
      hasBody: action.hasBody(),
      serviceName: serviceName,
      jsonBody: jsonBody,
      params: params,
      method: request.method,
      headers: headers,
      queryParams: queryParams,
      requestUrl: request.forwardURI.replaceFirst(request.contextPath, "")
    ]

    result
  }

  boolean supportsMethod(String method) {
    method = method.toUpperCase()

    actions.containsKey(method)
  }

  private def setup() {
    if(!resource.displayName) {
      throw new IllegalArgumentException("Resource ${path} has no display name defined")
    }

    def firstChar = "${resource.displayName.charAt(0)}".toLowerCase()

    serviceName = WordUtils
      .capitalizeFully(resource.displayName)
      .replaceAll(" ", "")
      .replaceFirst(".", firstChar)
    serviceName = serviceName + "Service"
  }

}

