(defproject courtauction "0.1.0-SNAPSHOT"
  :url "http://github.com/guriguri/courtauction"
  :license {:name "Apache License, Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"] 
  :test-paths ["test/clj"]
  :resource-paths ["src/resources"]
  :compile-path "target/classes" 
  :target-path "target/"   
  
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [junit "4.8"]
                 [org.clojure/tools.logging "0.2.3"]  
                 [ch.qos.logback/logback-classic "1.0.6"]
                 [org.slf4j/log4j-over-slf4j "1.6.6"]
                 [clj-yaml "0.4.0"]
                 [clj-http "0.7.2"]
                 [enlive "1.0.0"]
                 [org.clojure/java.jdbc "0.3.0-alpha5"]
                 [mysql/mysql-connector-java "5.1.25"]
                 [c3p0/c3p0 "0.9.1.2"]
                  ]
  
   :plugins [[lein-daemon "0.5.4"]]
  
   :repositories [["java.net" "http://download.java.net/maven/2"]
                 ["conjars" "http://conjars.org/repo"]
                 ] 
   
   :jar-exclusions  [#"courtauction-logback.xml"
                     #"application-context.yaml"]
   
   :daemon {:crawler {:ns courtauction.component.crawler
                      :pidfile "crawler.pid"}
            }
   
   :aot [courtauction.component.crawler]
  )
