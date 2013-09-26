(defproject courtauction "0.1.0-SNAPSHOT"
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
                 ]
  
   :repositories [["java.net" "http://download.java.net/maven/2"]
                 ["conjars" "http://conjars.org/repo"]
                 ] 
  )
