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

(deftest get-list
  (log/configure-logback "/courtauction-logback.xml")
  (config/config-yaml "/application-context.yaml")
  (def sido (get (config/get-value :location.SidoCd) 0))
  (doseq [tSigu (config/get-value :location.SiguCd)]
    (def sigu (get (. tSigu split ",") 0))
    (crawler/get-list! sido sigu)
    )
  )