(def VERSION (.trim (slurp "VERSION")))
(def MODULES (-> "MODULES" slurp (.split "\n")))
(def DEPENDENCIES (for [m MODULES] [(symbol (str "courtauction/" m)) VERSION]))

(eval `(defproject courtauction/parent ~VERSION
  :dependencies [~@DEPENDENCIES]
  :plugins [[~'lein-sub "0.2.1"]]  
  :min-lein-version "2.0.0"
  :sub [~@MODULES]
  ))
