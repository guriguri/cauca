(ns courtauction.component.crawler-test
  (:use [clojure test]      
        ) 
  (:require [courtauction.component.application-context :as application-context]
            [courtauction.component.crawler :as crawler]
            [courtauction.log :as log]
            [courtauction.config :as config]
            [clj-http.client :as client])
  )

(deftest get-list
  (config/config-yaml "/application-context.yaml")
  (crawler/get-list!)
  )