(ns courtauction.component.crawler-test
  (:use [clojure test]      
        ) 
  (:require [courtauction.beans :as beans]
            [courtauction.component.crawler :as crawler]
            [courtauction.log :as log]
            [courtauction.config :as config]
            [clj-http.client :as client]
            )
  )

(deftest add-courtauctions-test
  (log/configure-logback "/courtauction-logback.xml")
  (config/config-yaml "/application-context.yaml") 
  (let [dao-impl# (beans/get-obj :courtauction-dao)
        sido (first (config/get-value :location.SidoCd))]
    (crawler/add-courtauctions! dao-impl# sido 20)
    )
  )