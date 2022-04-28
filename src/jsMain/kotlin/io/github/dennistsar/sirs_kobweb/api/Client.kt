package io.github.dennistsar.sirs_kobweb.api
//
//import general.API_KEY
//import io.ktor.client.*
//import io.ktor.client.engine.cio.*
//import io.ktor.client.plugins.*
//import io.ktor.client.plugins.logging.*
//import io.ktor.client.request.*
//
//val client = HttpClient() {
//    install(Logging) {
//        logger = Logger.SIMPLE
//        level = LogLevel.INFO
//    }
//
//    //These next two seem to be required when making a lot of requests
//    //Number values chosen arbitrarily, perhaps there's a better way?
//    install(HttpTimeout){
//        connectTimeoutMillis = 100000
//    }
//    engine {
//        requestTimeout = 150000
//    }
//
//    defaultRequest{
//        header("Cookie", API_KEY)
//    }
//}
//
