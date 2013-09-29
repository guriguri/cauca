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
    (let [sido (first (config/get-value :location.SidoCd))
          sido-code (first (. sido split ","))]
      (doseq [sigu @(crawler/get-sigu-list! sido-code)]
        (println "sido=" sido ", sigu=" sigu ", rows.count="
                 (count @(crawler/get-auction-list! sido-code (:id sigu) 20)))
        )
      )
    )