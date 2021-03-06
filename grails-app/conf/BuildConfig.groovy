grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.fork = [
    // configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
    //  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],

    // configure settings for the test-app JVM, uses the daemon by default
    test: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
    // configure settings for the run-app JVM
    run: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    // configure settings for the run-war JVM
    war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    // configure settings for the Console UI JVM
    console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]

grails.project.repos.default = "bintray"
grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
        jcenter()
        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
        mavenRepo "https://repository-master.mulesoft.org/releases/"
        mavenRepo "http://dl.bintray.com/upennlib/maven/"
        mavenRepo "http://dl.bintray.com/iamedu/maven/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        // runtime 'mysql:mysql-connector-java:5.1.27'
        compile 'iamedu.api:api-annotations:0.1.0'
        compile "org.raml:raml-parser:0.8.6"
        compile "commons-beanutils:commons-beanutils:1.9.2"
        compile "org.commonjava.mimeparse:mimeparse:0.1.3.3"
        compile "com.googlecode.concurrentlinkedhashmap:concurrentlinkedhashmap-lru:1.4"
        compile "jmimemagic:jmimemagic:0.1.2"
    }

    plugins {
        build(":bintray-upload:0.2",
              ":release:3.0.1",
              ":rest-client-builder:1.0.3") {
            export = false
        }
    }
}
