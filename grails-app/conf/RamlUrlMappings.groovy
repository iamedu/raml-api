import org.apache.commons.logging.LogFactory

class RamlUrlMappings {
  
  static mappings = { applicationContext ->
    def logger = LogFactory.getLog(RamlUrlMappings)

    def patternList = applicationContext.grailsApplication.config.api.raml.mappings
    def ramlDefinition = applicationContext.grailsApplication.config.api.raml.ramlExportUrl

    if(!patternList) {
      patternList = ['/api']
    }

    logger.info "Setting url mappings for RAML controller:"

    if(ramlDefinition) {
      "${ramlDefinition}/**"(controller:"ramlDocumentation")
      logger.info("${ramlDefinition}/**")
    }

    patternList.each { pattern ->
      "${pattern}"(controller:"ramlApiHandler")
      "${pattern}/**"(controller:"ramlApiHandler")
      logger.info("${pattern}")
      logger.info("${pattern}/**")
    }

  }
}

